package me.gavin.app;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import me.gavin.base.function.IntConsumer;
import me.gavin.base.recycler.RecyclerAdapter;
import me.gavin.base.recycler.RecyclerHolder;
import me.gavin.ext.mjx.R;
import me.gavin.ext.mjx.databinding.ItemAccountBinding;
import me.gavin.util.ImageLoader;

/**
 * 账号列表
 *
 * @author gavin.xiong 2018/4/9
 */
public class AccountAdapter extends RecyclerAdapter<Account, ItemAccountBinding> {

    private IntConsumer mListener;

    AccountAdapter(Context context, @NonNull List<Account> list) {
        super(context, list, R.layout.item_account);
    }

    void setOnItemClickListener(IntConsumer onItemClickListener) {
        this.mListener = onItemClickListener;
    }

    @Override
    protected void onBind(RecyclerHolder<ItemAccountBinding> holder, int position, Account t) {
        if (position == mList.size() - 1) {
            holder.binding.imageView.setImageResource(R.drawable.vt_add_account_24dp);
            holder.binding.tvName.setText("新增账号");
        } else {
            ImageLoader.loadAvatar(holder.binding.imageView, t.getAvatar());
            holder.binding.tvName.setText(t.getNick());
        }
        if (mListener != null) {
            holder.itemView.findViewById(R.id.item).setOnClickListener(v -> mListener.accept(position));
        }
    }
}
