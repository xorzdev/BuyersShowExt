package me.gavin.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;
import java.util.List;

import me.gavin.base.BindingActivity;
import me.gavin.base.BundleKey;
import me.gavin.base.RequestCode;
import me.gavin.base.RxTransformers;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ActivityMainBinding;
import me.gavin.ext.mjx.databinding.DialogLoginBinding;
import me.gavin.util.InputUtil;

public class MainActivity extends BindingActivity<ActivityMainBinding> {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void afterCreate(@Nullable Bundle savedInstanceState) {
        mBinding.fab.setOnClickListener(v -> {
//            if (accounts.isEmpty()) {
                showLoginDialog();
//            }
        });
    }

    private void getData() {
//        getDataLayer().getMjxService()
//                .getAccount()
//                .compose(RxTransformers.applySchedulers())
//                .subscribe(L::e, t -> {
//                    if ("未登录".equals(t.getMessage())) {
//                        Snackbar.make(mBinding.recycler, "未登录~~~", Snackbar.LENGTH_LONG).show();
//                        // showLoginDialog();
//                        startActivityForResult(new Intent(this, LoginActivity.class), RequestCode.ZOOM);
//                    } else {
//                        Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show();
//                    }
//                });
    }

    private void showLoginDialog() {
        DialogLoginBinding binding = DialogLoginBinding.inflate(getLayoutInflater());
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("登录")
                .setView(binding.getRoot())
                .setPositiveButton("登录", (dialog, which)
                        -> login(binding.etPhone.getText().toString(), binding.etPass.getText().toString()))
                .setNegativeButton("取消", null)
                .show();
        binding.etPhone.post(() -> InputUtil.show(this, binding.etPhone));
        binding.etPass.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                alertDialog.dismiss();
                login(binding.etPhone.toString(), binding.etPass.getText().toString());
            }
            return true;
        });

        binding.etPhone.setText("18520776634");
        binding.etPass.setText("921127");
    }

    private void login(String phone, String pass) {
        getDataLayer().getMjxService()
                .login(phone, pass)
                .compose(RxTransformers.applySchedulers())
                .subscribe(cookie -> {
                    Account account = new Account();
                    account.setPhone(phone);
                    account.setPass(pass);
                    account.setCookie(cookie);
                    getDataLayer().getMjxService().insertOrReplace(account);
                    startActivityForResult(new Intent(this, AddActivity.class)
                            .putExtra(BundleKey.PHONE, phone), RequestCode.TAKE_PHOTO);
                }, t -> {
                    Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show();
                    showLoginDialog();
                });
    }
}
