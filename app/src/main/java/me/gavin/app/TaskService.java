package me.gavin.app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import me.gavin.base.App;
import me.gavin.base.RxTransformers;
import me.gavin.inject.component.ApplicationComponent;
import me.gavin.service.base.DataLayer;
import me.gavin.util.L;

/**
 * 工作 service
 *
 * @author gavin.xiong 2018/4/9
 */
public class TaskService extends Service {

    @Inject
    Lazy<DataLayer> mDataLayer;
    @Inject
    CompositeDisposable mCompositeDisposable;

    private final List<Task> tasks = new ArrayList<>();

    private Disposable mTimerDisposable;
    private CompositeDisposable mTaskCompositeDisposable;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.e("onCreate - " + this);
        ApplicationComponent.Instance.get().inject(this);
        startForeground(0x250, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e("onStartCommand - " + this);
        initTasks();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        L.e("onDestroy - " + this);
        super.onDestroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

    private void initTasks() {
        mDataLayer.get().getMjxService()
                .tasks(Task.TIME_HOPEFUL)
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(ts -> {
                    tasks.clear();
                    tasks.addAll(ts);
                    initTimer();
                });
    }

    private void initTimer() {
        Observable.fromIterable(tasks)
                .filter(task -> task.getTime() > System.currentTimeMillis() - Config.TIME_AFTER)
                .filter(task -> task.getState() != Task.STATE_SUCCESS)
                .toSortedList((o1, o2) -> o1.getTime() > o2.getTime() ? 1 : -1)
                .toObservable()
                .map(ts -> {
                    if (ts.isEmpty()) {
                        throw new NullPointerException("暂无任务");
                    }
                    NotificationHelper.notify(this, ts.size(), ts.get(0).getTime());
                    return ts.get(0);
                })
                .map(task -> {
                    long time = task.getTime() - Config.TIME_BEFORE - System.currentTimeMillis();
                    if (time > 0) {
                        throw new TimeoutException(String.valueOf(time));
                    }
                    if (mTaskCompositeDisposable != null) {
                        mTaskCompositeDisposable.dispose();
                    }
                    mTaskCompositeDisposable = new CompositeDisposable();
                    mCompositeDisposable.add(mTaskCompositeDisposable);
                    return task;
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            if (t instanceof TimeoutException) {
                                long time = Long.valueOf(t.getMessage());
                                L.e("时间未到，重新计时：" + time);
                                return Observable.timer(Math.min(1000 * 60 * 5, time / 2), TimeUnit.MILLISECONDS);
                            }
                            return Observable.error(new Throwable(t));
                        }))
//                .retryWhen(throwableObservable -> throwableObservable
//                        .zipWith(Observable.range(0, 3), (t, i) -> i)
//                        .flatMap(retryCount -> Observable.timer((long) Math.pow(5, retryCount), TimeUnit.MILLISECONDS)))
                .flatMap(task -> Observable.fromIterable(tasks))
                .filter(task -> task.getTime() > System.currentTimeMillis() - Config.TIME_AFTER)
                .filter(task -> task.getTime() < System.currentTimeMillis() + Config.TIME_AFTER)
                .filter(task -> task.getState() != Task.STATE_SUCCESS)
                .doOnSubscribe(disposable -> {
                    mCompositeDisposable.add(disposable);
                    if (mTimerDisposable != null) {
                        mTimerDisposable.dispose();
                    }
                    mTimerDisposable = disposable;
                })
                .repeatWhen(objectObservable -> objectObservable
                        .delay(Config.TIME_BEFORE + Config.TIME_AFTER + 1000 * 60, TimeUnit.MILLISECONDS)
                        .flatMap(arg0 -> {
                            initTasks();
                            return Observable.empty();
                        }))
                .subscribe(this::task, t -> {
                    L.e("stopSelf - " + t);
                    stopSelf();
                });
    }

    private void task(Task task) {
        L.e("任务开始 - " + task);
        mDataLayer.get().getMjxService()
                .task(task)
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mTaskCompositeDisposable::add)
                .subscribe(aBoolean -> {
                    task.setState(Task.STATE_SUCCESS);
                    L.e("任务结束 - 成功 - " + task);
                    mDataLayer.get().getMjxService().insertOrReplace(task);
                    NotificationHelper.notify(App.get(), task, "成功");
                }, t -> {
                    L.e("任务结束 - 失败 - " + task + " - " + t.toString());
                    mDataLayer.get().getMjxService().insertOrReplace(task);
                    NotificationHelper.notify(App.get(), task, t.getMessage());
                });
    }
}
