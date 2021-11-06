package com.file.evolution.application.folder.adapters;

import android.app.Activity;
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

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.file.evolution.R;
import com.file.evolution.engine.app.models.VideoData;
import com.file.evolution.engine.app.models.ActionItem;
import com.file.evolution.engine.app.preview.MimeTypes;
import com.file.evolution.application.folder.ActionMenuItem;
import com.file.evolution.application.folder.listeners.OnClickManager;
import com.file.evolution.application.folder.utils.VideoFolderUtils;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.viewHolder> {



    private Activity mContext;
    private ArrayList<VideoData> urlList;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public VideoFolderAdapter(Activity context, ArrayList<VideoData> urlList) {
        this.mContext = context;
        this.urlList = urlList; 
    }

    @Override
    public VideoFolderAdapter.viewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.video_folder, viewGroup, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(final VideoFolderAdapter.viewHolder holder, final int position) {
        final VideoData video = urlList.get(position);   
        if (urlList == null || urlList.isEmpty())
            return;
        final int P = position % urlList.size();
        
        holder.mMenuAction.setVideoThumbnail(video.getVideoThumbnail());         
        holder.mMenuAction.setVideoTitle(video.getVideoTitle());     
        holder.mMenuAction.setVideoDuration(video.getVideoDuration());
        holder.mMenuAction.setVideoSize(video.getVideoSize());
        holder.mMenuAction.setVideoLastModified(video.getVideoDate());
        holder.mMenuAction.setOnThumbnailClickListener(new ActionMenuItem.OnThumbnailClickListener(){
                @Override
                public void onThumbnailClick(View v) {
                    VideoFolderUtils.openFile(mContext, new File(video.getVideoPath()));             
                }
            });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {             
                    if (mOnItemClickListener != null)
                        mOnItemClickListener.onItemClick(holder.mMenuAction, P, v);                 
                }
            });

        OnClickManager.getInstance().onClickListener(mContext, holder.mMenuAction, video);
    }

    @Override
    public int getItemCount() {
        if (urlList != null) {
            return urlList.size();
        }
        return 0;
    }


    public class viewHolder extends RecyclerView.ViewHolder {

        ActionMenuItem mMenuAction;
        public viewHolder(View itemView) {
            super(itemView);
            mMenuAction = (ActionMenuItem) itemView.findViewById(R.id.menu_action);

        }

    }

    public interface OnItemClickListener {
        void onItemClick(ActionMenuItem mMenuAction, int position, View v);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(ActionMenuItem mMenuAction, int position, View v);
    }
}


