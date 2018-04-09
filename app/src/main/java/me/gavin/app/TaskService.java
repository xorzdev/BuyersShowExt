package me.gavin.app;

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
import io.reactivex.functions.Function;
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
        initTasks();
        initTimer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e("onCreate - " + this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        L.e("onCreate - " + this);
        super.onDestroy();
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
    }

    private void initTasks() {
        mDataLayer.get().getMjxService()
                .tasks()
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(ts -> {
                    tasks.clear();
                    tasks.addAll(ts);
                });
    }

    private void initTimer() {
        Observable.fromIterable(tasks)
                .filter(task -> task.getTime() - Config.TIME_BEFORM > System.currentTimeMillis())
                .toSortedList((o1, o2) -> o1.getTime() > o2.getTime() ? 1 : -1)
                .toObservable()
                .map(ts -> ts.get(0))
                .map(task -> {
                    long time = task.getTime() - Config.TIME_BEFORM - System.currentTimeMillis();
                    if (time > 1000) {
                        throw new TimeoutException(String.valueOf(time));
                    }
                    return task;
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>) t -> {
                            if (t instanceof TimeoutException) {
                                long time = Long.valueOf(t.getMessage());
                                return Observable.just(0).delay(time / 2, TimeUnit.MILLISECONDS);
                            }
                            return Observable.error(new Throwable("出错了！"));
                        }))
                .flatMap(task -> Observable.fromIterable(tasks))
                .filter(task -> Math.abs(task.getTime() - System.currentTimeMillis()) < 1000 * 60 * 5)
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(this::task, t -> {

                });
    }

    private void task(Task task) {
        mDataLayer.get().getMjxService()
                .task(task)
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(aBoolean -> {
                    task.setState(1);
                    mDataLayer.get().getMjxService().insertOrReplace(task);
                }, t -> {
                    task.setState(-1);
                    mDataLayer.get().getMjxService().insertOrReplace(task);
//                    Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }
}
