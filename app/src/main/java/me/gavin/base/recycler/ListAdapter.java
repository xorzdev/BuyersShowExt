package me.gavin.base.recycler;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class ListAdapter<T, B extends ViewDataBinding> extends BaseAdapter {

    protected final Context mContext;
    private final int layoutId;
    protected final List<T> mList;

    public ListAdapter(Context context, @NonNull List<T> list, @LayoutRes int layoutId) {
        this.mContext = context;
        this.mList = list;
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListHolder<B> holder;
        if (convertView == null) {
            B binding = DataBindingUtil.inflate(LayoutInflater.from(mContext), layoutId, parent, false);
            holder = new ListHolder<>(binding);
            convertView = holder.binding.getRoot();
            convertView.setTag(holder);
        } else {
            holder = (ListHolder<B>) convertView.getTag();
        }
        onBind(holder, position, mList.get(position));
        return convertView;
    }

    protected abstract void onBind(ListHolder<B> holder, int position, T t);
}