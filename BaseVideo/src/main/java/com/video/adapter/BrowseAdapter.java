package com.video.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hpplay.sdk.source.browse.api.LelinkServiceInfo;
import com.huoyan.basevideo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zippo on 2018/6/8.
 * Date: 2018/6/8
 * Time: 17:00:39
 */
public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.RecyclerHolder> {

    private static final String TAG = "BrowseAdapter";
    private Context mContext;
    private List<LelinkServiceInfo> mDatas;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;

    public interface OnItemClickListener {
        void onClick(int position, LelinkServiceInfo pInfo);
    }

    public BrowseAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDatas = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.device_item, parent, false);
        return new RecyclerHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, final int position) {
        final LelinkServiceInfo info = mDatas.get(position);
        if (null == info) {
            return;
        }
        holder.name.setText(info.getName());
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mItemClickListener) {
                    mItemClickListener.onClick(position,info);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == mDatas ? 0 : mDatas.size();
    }

    public void updateDatas(List<LelinkServiceInfo> infos) {
        if (null != infos) {
            mDatas.clear();
            mDatas.addAll(infos);
            notifyDataSetChanged();
        }
    }

    class RecyclerHolder extends RecyclerView.ViewHolder {

        TextView name;
        RelativeLayout item;

        private RecyclerHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
            item = (RelativeLayout) itemView.findViewById(R.id.item);
        }

    }
}
