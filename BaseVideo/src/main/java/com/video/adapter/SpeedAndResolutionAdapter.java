package com.video.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.huoyan.basevideo.R.id;
import com.huoyan.basevideo.R.layout;
import com.video.model.ResolutionModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpeedAndResolutionAdapter extends Adapter<SpeedAndResolutionAdapter.VH> {
    private List<String> speed;
    private List<ResolutionModel> resolution;
    private int selectColor = Color.parseColor("#F06950");
    private int defaultColor = Color.parseColor("#ffffff");
    private boolean isSpeed = false;
    private String isSelect = "";
    private Context mContext;
    private SpeedAndResolutionAdapter.SelectListener mSelectListener;

    public SpeedAndResolutionAdapter(Context mContext) {
        this.mContext = mContext;
        this.speed = new ArrayList();
        this.resolution = new ArrayList();
    }

    public void initSpeed(String isSelect) {
        this.isSpeed = true;
        this.isSelect = isSelect.replace("x", "");
        this.speed = Arrays.asList("2.0", "1.5", "1.25", "1.0", "0.5");
        this.notifyDataSetChanged();
    }

    public void initResoultion(List<ResolutionModel> resolution, String isSelect) {
        this.isSpeed = false;
        this.resolution = resolution;
        this.isSelect = isSelect;
        this.notifyDataSetChanged();
    }

    public void setSelectListener(SpeedAndResolutionAdapter.SelectListener selectListener) {
        this.mSelectListener = selectListener;
    }

    @NonNull
    public SpeedAndResolutionAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(this.mContext).inflate(layout.speed_resolution_item, parent, false);
        return new SpeedAndResolutionAdapter.VH(view);
    }

    public void onBindViewHolder(@NonNull SpeedAndResolutionAdapter.VH holder, final int position) {
        if (this.isSpeed) {
            holder.speed.setVisibility(View.VISIBLE);
            holder.resolution.setVisibility(View.GONE);
            holder.name.setText((CharSequence)this.speed.get(position));
            boolean spb = this.isSelect.equals(this.speed.get(position));
            holder.name.setTextColor(spb ? this.selectColor : this.defaultColor);
            holder.speed.setTextColor(spb ? this.selectColor : this.defaultColor);
        } else {
            holder.speed.setVisibility(View.GONE);
            holder.resolution.setVisibility(View.VISIBLE);
            ResolutionModel resolutionModel = (ResolutionModel)this.resolution.get(position);
            holder.name.setText(resolutionModel.resolution);
            holder.resolution.setText(resolutionModel.resolutionName);
            boolean reb = this.isSelect.equals(resolutionModel.resolutionName);
            holder.name.setTextColor(reb ? this.selectColor : this.defaultColor);
            holder.resolution.setTextColor(reb ? this.selectColor : this.defaultColor);
        }

        holder.item.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (SpeedAndResolutionAdapter.this.mSelectListener != null) {
                    if (SpeedAndResolutionAdapter.this.isSpeed) {
                        SpeedAndResolutionAdapter.this.mSelectListener.speed(Float.valueOf((String)SpeedAndResolutionAdapter.this.speed.get(position)));
                    } else {
                        SpeedAndResolutionAdapter.this.mSelectListener.resolution((ResolutionModel)SpeedAndResolutionAdapter.this.resolution.get(position));
                    }
                }

            }
        });
    }

    public int getItemCount() {
        return this.isSpeed ? this.speed.size() : this.resolution.size();
    }

    public int getItemViewType(int isSelect) {
        return isSelect;
    }

    public interface SelectListener {
        void speed(float var1);

        void resolution(ResolutionModel var1);
    }

    class VH extends ViewHolder {
        TextView name;
        TextView speed;
        TextView resolution;
        LinearLayout item;

        public VH(@NonNull View itemView) {
            super(itemView);
            this.name = (TextView)itemView.findViewById(id.name);
            this.speed = (TextView)itemView.findViewById(id.speed);
            this.resolution = (TextView)itemView.findViewById(id.resolution);
            this.item = (LinearLayout)itemView.findViewById(id.item);
        }
    }
}
