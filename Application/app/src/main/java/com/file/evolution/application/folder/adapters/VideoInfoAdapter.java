package com.file.evolution.application.folder.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import com.file.evolution.R;
import com.file.evolution.application.folder.VideoInfo;

public class VideoInfoAdapter extends RecyclerView.Adapter<VideoInfoAdapter.viewHolder> {

    private Context mContext;
    private ArrayList<VideoInfo> urlList;
    public VideoInfoAdapter(Context context, ArrayList<VideoInfo> urlList) {
        this.mContext = context;
        this.urlList = urlList; 
    }
    
    @Override
    public VideoInfoAdapter .viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.videoinfo_items, viewGroup, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoInfoAdapter.viewHolder holder, final int position) {
        final VideoInfo video = urlList.get(position);   
        
        holder.title.setText(video.getVideoInfoTitle());     
        holder.value.setText(video.getVideoInfoValue());      
    }

    @Override
    public int getItemCount() {
        if (urlList != null) {
            return urlList.size();
        }
        return 0;
    }


    public class viewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView value;
        
        public viewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.Details_ID);
            value = (TextView) itemView.findViewById(R.id.Details_VALUES);    
        }

    }

}
