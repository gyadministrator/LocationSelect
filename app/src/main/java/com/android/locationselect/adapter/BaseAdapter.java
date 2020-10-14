package com.android.locationselect.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: BaseAdapter
 * @Description:
 * @Author: gy
 * @CreateDate: 20-4-13 下午12:06
 * @UpdateUser: gy
 * @UpdateDate: 20-4-13 下午12:06
 * @UpdateRemark:
 * @Version: 1.0
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //viewType--分别为item以及空view
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_EMPTY = 0;
    protected List<T> allValues = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_EMPTY) {
            return setEmptyView(parent);
        } else {
            return setContentView(parent);
        }
    }

    protected abstract RecyclerView.ViewHolder setContentView(ViewGroup parent);

    protected abstract RecyclerView.ViewHolder setEmptyView(ViewGroup parent);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemViewType(int position) {
        //在这里进行判断，如果我们的集合的长度为0时，我们就使用emptyView的布局
        if (allValues.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }
        //如果有数据，则使用ITEM的布局
        return VIEW_TYPE_ITEM;
    }

    /**
     * 加载更多
     * @param more 更多
     */
    public void addMore(List<T> more) {
        allValues.addAll(more);
        notifyItemChanged(allValues.size() - more.size());
    }

    @Override
    public int getItemCount() {
        if (allValues != null && allValues.size() == 0) {
            return 1;
        }
        return allValues == null ? 0 : allValues.size();
    }

    public void setAllValues(List<T> allValues) {
        this.allValues = allValues;
    }
}
