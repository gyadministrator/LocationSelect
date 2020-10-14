package com.android.locationselect.util;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.android.locationselect.adapter.BaseAdapter;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.List;

/**
 * <p>文件描述：<p>
 * <p>作者：Administrator<p>
 * <p>邮箱：1984629668@qq.com<p>
 * <p>创建时间：2020/9/25<p>
 * <p>更改时间：2020/9/25<p>
 * <p>版本号：1.0<p>
 */
public class RecyclerUtils {

    private static void initRecyclerViewData(XRecyclerView recyclerView, BaseAdapter adapter, List allValues, XRecyclerView.LoadingListener loadingListener, RecyclerView.LayoutManager layoutManager, boolean isStatic) {
        int pageSize = 10;
        if (allValues.size() < pageSize) {
            recyclerView.setLoadingMoreEnabled(false);
        } else {
            recyclerView.setLoadingMoreEnabled(true);
        }
        recyclerView.setLayoutManager(layoutManager);
        if (isStatic) {
            recyclerView.setPullRefreshEnabled(false);
            recyclerView.setLoadingMoreEnabled(false);
        } else {
            recyclerView.setPullRefreshEnabled(false);
        }
        recyclerView.setRefreshProgressStyle(ProgressStyle.BallClipRotate);
        recyclerView.setLoadingMoreProgressStyle(ProgressStyle.BallClipRotate);
        recyclerView.setLoadingListener(loadingListener);
        recyclerView.getDefaultFootView().setLoadingHint("更多数据加载中...");
        recyclerView.getDefaultFootView().setNoMoreHint("到底了");
        recyclerView.getDefaultRefreshHeaderView().setRefreshTimeVisible(true);
        recyclerView.setLimitNumberToCallLoadMore(2);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        adapter.setAllValues(allValues);
        if (adapter.hasObservers()) {
            adapter.notifyDataSetChanged();
        } else {
            recyclerView.setAdapter(adapter);
        }
    }

    public static void setRecyclerViewData(boolean isRefresh, List list, XRecyclerView recyclerView, BaseAdapter adapter, RecyclerView.LayoutManager layoutManager, XRecyclerView.LoadingListener loadingListener) {
        if (recyclerView == null) return;
        if (list != null) {
            if (isRefresh) {
                recyclerView.refreshComplete();
                initRecyclerViewData(recyclerView, adapter, list, loadingListener, layoutManager, false);
            } else {
                recyclerView.loadMoreComplete();
                if (list.size() > 0) {
                    adapter.addMore(list);
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }
    }

    public static void setStaticRecyclerViewData(boolean isRefresh, List list, XRecyclerView recyclerView, BaseAdapter adapter, RecyclerView.LayoutManager layoutManager, XRecyclerView.LoadingListener loadingListener) {
        if (recyclerView == null) return;
        if (list != null) {
            if (isRefresh) {
                recyclerView.refreshComplete();
                initRecyclerViewData(recyclerView, adapter, list, loadingListener, layoutManager, true);
            } else {
                recyclerView.loadMoreComplete();
                if (list.size() > 0) {
                    adapter.addMore(list);
                } else {
                    recyclerView.setNoMore(true);
                }
            }
        }
    }
}
