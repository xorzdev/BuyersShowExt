package me.gavin.service;

import android.database.Cursor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import me.gavin.app.Account;
import me.gavin.app.ModelResult;
import me.gavin.app.Task;
import me.gavin.base.RxBus;
import me.gavin.db.dao.AccountDao;
import me.gavin.db.dao.TaskDao;
import me.gavin.service.base.BaseManager;
import me.gavin.service.base.DataLayer;
import me.gavin.util.L;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

/**
 * MjxManager
 *
 * @author gavin.xiong 2017/4/28
 */
public class MjxManager extends BaseManager implements DataLayer.MjxService {

    @Override
    public Observable<Account> login(String phone, String pass) {
        return getApi().login(phone, pass)
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .map(document -> {
                    if ("发生错误".equals(document.head().tagName("title").text())) {
                        throw new IllegalArgumentException(document
                                .selectFirst("div[class=error_info] div[class=error_content]  div[class=warning_text]")
                                .text());
                    }
                    return document;
                })
                .map(Document::body)
                .map(Element::text)
                .map(cookie -> {
                    Account account = new Account();
                    account.setPhone(phone);
                    account.setPass(pass);
                    account.setCookie(cookie);
                    return account;
                })
                .flatMap(this::getAccount);
    }

    private Observable<Account> getAccount(Account account) {
        return getApi().getAccount(account.getCookie())
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .map(document -> {
                    String name = document.selectFirst("div[id=container] div[class=header bg] div[class=u-text-pink f-center nickname]").text();
                    String avatar = document.selectFirst("div[id=container] div[class=header bg] div[class=avatar bg A]").attr("style");
                    avatar = avatar.substring(avatar.indexOf("url(") + 4, avatar.length() - 1);
                    account.setNick(name);
                    account.setAvatar("http://www.maijiaxiuwang.com" + avatar);
                    return account;
                });
    }

    @Override
    public void insertOrReplace(Account account) {
        getDaoSession().getAccountDao().insertOrReplace(account);
    }

    @Override
    public Observable<ModelResult> getWaiting(String cookie) {
        return getApi().getWaiting(cookie);
    }

    @Override
    public Observable<String> getToken(String cookie, long id, String ids) {
        return getApi().getDetail(cookie, id, "preview", ids)
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .compose(reLogin(cookie))
                .map(document -> document.selectFirst("div[id=app]"))
                .map(element -> element.attr("data-token"));
    }

    @Override
    public void insertOrReplace(Task task) {
        getDaoSession().getTaskDao().insertOrReplace(task);
    }

    @Override
    public Observable<List<Task>> tasks() {
        String sql = " SELECT * FROM TASK LEFT JOIN ACCOUNT ON TASK.PHONE = ACCOUNT.PHONE WHERE TASK.STATE = ? ";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(sql, new String[]{"0"});
        List<Task> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Task task = new Task(
                    cursor.getLong(cursor.getColumnIndex(TaskDao.Properties._id.columnName)),
                    cursor.getLong(cursor.getColumnIndex(TaskDao.Properties.Id.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Ids.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Token.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Phone.columnName)),
                    cursor.getInt(cursor.getColumnIndex(TaskDao.Properties.State.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Name.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Cover.columnName)),
                    cursor.getLong(cursor.getColumnIndex(TaskDao.Properties.Time.columnName))
            );
            String cookie = cursor.getString(cursor.getColumnIndex(AccountDao.Properties.Cookie.columnName));
            task.setCookie(cookie);
            result.add(task);
        }
        cursor.close();
        return Observable.just(result);
    }

    private Observable<ResponseBody> debug() {
        L.e("debug - " + System.currentTimeMillis());
        return Observable.just(ResponseBody.create(MediaType.parse("text/plain"), " ~~~~~~ "));
    }

    @Override
    public Observable<Boolean> task(Task task) {
        return getApi().task(task.getCookie(), task.getId(), task.getToken(), task.getIds().split(","))
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .compose(reLogin(task.getCookie()))
                .map(document -> {
                    if ("发生错误".equals(document.head().tagName("title").text())) {
                        throw new IllegalArgumentException(document
                                .selectFirst("div[class=error_info] div[class=error_content]  div[class=warning_text]")
                                .text());
                    }
                    return document;
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            if (System.currentTimeMillis() - task.getTime() > 1000 * 10) {
                                return Observable.error(new Throwable("尽力了"));
                            }
                            return Observable.just(0).delay(1050, TimeUnit.MILLISECONDS);
                        }))
                .map(document -> true);
    }

    /**
     * 如果 token 过期 - 重新登录并报错
     */
    private ObservableTransformer<Document, Document> reLogin(String cookie) {
        return upstream -> upstream
                .flatMap(document -> {
                    if ("登录".equals(document.head().tagName("title").text())) {
                        Account account = getDaoSession()
                                .getAccountDao()
                                .queryBuilder()
                                .where(AccountDao.Properties.Cookie.eq(cookie))
                                .uniqueOrThrow();
                        return login(account.getPhone(), account.getPass())
                                .map(account1 -> {
                                    account.setCookie(account1.getCookie());
                                    getDaoSession().getAccountDao().update(account);
                                    RxBus.get().post(account);
                                    throw new NullPointerException("需登录");
                                });
                    }
                    return Observable.just(document);
                });
    }
}
