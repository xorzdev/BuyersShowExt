package me.gavin.service;

import android.accounts.AccountsException;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import me.gavin.app.Account;
import me.gavin.app.Config;
import me.gavin.app.ModelResult;
import me.gavin.app.NotificationHelper;
import me.gavin.app.Task;
import me.gavin.app.Temp;
import me.gavin.base.App;
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
    public Observable<String> getCookie(String phone, String pass) {
        return getApi().login(phone, pass)
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .map(document -> {
                    if ("发生错误".equals(document.title())) {
                        throw new IllegalStateException(document
                                .selectFirst("div[class=error_info] div[class=error_content]  div[class=warning_text]")
                                .text());
                    }
                    return document;
                })
                .map(Document::body)
                .map(Element::text);
    }

    @Override
    public Observable<Account> login(String phone, String pass) {
        return getCookie(phone, pass)
                .map(cookie -> {
                    Account account = new Account();
                    account.setPhone(phone);
                    account.setPass(pass);
                    account.setCookie(cookie);
                    return account;
                })
                .flatMap(this::getAccount);
    }

    private Observable<String> getCookie(String phone) {
        return Observable.defer(() -> Observable
                .just(getDaoSession().getAccountDao().load(phone).getCookie()));
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
                    getDaoSession().getAccountDao().insertOrReplace(account);
                    return account;
                });
    }

    @Override
    public Observable<List<Task>> getWaiting(String phone, String category) {
        return getCookie(phone)
                .flatMap(cookie -> getApi().getWaiting(cookie, category, "waiting"))
                .map(ResponseBody::string)
                .compose(reLogin(phone))
                .map(json -> getGson().fromJson(json, ModelResult.class))
                .map(modelResult -> modelResult.data)
                .flatMap(Observable::fromIterable)
                .map(Task::format)
                .toSortedList((o1, o2) -> o1.getHour() - o2.getHour())
                .toObservable();
    }

    @Override
    public Observable<String> getToken(String phone, long id, String ids) {
        return getCookie(phone)
                .flatMap(cookie -> getApi().getDetail(cookie, id, "preview", ids))
                .map(ResponseBody::string)
                .compose(reLogin(phone))
                .map(Jsoup::parse)
                .map(document -> document.selectFirst("div[id=app]"))
                .map(element -> element.attr("data-token"));
    }

    @Override
    public Observable<String> getTokenWithCheckTemp(String phone, long id, String ids) {
        return checkTemp(phone)
                .flatMap(valid -> {
                    if (valid) {
                        return getToken(phone, id, ids);
                    }
                    throw new AccountsException("软件试用过期，无法添加任务");
                });
    }

    @Override
    public long insertOrReplace(Task task) {
        return getDaoSession().getTaskDao().insertOrReplace(task);
    }

    @Override
    public Observable<List<Task>> tasks(int time) {
        QueryBuilder<Task> queryBuilder = getDaoSession().getTaskDao().queryBuilder();
        if (time == Task.TIME_TODAY) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long start = cal.getTimeInMillis();
            long end = start + TimeUnit.DAYS.toMillis(1) - 1;
            queryBuilder = queryBuilder.where(TaskDao.Properties.Time.between(start, end));
        } else if (time == Task.TIME_HOPEFUL) {
            queryBuilder = queryBuilder.where(TaskDao.Properties.Time.gt(System.currentTimeMillis() - Config.TIME_AFTER));
        }
        List<Task> result = queryBuilder.list();
        long millis = System.currentTimeMillis() - Config.TIME_AFTER;
        return Observable.just(result)
                .flatMap(Observable::fromIterable)
                .toSortedList((o1, o2) -> {
                    if (o1.getTime() >= millis && o2.getTime() >= millis) {
                        // 都没结束 - 先时间从小到大 - 后状态值从大到小
                        return o1.getTime() == o2.getTime() ? o2.getState() - o1.getState()
                                : (int) (o1.getTime() / TimeUnit.HOURS.toMillis(1) - o2.getTime() / TimeUnit.HOURS.toMillis(1));
                    } else if (o1.getTime() < millis && o2.getTime() < millis) {
                        // 都已结束 - 先状态从大到小 - 后时间从大到小
                        return o1.getState() == o2.getState()
                                ? (int) (o2.getTime() / TimeUnit.HOURS.toMillis(1) - o1.getTime() / TimeUnit.HOURS.toMillis(1))
                                : o2.getState() - o1.getState();
                    } else {
                        // 一个结束一个没结束 - 时间从大到小
                        return (int) (o2.getTime() / TimeUnit.HOURS.toMillis(1) - o1.getTime() / TimeUnit.HOURS.toMillis(1));
                    }
                })
                .toObservable();
    }

    @Override
    public Observable<Boolean> taskOnce(Task task) {
        return Observable.defer(() -> {
            task.setCount(task.getCount() + 1);
            getDaoSession().getTaskDao().update(task);
            String referer = String.format("http: //www.maijiaxiuwang.com/buyer/plan/%s?show_type=now&ids=%s", task.getId(), task.getIds());
            return getApi().task(task.getCookie(), referer, task.getId(), task.getToken(), task.getIds().split(","));
        })
                .map(ResponseBody::string)
                .map(Jsoup::parse)
                .map(document -> {
                    try {
                        JSONObject jsonObject = new JSONObject(document.body().text());
                        if (jsonObject.get("task_id") != null) {
                            return true; // 成功
                        }
                    } catch (JSONException e) {
                        // do nothing
                    }
                    if ("测评详情".equals(document.title())) {
                        return true; // 成功?
                    } else if ("发生错误".equals(document.title())) {
                        throw new IllegalStateException(document
                                .selectFirst("div[class=error_info] div[class=error_content]  div[class=warning_text]")
                                .text());
                    }
                    throw new IllegalStateException("未知错误 - " + document);
                });
    }

    @Override
    public Observable<Boolean> task(Task task) {
        return taskOnce(task)
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            L.e("retryWhen - 非登录过期" + t);
                            NotificationHelper.notify(App.get(), task, t.getMessage());
                            if (task.getTime() < System.currentTimeMillis() - Config.TIME_AFTER) {
                                return Observable.error(new Throwable(t.getMessage()));
                            }
                            return Observable.just(0).delay(Config.TIME_MIN
                                    + Math.round(Math.random() * (Config.TIME_MAX - Config.TIME_MIN)), TimeUnit.MILLISECONDS);
                        }));
    }

    /**
     * 如果 token 过期 - 重新登录并重试
     */
    private ObservableTransformer<String, String> reLogin(String phone) {
        return upstream -> upstream
                .flatMap(s -> {
                    if ("登录".equals(Jsoup.parse(s).title())) {
                        Account account = getDaoSession().getAccountDao().load(phone);
                        return getCookie(account.getPhone(), account.getPass())
                                .map(cookie -> {
                                    account.setCookie(cookie);
                                    getDaoSession().getAccountDao().update(account);
                                    throw new AccountsException("登录失效");
                                });
                    }
                    return Observable.just(s);
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            L.e("retryWhen - 登录过期");
                            if (t instanceof AccountsException && "登录失效".equals(t.getMessage())) {
                                return Observable.just(0);
                            }
                            return Observable.error(new Throwable(t));
                        }));
    }

    @Override
    public Observable<Boolean> checkTemp(String phone) {
        return getApi().getTemps()
                .map(temps -> {
                    L.e(temps);
                    for (Temp t : temps) {
                        if (phone.equals(t.getPhone())) {
                            return t.isValid();
                        }
                    }
                    return false;
                });
    }
}
