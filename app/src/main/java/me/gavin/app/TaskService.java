package me.gavin.app;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
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

    private Handler mHandler;

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

        mHandler = new Handler(msg -> {
            switch (msg.what) {

            }
            return false;
        });

        initTasks();
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
        Observable.interval(400, TimeUnit.MILLISECONDS)
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(arg0 -> {

                });
    }
}
