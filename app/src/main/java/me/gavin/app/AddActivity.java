package me.gavin.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import me.gavin.base.BindingActivity;
import me.gavin.base.BundleKey;
import me.gavin.base.RxBus;
import me.gavin.base.RxTransformers;
import me.gavin.base.recycler.BindingAdapter;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ActivityMainBinding;
import me.gavin.inject.component.ApplicationComponent;

public class AddActivity extends BindingActivity<ActivityMainBinding> {

    private Account mAccount;

    private final List<Task> mList = new ArrayList<>();
    private BindingAdapter<Task> mAdapter;

    private String mCategory = Task.CATEGORY_TEST;

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

        mBinding.includeBar.toolbar.setTitle("待发放");
        mBinding.includeBar.toolbar.setNavigationIcon(R.drawable.vt_arrow_back_black_24dp);
        mBinding.includeBar.toolbar.setNavigationOnClickListener(v -> finish());
        mBinding.includeBar.toolbar.inflateMenu(R.menu.menu_category);
        MenuItem menuType = mBinding.includeBar.toolbar.getMenu().findItem(R.id.action_category);
        mBinding.includeBar.toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.category_love:
                    menuType.setTitle(item.getTitle());
                    mCategory = Task.CATEGORY_LOVE;
                    getData();
                    return true;
                case R.id.category_test:
                    menuType.setTitle(item.getTitle());
                    mCategory = Task.CATEGORY_TEST;
                    getData();
                    return true;
                case R.id.category_image:
                    menuType.setTitle(item.getTitle());
                    mCategory = Task.CATEGORY_IMAGE;
                    getData();
                    return true;
                case R.id.category_video:
                    menuType.setTitle(item.getTitle());
                    mCategory = Task.CATEGORY_VIDEO;
                    getData();
                    return true;
                default:
                    return false;
            }
        });

        mBinding.refresh.setOnRefreshListener(this::getData);

        mAdapter = new BindingAdapter<>(this, mList, R.layout.item_task);
        mAdapter.setOnItemClickListener(i -> {
            // TODO: 2018/4/10 详情
        });
        mBinding.recycler.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback mCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                getToken(mList.get(viewHolder.getAdapterPosition()));
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mCallback);
        itemTouchHelper.attachToRecyclerView(mBinding.recycler);

        getData();
    }

    private void getData() {
        getDataLayer().getMjxService()
                .getWaiting(mAccount.getPhone(), mCategory)
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(disposable -> {
                    mCompositeDisposable.add(disposable);
                    mBinding.refresh.setRefreshing(true);
                })
                .doOnComplete(() -> mBinding.refresh.setRefreshing(false))
                .doOnError(t -> mBinding.refresh.setRefreshing(false))
                .subscribe(list -> {
                    mList.clear();
                    mList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                }, t -> Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show());
    }

    private void getToken(Task task) {
        getDataLayer().getMjxService()
                .getToken(mAccount.getPhone(), task.getId(), task.getIds())
                .compose(RxTransformers.applySchedulers())
                .doOnSubscribe(mCompositeDisposable::add)
                .subscribe(token -> {
                    long id = getDataLayer().getMjxService().insertOrReplace(task.format(token, mAccount.getPhone()));
                    Snackbar.make(mBinding.recycler, "任务添加成功", Snackbar.LENGTH_LONG)
                            .setAction("删除", v -> {
                                ApplicationComponent.Instance.get().getDaoSession().getTaskDao().deleteByKey(id);
                                Snackbar.make(mBinding.recycler, "任务已删除", Snackbar.LENGTH_LONG).show();
                                RxBus.get().post(task);
                            })
                            .show();
                    RxBus.get().post(task);
                }, t -> Snackbar.make(mBinding.recycler, t.getMessage(), Snackbar.LENGTH_LONG).show());
    }
}
