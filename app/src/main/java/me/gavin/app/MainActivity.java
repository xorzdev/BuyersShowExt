package me.gavin.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import java.util.ArrayList;
import java.util.List;

import me.gavin.base.BindingActivity;
import me.gavin.base.BundleKey;
import me.gavin.base.RequestCode;
import me.gavin.base.RxTransformers;
import me.gavin.base.recycler.BindingAdapter;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ActivityMainBinding;
import me.gavin.ext.mjx.databinding.DialogAccountBinding;
import me.gavin.ext.mjx.databinding.DialogLoginBinding;
import me.gavin.inject.component.ApplicationComponent;
import me.gavin.util.InputUtil;

public class MainActivity extends BindingActivity<ActivityMainBinding> {

    private final List<Task> mList = new ArrayList<>();
    private BindingAdapter<Task> mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void afterCreate(@Nullable Bundle savedInstanceState) {
        startService(new Intent(this, TaskService.class));

        mBinding.refresh.setOnRefreshListener(this::getData);

        mAdapter = new BindingAdapter<>(this, mList, R.layout.item_task);
        mAdapter.setOnItemClickListener(i -> {

        });
        mBinding.recycler.setAdapter(mAdapter);

        mBinding.fab.setOnClickListener(v -> showSelectAccountDialog());

//        initDebugData();
        getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.fab.show();
//        getData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBinding.fab.hide();
    }

    private void getData() {
        getDataLayer().getMjxService()
                .tasks()
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(disposable -> {
                    mCompositeDisposable.add(disposable);
                    mBinding.refresh.setRefreshing(true);
                })
                .doOnComplete(() -> mBinding.refresh.setRefreshing(false))
                .doOnError(t -> mBinding.refresh.setRefreshing(false))
                .subscribe(tasks -> {
                    mList.clear();
                    mList.addAll(tasks);
                    mAdapter.notifyDataSetChanged();
                }, t -> Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private void showSelectAccountDialog() {
        DialogAccountBinding binding = DialogAccountBinding.inflate(getLayoutInflater());
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择账号")
                .setView(binding.getRoot())
                .show();
        List<Account> accounts = ApplicationComponent.Instance.get().getDaoSession().getAccountDao().loadAll();
        accounts.add(null);
        AccountAdapter adapter = new AccountAdapter(this, accounts);
        adapter.setOnItemClickListener(i -> {
            if (i == accounts.size() - 1) {
                dialog.dismiss();
                showLoginDialog();
            } else {
                dialog.dismiss();
                startActivityForResult(new Intent(this, AddActivity.class)
                        .putExtra(BundleKey.PHONE, accounts.get(i).getPhone()), RequestCode.TAKE_PHOTO);
            }
        });
        binding.recycler.setAdapter(adapter);
    }

    private void showLoginDialog() {
        DialogLoginBinding binding = DialogLoginBinding.inflate(getLayoutInflater());
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle("新增账号")
                .setView(binding.getRoot())
                // .setPositiveButton("确定", null)
                // .setNegativeButton("取消", null)
                .show();
        binding.etPhone.post(() -> InputUtil.show(this, binding.etPhone));
        binding.etPass.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.dismiss();
                addAccount(binding.etPhone.toString(), binding.etPass.getText().toString(),
                        binding.tvSure, binding.progressBar, dialog);
            }
            return true;
        });
        binding.tvSure.setOnClickListener(v ->
                addAccount(binding.etPhone.getText().toString(), binding.etPass.getText().toString(),
                        binding.tvSure, binding.progressBar, dialog));
        binding.tvCancel.setClickable(true);

        binding.etPhone.setText("18520776634");
        binding.etPass.setText("921127");
    }

    private void addAccount(String phone, String pass, View v, View progress, Dialog dialog) {
        getDataLayer().getMjxService()
                .login(phone, pass)
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(disposable -> {
                    mCompositeDisposable.add(disposable);
                    v.setVisibility(View.INVISIBLE);
                    progress.setVisibility(View.VISIBLE);
                })
                .doOnComplete(dialog::dismiss)
                .doOnError(throwable -> {
                    v.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                })
                .compose(RxTransformers.log())
                .subscribe(account -> {
                    getDataLayer().getMjxService().insertOrReplace(account);
                    startActivityForResult(new Intent(this, AddActivity.class)
                            .putExtra(BundleKey.PHONE, phone), RequestCode.TAKE_PHOTO);
                }, t -> Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private void initDebugData() {
        long time = System.currentTimeMillis();
        List<Task> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Task task = new Task();
            task.setName("任务_" + i + " - ");
            task.setId(1000 + i);
            task.setIds(String.valueOf(task.getId()));
            task.setToken("~~~~~~");
            task.setTime(time + 1000 * 5 + 1000 * 60 * (i / 2 - 2));
            list.add(task);
        }
        ApplicationComponent.Instance.get().getDaoSession().getTaskDao().deleteAll();
        ApplicationComponent.Instance.get().getDaoSession().getTaskDao().saveInTx(list);
    }
}
