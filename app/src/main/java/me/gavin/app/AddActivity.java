package me.gavin.app;

import android.os.Bundle;
import android.support.annotation.Nullable;

import me.gavin.base.BindingActivity;
import me.gavin.base.BundleKey;
import me.gavin.base.RxTransformers;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ActivityMainBinding;
import me.gavin.inject.component.ApplicationComponent;
import me.gavin.util.L;

public class AddActivity extends BindingActivity<ActivityMainBinding> {

    private Account mAccount;

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
        getData();
    }

    private void getData() {
        getDataLayer().getMjxService()
                .getWaiting(mAccount.getCookie())
                .compose(RxTransformers.applySchedulers())
                .subscribe(L::e, L::e);
    }
}
