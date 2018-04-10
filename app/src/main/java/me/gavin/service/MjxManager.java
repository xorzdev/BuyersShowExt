package me.gavin.service;

import android.accounts.AccountsException;
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
import me.gavin.app.Config;
import me.gavin.app.Task;
import me.gavin.base.RxBus;
import me.gavin.db.dao.AccountDao;
import me.gavin.db.dao.TaskDao;
import me.gavin.service.base.BaseManager;
import me.gavin.service.base.DataLayer;
import me.gavin.util.L;
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
                        throw new IllegalStateException(document
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
    public Observable<List<Task>> getWaiting(String cookie, String category) {
        return getApi().getWaiting(cookie, category)
                .map(modelResult -> modelResult.data)
                .flatMap(Observable::fromIterable)
                .map(Task::format)
                .toSortedList((o1, o2) -> o1.getHour() - o2.getHour())
                .toObservable();
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
    public long insertOrReplace(Task task) {
        return getDaoSession().getTaskDao().insertOrReplace(task);
    }

    @Override
    public Observable<List<Task>> tasks() {
        String sql = " SELECT * FROM TASK LEFT JOIN ACCOUNT ON TASK.PHONE = ACCOUNT.PHONE WHERE TASK.TIME > ? AND TASK.STATE > ? ";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(sql, new String[]{String.valueOf(System.currentTimeMillis() - 1000 * 60 * 30), "-2"});
        List<Task> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Task task = new Task(
                    cursor.getLong(cursor.getColumnIndex(TaskDao.Properties._id.columnName)),
                    cursor.getLong(cursor.getColumnIndex(TaskDao.Properties.Id.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Ids.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Name.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Cover.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Type.columnName)),
                    cursor.getDouble(cursor.getColumnIndex(TaskDao.Properties.Price.columnName)),
                    cursor.getInt(cursor.getColumnIndex(TaskDao.Properties.Hour.columnName)),
                    cursor.getLong(cursor.getColumnIndex(TaskDao.Properties.Time.columnName)),
                    cursor.getInt(cursor.getColumnIndex(TaskDao.Properties.Total.columnName)),
                    cursor.getInt(cursor.getColumnIndex(TaskDao.Properties.Doing.columnName)),
                    cursor.getDouble(cursor.getColumnIndex(TaskDao.Properties.Reward.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Phone.columnName)),
                    cursor.getString(cursor.getColumnIndex(TaskDao.Properties.Token.columnName)),
                    cursor.getInt(cursor.getColumnIndex(TaskDao.Properties.State.columnName)),
                    cursor.getInt(cursor.getColumnIndex(TaskDao.Properties.Count.columnName))
            );
            String cookie = cursor.getString(cursor.getColumnIndex(AccountDao.Properties.Cookie.columnName));
            task.setCookie(cookie);
            result.add(task);
        }
        cursor.close();
        return Observable.just(result);
    }

    @Override
    public Observable<Boolean> task(Task task) {
        return getApi().task(task.getCookie(), task.getId(), task.getToken(), task.getIds().split(","))
                .map(ResponseBody::string)
//        return task2(task)
                .map(Jsoup::parse)
                .compose(reLogin(task.getCookie()))
                .map(document -> {
                    if ("发生错误".equals(document.head().tagName("title").text())) {
                        throw new IllegalStateException(document
                                .selectFirst("div[class=error_info] div[class=error_content]  div[class=warning_text]")
                                .text());
                    }
                    return document;
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            if (System.currentTimeMillis() - task.getTime() > Config.TIME_AFTER) {
                                return Observable.error(new Throwable("尽力了"));
                            }
                            return Observable.just(0).delay(Config.TIME_MIN
                                    + Math.round(Math.random() * (Config.TIME_MAX - Config.TIME_MIN)), TimeUnit.MILLISECONDS);
                        }))
                .map(document -> true);
    }

    private Observable<String> task2(Task task) {
        return Observable.defer(() -> {
            L.d("<-- " + task);
            task.setCount(task.getCount() + 1);
            return Observable.just(0);
//        }).delay(Math.round(Math.random() * 500), TimeUnit.MILLISECONDS)
        }).delay(1000, TimeUnit.MILLISECONDS)
                .map(arg0 -> {
                    L.d("--> " + task);
                    return arg0;
                })
                .map(arg0 -> "xxx");
    }

    /**
     * 如果 token 过期 - 重新登录并重试
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
                                    throw new AccountsException("登录失效");
                                });
                    }
                    return Observable.just(document);
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            if (t instanceof AccountsException && "登录失效".equals(t.getMessage())) {
                                return Observable.just(0).delay(1000, TimeUnit.MILLISECONDS);
                            }
                            return Observable.error(new Throwable(t));
                        }));
    }
}
