package me.gavin.base.recycler;

import android.databinding.ViewDataBinding;

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2018/4/9
 */
public class ListHolder<B extends ViewDataBinding> {
    public B binding;

    public ListHolder(B binding) {
        this.binding = binding;
    }
}
