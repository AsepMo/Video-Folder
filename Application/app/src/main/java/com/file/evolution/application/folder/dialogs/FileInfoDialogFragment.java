package com.file.evolution.application.folder.dialogs;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import org.json.JSONException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.file.evolution.R;
import com.file.evolution.application.folder.VideoInfo;
import com.file.evolution.application.folder.adapters.VideoInfoAdapter;

public class FileInfoDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = FileInfoDialogFragment.class.getSimpleName();

    private ImageButton mIcon;
    private TextView mTitle;
    private ImageButton mEdit;
    private ImageButton mClose;
    private RecyclerView mRecyclerView;


    private ArrayList<VideoInfo> videoInfo;
    public final String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    
    private static String currentPath;
    private VideoInfoAdapter adapter;
    public static FileInfoDialogFragment newInstance(String file) {
        FileInfoDialogFragment fragment = new FileInfoDialogFragment();
        currentPath = file;
        return fragment;
    }

    private View.OnClickListener onClickListener;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }
        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.videoinfo_layout, null);
        mIcon = (ImageButton) contentView.findViewById(R.id.bottom_sheet_icon);
        mIcon.setImageResource(R.drawable.ic_video_info);

        mTitle = (TextView) contentView.findViewById(R.id.bottom_sheet_title);
        mTitle.setText("Rincian");
        mTitle.setTextColor(ContextCompat.getColor(dialog.getContext(),R.color.md_white_1000));

        mEdit = (ImageButton) contentView.findViewById(R.id.bottom_sheet_edit);
        mEdit.setOnClickListener(onClickListener);

        mClose = (ImageButton) contentView.findViewById(R.id.bottom_sheet_close);
        mClose.setImageResource(R.drawable.ic_close_circle);
        mClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

        mRecyclerView = (RecyclerView) contentView.findViewById(R.id.bottom_sheet_recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        videoInfo = new ArrayList<VideoInfo>();
        
        String format = FilenameUtils.getExtension(currentPath);     
        
        File file = new File(currentPath);
        long fileSize = file.length();
        
        Date d = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);


        videoInfo.add(new VideoInfo("Nama :", FilenameUtils.getName(currentPath)));
        videoInfo.add(new VideoInfo("Format :", format));
        videoInfo.add(new VideoInfo("Ukuran File :", Formatter.formatFileSize(getActivity(), fileSize)));
        videoInfo.add(new VideoInfo("Terakhir Di Ubah :", formatter.format(d)));
        videoInfo.add(new VideoInfo("Alur :", file.getAbsolutePath()));

        adapter = new VideoInfoAdapter(getActivity(), videoInfo);
        mRecyclerView.setAdapter(adapter);

        contentView.findViewById(R.id.ll_bottom_sheet_layout).setBackgroundColor(ContextCompat.getColor(dialog.getContext(), android.R.color.darker_gray));     
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = layoutParams.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

    }

    /**
     * Method to convert bits per second to MB/s
     *
     * @param bps float bitsPerSecond
     * @return float
     */
    private float bitsToMb(float bps) {
        return bps / (1024 * 1024);
    }

    private String getTimeString(int duration) {
        final StringBuilder sb = new StringBuilder(8);
        final int hours = duration / (60 * 60);
        final int minutes = (duration % (60 * 60)) / 60;
        final int seconds = ((duration % (60 * 60)) % 60);

        if (duration > 3600) {
            sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":");
        }

        sb.append(String.format(Locale.getDefault(), "%02d", minutes));
        sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds));

        return sb.toString();
    }

}

