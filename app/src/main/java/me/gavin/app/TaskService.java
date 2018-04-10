package me.gavin.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

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
import me.gavin.base.RxBus;
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
        startForeground(0x250, NotificationHelper.buildNotification(this));
        subscribe();
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

    private void subscribe() {
        RxBus.get().toObservable(Account.class)
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(arg0 -> initTasks());
        RxBus.get().toObservable(Task.class)
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(arg0 -> initTasks());
    }

    private void initTasks() {
        mDataLayer.get().getMjxService()
                .tasks()
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
                .filter(task -> task.getTime() - Config.TIME_BEFORE > System.currentTimeMillis())
                .toSortedList((o1, o2) -> o1.getTime() > o2.getTime() ? 1 : -1)
                .toObservable()
                .map(ts -> {
                    if (ts.isEmpty()) {
                        throw new NullPointerException("暂无任务");
                    }
                    return ts;
                })
                .map(ts -> ts.get(0))
                .map(task -> {
                    long time = task.getTime() - Config.TIME_BEFORE - System.currentTimeMillis();
                    if (time > 1000) {
                        throw new TimeoutException(String.valueOf(time));
                    }
                    return task;
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            if (t instanceof TimeoutException) {
                                long time = Long.valueOf(t.getMessage());
                                L.e("时间未到，重新计时：" + time);
                                return Observable.just(0).delay(time / 2, TimeUnit.MILLISECONDS);
                            }
                            return Observable.error(new Throwable("出错了！"));
                        }))
                .flatMap(task -> Observable.fromIterable(tasks))
                .filter(task -> Math.abs(task.getTime() - System.currentTimeMillis()) < 1000 * 30)
                .doOnSubscribe(disposable -> {
                    mCompositeDisposable.add(disposable);
                    if (mTimerDisposable != null) {
                        mTimerDisposable.dispose();
                    }
                    mTimerDisposable = disposable;
                })
                .subscribe(this::task, t -> {
                    Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void task(Task task) {
        L.e("任务开始 - " + task);
        mDataLayer.get().getMjxService()
                .task(task)
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(aBoolean -> {
                    task.setState(1);
                    L.e("任务结束 - " + task);
                    mDataLayer.get().getMjxService().insertOrReplace(task);
                    initTimer();
                }, t -> {
                    task.setState(-1);
                    L.e("任务结束 - " + task);
                    mDataLayer.get().getMjxService().insertOrReplace(task);
                    Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
                    initTimer();
                });
    }
}
