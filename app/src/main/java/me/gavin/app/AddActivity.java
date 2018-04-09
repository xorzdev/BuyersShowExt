package me.gavin.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.gavin.base.BindingActivity;
import me.gavin.base.BundleKey;
import me.gavin.base.RxTransformers;
import me.gavin.base.recycler.BindingAdapter;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ActivityMainBinding;
import me.gavin.inject.component.ApplicationComponent;

public class AddActivity extends BindingActivity<ActivityMainBinding> {

    private Account mAccount;

    private final List<Model> mList = new ArrayList<>();
    private BindingAdapter<Model> mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void afterCreate(@Nullable Bundle savedInstanceState) {
        String phone = getIntent().getStringExtra(BundleKey.PHONE);
        mAccount = ApplicationComponent.Instance.get()
                .getDaoSession()
                .getAccountDao()
                .load(phone);
        if (mAccount == null) {
            finish();
            return;
        }

        mBinding.includeBar.toolbar.setNavigationIcon(R.drawable.vt_arrow_back_black_24dp);
        mBinding.includeBar.toolbar.setNavigationOnClickListener(v -> finish());

        mBinding.refresh.setOnRefreshListener(this::getData);

        mAdapter = new BindingAdapter<>(this, mList, R.layout.item_model);
        mAdapter.setOnItemClickListener(i -> getToken(mList.get(i)));
        mBinding.recycler.setAdapter(mAdapter);

        getData();
    }

    private void getData() {
        getDataLayer().getMjxService()
                .getWaiting(mAccount.getCookie())
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(disposable -> {
                    mCompositeDisposable.add(disposable);
                    mBinding.refresh.setRefreshing(true);
                })
                .doOnComplete(() -> mBinding.refresh.setRefreshing(false))
                .doOnError(t -> mBinding.refresh.setRefreshing(false))
                .subscribe(modelResult -> {
                    mList.clear();
                    mList.addAll(modelResult.data);
                    mAdapter.notifyDataSetChanged();
                }, t -> Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private void getToken(Model model) {
        getDataLayer().getMjxService()
                .getToken(mAccount.getCookie(), model.getId(), model.getIds())
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(token -> {
                    Task task = new Task();
                    task.setId(model.getId());
                    task.setIds(model.getIds());
                    task.setName(model.getName());
                    task.setCover(model.getCover());
                    task.setTime(getTime(model.getTime()));
                    task.setToken(token);
                    task.setPhone(mAccount.getPhone());
                    getDataLayer().getMjxService().insertOrReplace(task);
                    Snackbar.make(mBinding.recycler, "添加成功", Snackbar.LENGTH_LONG).show();
                }, t -> Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private long getTime(int time) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
