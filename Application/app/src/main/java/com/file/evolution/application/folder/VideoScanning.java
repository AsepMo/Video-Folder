package com.file.evolution.application.folder;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import com.file.evolution.R;

public class VideoScanning extends VideoFolder{

    public VideoScanning(Context context) {
        super(context, R.layout.videofolder_layout_complete, R.layout.videofolder_layout_error, R.layout.videofolder_layout_loading);
    }

    public VideoScanning(Context context, AttributeSet attrs) {
        super(context, attrs, R.layout.videofolder_layout_complete, R.layout.videofolder_layout_error, R.layout.videofolder_layout_loading);
    }

    public VideoScanning(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, R.layout.videofolder_layout_complete, R.layout.videofolder_layout_error, R.layout.videofolder_layout_loading);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoScanning(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes, R.layout.videofolder_layout_complete, R.layout.videofolder_layout_error, R.layout.videofolder_layout_loading);
    }
}

