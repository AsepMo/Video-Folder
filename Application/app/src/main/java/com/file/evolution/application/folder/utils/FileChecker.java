package com.file.evolution.application.folder.utils;

import android.util.Log;

import java.io.File;
import com.file.evolution.engine.app.models.VideoData;

public final class FileChecker {

    private static final String TAG = FileChecker.class.getSimpleName();

    /**
     * Contains all possible places to check binaries
     */
    private static final String[] pathList;
    private static final String fileInitialisePath = VideoData.FOLDER + "/video_data_initialise.json";
    private static final String fileSavePath = VideoData.FOLDER + VideoData.FILENAME;
    
    /**
     * The binary which grants the root privileges
     */
    private static final String KEY_VIDEO_DATA = fileSavePath;
    private static final String KEY_VIDEO_DATA_INITIALISE = fileInitialisePath;
    
    static {
        pathList = new String[]{
                VideoData.FOLDER + "/"           
        };
    }

    public static boolean isDataExist() {
        return doesFileExists(KEY_VIDEO_DATA);
    }
    
    public static boolean isDataInitialiseExist() {
        return doesFileExists(KEY_VIDEO_DATA_INITIALISE);
    }
    
    /**
     * Checks the all path until it finds it and return immediately.
     *
     * @param value must be only the binary name
     * @return if the value is found in any provided path
     */
    private static boolean doesFileExists(String value) {
        boolean result = false;
        for (String path : pathList) {
            File file = new File(path + "/" + value);
            result = file.exists();
            if (result) {
                Log.d(TAG, path + " contains savedata binary");
                break;
            }
        }
        return result;
    }
}
