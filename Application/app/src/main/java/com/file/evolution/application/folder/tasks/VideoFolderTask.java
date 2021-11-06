package com.file.evolution.application.folder.tasks;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import com.file.evolution.R;
import com.file.evolution.engine.app.models.VideoData;
import com.file.evolution.application.folder.ActionMenuFolder;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.engine.app.preview.MimeTypes;

public class VideoFolderTask extends AsyncTask<Void, Void, ArrayList<VideoData>> {

    private Activity mActivity;
    private Context mContext;
    private VideoData mVideoData;
    private ArrayList<VideoData> videoList;
    private String folder;
    private String[] extensions = new String[]{VideoFolderUtils.MP4};
    private int mCount = 0;
    private ActionMenuFolder mActionMenuFolder;
    private OnVideoTaskListener mOnVideoTaskListener;
    public VideoFolderTask(Context context, String folder, String[] extensions, ActionMenuFolder mActionMenuFolder) {
        this.mContext = context; 
        this.mActivity = (Activity)mContext;
        this.folder = folder;
        this.extensions = extensions;
        this.mActionMenuFolder = mActionMenuFolder;
        mVideoData = new VideoData(context, VideoData.FILENAME);  
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute(); 
        if (mOnVideoTaskListener != null) {
            mOnVideoTaskListener.onPreExecute();
        }
    }

    @Override
    protected void onProgressUpdate(Void[] values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

    }

    @Override
    protected void onCancelled(ArrayList<VideoData> result) {
        super.onCancelled(result);
    }


    @Override
    protected ArrayList<VideoData> doInBackground(Void[] params) {
        videoList = new ArrayList<VideoData>();
        File mFolder = new File(folder);
        if (mFolder.exists()) {
            listOfFile(mFolder);
        }

        return videoList;
    }

    @Override
    protected void onPostExecute(ArrayList<VideoData> result) {
        super.onPostExecute(result);
        if (result.size() < 1) {
            if (mOnVideoTaskListener != null) {
                mOnVideoTaskListener.isEmpty();
            }
        } else {
            VideoData video = result.get(0);
            video.initialise(video);
            if (mOnVideoTaskListener != null) {
                mOnVideoTaskListener.onSuccess(result);
            }
            mActionMenuFolder.setDirectoryButtons(folder);
        }
    }

    public void setOnVideoTaskListener(OnVideoTaskListener mOnVideoTaskListener) {
        this.mOnVideoTaskListener = mOnVideoTaskListener;
    }

    public interface OnVideoTaskListener {
        void onPreExecute();
        void onSuccess(ArrayList<VideoData> result);
        void onFailed();
        void isEmpty();
    }

    //time conversion
    public static String timeConversion(long value) {
        String songTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            songTime = String.format("%02d:%02d", mns, scs);
        }
        return songTime;
    }

    private void listOfFile(File dir) {
        File[] list = dir.listFiles();

        for (File file : list) {
            if (file.isDirectory()) {
                if (!new File(file, ".nomedia").exists() && !file.getName().startsWith(".")) {
                    Log.w("LOG", "IS DIR " + file);
                    listOfFile(file);
                }
            } else {
                String path = file.getAbsolutePath();
                //String[] extensions = new String[]{".mp4"};

                for (String ext : extensions) {
                    if (path.endsWith(ext)) {
                        String[] split = path.split("/");
                        String mTitle = split[split.length - 1];
                        mCount++;
                        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                        String date = format.format(file.lastModified());
                        final String type = FilenameUtils.getExtension(file.getAbsolutePath()); 
                         
                        VideoData videoData  = new VideoData(mContext);
                        videoData.setId(mCount);
                        videoData.setVideoTitle(mTitle);
                        videoData.setVideoUri(Uri.parse(file.getAbsolutePath()));
                        videoData.setVideoPath(file.getAbsolutePath());                  
                        videoData.setVideoThumbnail(file.getAbsolutePath());   
                        videoData.setVideoSize(FileUtils.byteCountToDisplaySize(file.length()));
                        if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(type)) {                            
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(file.getAbsolutePath());
                            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            videoData.setVideoDuration(timeConversion(Long.parseLong(duration)));                     
                        }else{
                            videoData.setVideoDuration(Integer.toString(mCount));                   
                        }
                        videoData.setVideoDate(date);
                        videoList.add(videoData);

                        try {
                            videoData.saveToFile(videoList);

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            if (mOnVideoTaskListener != null) {
                                mOnVideoTaskListener.onFailed();
                            }
                        }
                        Log.i("LOG", "ADD " + videoData.getVideoTitle() + " " + videoData.getVideoThumbnail());
                    }
                }
            }
        }
        Log.d("LOG", videoList.size() + " DONE");
    }

}

