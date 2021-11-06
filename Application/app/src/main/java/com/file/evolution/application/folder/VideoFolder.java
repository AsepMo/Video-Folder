package com.file.evolution.application.folder;

import android.Manifest;
import android.support.annotation.RequiresApi;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import org.apache.commons.io.FilenameUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.file.evolution.R;
import com.file.evolution.application.folder.adapters.VideoFolderAdapter;
import com.file.evolution.application.folder.tasks.VideoFolderTask;
import com.file.evolution.application.folder.listeners.OnClickManager;
import com.file.evolution.application.folder.listeners.SimpleAnimListener;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.application.settings.Settings;
import com.file.evolution.engine.app.models.ActionItem;
import com.file.evolution.engine.app.models.VideoData;
import com.file.evolution.engine.app.folders.utils.SharedPref;
import com.file.evolution.engine.app.preview.MimeTypes;
import android.app.Activity;

public class VideoFolder extends RelativeLayout {

    private Activity mActivity;
    private Context mContext;
    //private static final int DISMISS_ON_COMPLETE_DELAY = 1000;
    public static final int PERMISSION_READ = 0;
    
    public enum Status {
        IDLE, LOADING, ERROR, COMPLETE, MESSAGE
    }
    /**
     * Current status of status view
     */
    private Status currentStatus;

    /**
     * Automatically hide when status changed to complete
     */
    private boolean hideOnComplete;

    /**
     * Views for each status
     */
    private View completeView;
    private View errorView;
    private View loadingView;
    private View messageView;
    /**
     * Fade in out animations
     */
    private Animation slideOut;
    private Animation slideIn;
    
    private RecyclerView mFolderList;
    private TextView mTitleMessage;
    private ImageView mIconMessage;
    private TextView mMessage;
    
    /**
     * layout inflater
     */
    private LayoutInflater inflater;

    /**
     * Handler
     */
    private Handler handler;

    public VideoFolder(Context context) {
        super(context);
        init(context, null, 0, 0, 0);
    }

