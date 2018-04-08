package me.gavin.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import me.gavin.base.BindingActivity;
import me.gavin.base.BundleKey;
import me.gavin.base.RxTransformers;
import me.gavin.base.recycler.BindingAdapter;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ActivityMainBinding;
import me.gavin.inject.component.ApplicationComponent;
import me.gavin.util.L;

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
        phone = "18520776634"; // TODO: 2018/4/8
        mAccount = ApplicationComponent.Instance.get()
                .getDaoSession()
                .getAccountDao()
                .load(phone);
        if (mAccount == null) {
            finish();
            return;
        }

        mAdapter = new BindingAdapter<>(this, mList, R.layout.item_model);
        mAdapter.setOnItemClickListener(i -> {
            Model t = mList.get(i);
            Task task = new Task();
            task.setId(t.getId());
            task.setIds(t.getIds());
            task.setName(t.getName());
            task.setTime(t.getTime());
            task.setToken(""); // TODO: 2018/4/8
            task.setPhone(mAccount.getPhone());
            task(task);
        });
        mBinding.recycler.setAdapter(mAdapter);

        getData();

        mBinding.includeBar.toolbar.setOnClickListener(v -> {
            Task task = new Task();
            task.setId(451101);
            task.setIds("451101,451285");
            task.setName("unknow");
            task.setTime(17);
            task.setToken("ot18k3zrpm");
            task.setPhone(mAccount.getPhone());
            task(task);
        });
    }

    private void getData() {
        getDataLayer().getMjxService()
                .getWaiting(mAccount.getCookie())
                .compose(RxTransformers.applySchedulers())
                .subscribe(modelResult -> {
                    mList.clear();
                    mList.addAll(modelResult.data);
                    mAdapter.notifyDataSetChanged();
                }, L::e);
    }

    private void task(Task task) {
        getDataLayer().getMjxService()
                .task(mAccount.getCookie(), task.getId(), task.getToken(), task.getIds().split(","))
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(aBoolean -> {
                    task.setState(true);
                    ApplicationComponent.Instance.get().getDaoSession().update(task);
                }, L::e);
    }
}
