package com.android.locationselect.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.services.core.PoiItem;
import com.android.locationselect.R;

/**
 * <p>文件描述：<p>
 * <p>作者：Administrator<p>
 * <p>邮箱：1984629668@qq.com<p>
 * <p>创建时间：2020/9/25<p>
 * <p>更改时间：2020/9/25<p>
 * <p>版本号：1.0<p>
 */
public class LocationItemAdapter extends BaseAdapter {
    private Context context;
    private int currentPosition = 0;

    public LocationItemAdapter(Context context) {
        this.context = context;
    }

    @Override
    protected RecyclerView.ViewHolder setContentView(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.location_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected RecyclerView.ViewHolder setEmptyView(ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.empty_view, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    private void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ViewHolder) {
            currentPosition = 0;
            ViewHolder viewHolder = (ViewHolder) holder;
            if (position == currentPosition) {
                viewHolder.ivSelect.setVisibility(View.VISIBLE);
            } else {
                viewHolder.ivSelect.setVisibility(View.GONE);
            }
            final PoiItem poiItem = (PoiItem) allValues.get(position);
            if (poiItem != null) {
                int distance = poiItem.getDistance();
                String title = poiItem.getTitle();
                if (!TextUtils.isEmpty(title)) {
                    viewHolder.tvTitle.setText(title);
                }
                viewHolder.tvDesc.setText(Math.abs(distance) + "m " + poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getTitle());
            }
            viewHolder.llContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentPosition(position);
                    notifyDataSetChanged();
                    if (locationItemClickListener != null) {
                        locationItemClickListener.clickItem(poiItem);
                    }
                }
            });
        }
    }

    private OnLocationItemClickListener locationItemClickListener;

    public void setLocationItemClickListener(OnLocationItemClickListener locationItemClickListener) {
        this.locationItemClickListener = locationItemClickListener;
    }

    public interface OnLocationItemClickListener {
        void clickItem(PoiItem poiItem);
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llContent;
        TextView tvTitle;
        TextView tvDesc;
        ImageView ivSelect;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            llContent = itemView.findViewById(R.id.ll_content);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDesc = itemView.findViewById(R.id.tv_desc);
            ivSelect = itemView.findViewById(R.id.iv_select);
        }
    }
}