    public VideoFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0, 0);
    }

    public VideoFolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, 0, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoFolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public VideoFolder(Context context, int completeLayout, int errorLayout, int loadingLayout) {
        super(context);
        init(context, null, completeLayout, errorLayout, loadingLayout);
    }

    public VideoFolder(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    public VideoFolder(Context context, AttributeSet attrs, int defStyleAttr, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoFolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int completeLayout, int errorLayout, int loadingLayout) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, completeLayout, errorLayout, loadingLayout);
    }

    private void init(Context context, AttributeSet attrs, int completeLayout, int errorLayout, int loadingLayout) {

        mContext = context;
        mActivity = (Activity)mContext;
        /**
         * Load initial values
         */
        currentStatus = Status.IDLE;
        hideOnComplete = true;
        slideIn = AnimationUtils.loadAnimation(context, R.anim.slide_in);
        slideOut = AnimationUtils.loadAnimation(context, R.anim.slide_out);
        inflater = LayoutInflater.from(context);
        handler = new Handler();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.statusview);

        /**
         * get status layout ids
         */
        int completeLayoutId = a.getResourceId(R.styleable.statusview_complete, R.layout.videofolder_layout_complete);
        int errorLayoutId = a.getResourceId(R.styleable.statusview_error, R.layout.videofolder_layout_error);
        int loadingLayoutId = a.getResourceId(R.styleable.statusview_loading, R.layout.videofolder_layout_loading);
        int messageLayoutId = a.getResourceId(R.styleable.statusview_message, R.layout.videofolder_layout_message);
        
        /**
         * inflate layouts
         */
        if (completeLayout == 0) {
            completeView = inflater.inflate(completeLayoutId, null);
            errorView = inflater.inflate(errorLayoutId, null);
            loadingView = inflater.inflate(loadingLayoutId, null);
            messageView = inflater.inflate(messageLayoutId, null);         
        } else {
            completeView = inflater.inflate(completeLayout, null);
            errorView = inflater.inflate(errorLayout, null);
            loadingView = inflater.inflate(loadingLayout, null);
            messageView = inflater.inflate(messageLayoutId, null);  
        }

        /**
         * Default layout params
         */
        completeView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        errorView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        loadingView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        messageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        /**
         * Add layout to root
         */
        addView(completeView);
        addView(errorView);
        addView(loadingView);
        addView(messageView);
        /**
         * set visibilities of childs
         */
        completeView.setVisibility(View.INVISIBLE);
        errorView.setVisibility(View.INVISIBLE);
        loadingView.setVisibility(View.INVISIBLE);
        messageView.setVisibility(View.INVISIBLE);
        
        a.recycle();
        
        mFolderList = (RecyclerView) completeView.findViewById(R.id.video_list);
        mFolderList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mFolderList.setItemAnimator(new DefaultItemAnimator());        
        mTitleMessage = (TextView) messageView.findViewById(R.id.folder_title_message);
        mIconMessage = (ImageView) messageView.findViewById(R.id.folder_icon_message);
        mMessage = (TextView) messageView.findViewById(R.id.folder_message);
    }

    public void setMessage(String title, int Icon, String message){
        mTitleMessage.setText(title);
        mIconMessage.setImageResource(Icon);
        mMessage.setText(message);
        setStatus(Status.MESSAGE);
    }
    
    public void setMessage(String title, String Icon, String message){
        mTitleMessage.setText(title);
        Glide.with(mContext)
            .load(Icon)
            .apply(new RequestOptions().placeholder(R.drawable.video_placeholder))
            .into(mIconMessage);
        mMessage.setText(message);
        setStatus(Status.MESSAGE);
    }
    
     public void setVideoScanning(final ActionMenuFolder mMenuAction, final String folder, String[] extensions) {
        setVideoFolderTask(mMenuAction, folder, extensions);
    }
    
    public void setVideoFolderTask(final ActionMenuFolder mMenuAction, final String folder, String[] extensions) {
        VideoFolderTask task = new VideoFolderTask(mActivity, folder, extensions, mMenuAction);
        task.setOnVideoTaskListener(new VideoFolderTask.OnVideoTaskListener(){
                @Override
                public void onPreExecute() {
                    mMenuAction.setDirectoryButtons(folder);     
                    setStatus(Status.LOADING);
                }
                @Override
                public void onSuccess(final ArrayList<VideoData> result) {
                    
                    VideoFolderAdapter adapter = new VideoFolderAdapter(mActivity, result);
                    mFolderList.setAdapter(adapter);
                    adapter.setOnItemClickListener(new VideoFolderAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(ActionMenuItem mMenuAction, int pos, View v) {
                                VideoData video = result.get(pos);
                                final String ext = FilenameUtils.getExtension(video.getVideoPath());
                                if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(ext)) {
                                    video.initialise(video);
                                }else{
                                    VideoFolderUtils.openFile(mActivity, new File(video.getVideoPath()));
                                    //MrToast.createInstance(getActivity()).sendShortMessage(video.getVideoTitle());
                                }
                            }
                        });
                    mMenuAction.setDirectoryButtons(folder);     
                    setStatus(Status.COMPLETE);
                }

                @Override
                public void onFailed() {
                    mMenuAction.setDirectoryButtons(folder);     
                    setStatus(Status.ERROR);
                } 
                @Override
                public void isEmpty() {
                    mMenuAction.setDirectoryButtons(folder);
                    setStatus(Status.MESSAGE);
                }
            });
        task.execute();    
    }

    
    public void setOnErrorClickListener(OnClickListener onErrorClickListener) {
        errorView.setOnClickListener(onErrorClickListener);
    }

    public void setOnLoadingClickListener(OnClickListener onLoadingClickListener) {
        loadingView.setOnClickListener(onLoadingClickListener);
    }

    public void setOnCompleteClickListener(OnClickListener onCompleteClickListener){
        completeView.setOnClickListener(onCompleteClickListener);
    }

    public void setOnMessageClickListener(OnClickListener onCompleteClickListener){
        messageView.setOnClickListener(onCompleteClickListener);
    }
    
    public View getErrorView() {
        return errorView;
    }

    public View getCompleteView() {
        return completeView;
    }

    public View getLoadingView() {
        return loadingView;
    }

    public View getMessageView() {
        return messageView;
    }
    
    public void setStatus(final Status status) {
        if (currentStatus == Status.IDLE) {
            currentStatus = status;
            enterAnimation(getCurrentView(currentStatus));
        } else if (status != Status.IDLE) {
            switchAnimation(getCurrentView(currentStatus), getCurrentView(status));
            currentStatus = status;
        } else {
            exitAnimation(getCurrentView(currentStatus));
        }

        handler.removeCallbacksAndMessages(null);
        //if (status == Status.COMPLETE)
            //handler.postDelayed(autoDismissOnComplete, DISMISS_ON_COMPLETE_DELAY);
    }
    /**
     * 
     * @return Status object 
     */
    public Status getStatus(){
        return this.currentStatus;
    }
    
    private View getCurrentView(Status status) {
        if (status == Status.IDLE)
            return null;
        else if (status == Status.COMPLETE)
            return completeView;
        else if (status == Status.ERROR)
            return errorView;
        else if (status == Status.LOADING)
            return loadingView;
        else if (status == Status.MESSAGE)
            return messageView;  
        return null;
    }

    private void switchAnimation(final View exitView, final View enterView) {
        clearAnimation();
        exitView.setVisibility(View.VISIBLE);
        exitView.startAnimation(slideOut);
        slideOut.setAnimationListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                slideOut.setAnimationListener(null);
                exitView.setVisibility(View.INVISIBLE);
                enterView.setVisibility(View.VISIBLE);
                enterView.startAnimation(slideIn);
            }
        });
    }

    private void enterAnimation(View enterView) {
        if (enterView == null)
            return;

        enterView.setVisibility(VISIBLE);
        enterView.startAnimation(slideIn);
    }

    private void exitAnimation(final View exitView) {
        if (exitView == null)
            return;

        exitView.startAnimation(slideOut);
        slideOut.setAnimationListener(new SimpleAnimListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                currentStatus = Status.IDLE;
                exitView.setVisibility(INVISIBLE);
                slideOut.setAnimationListener(null);
            }
        });
    }
}
