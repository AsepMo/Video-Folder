package com.file.evolution.engine.app.folders.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.Comparator;

import com.file.evolution.engine.app.folders.VideoFolder;
import com.file.evolution.application.folder.utils.VideoFolderUtils;

public final class SharedPref {

    //public static final String SD_CARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String SD_CARD_ROOT = VideoFolder.ZFOLDER;
    public static final String EXTENSION = VideoFolderUtils.MP4;
    
    private static final String 
    NAME = "FileExplorerPreferences",

    PREF_START_FOLDER = "start_folder",
    PREF_CARD_LAYOUT = "card_layout",
    PREF_SORT_BY = "sort_by";

    public static final int
    SORT_BY_NAME = 0,
    SORT_BY_TYPE = 1,
    SORT_BY_SIZE = 2;

    
    private final static int DEFAULT_SORT_BY = SORT_BY_NAME;

    File startFolder;
    String extension;
    int sortBy;

    private SharedPref() {
    }
    
    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getPrefs(context).edit();
    }
    
    public SharedPref setStartFolder(File startFolder)
    {
        this.startFolder = startFolder;
        return this;
    }

    public SharedPref setStartFile(String extension)
    {
        this.extension = extension;
        return this;
    }
    
    public SharedPref setSortBy(int sortBy)
    {
        if (sortBy < 0 || sortBy > 2)
            throw new InvalidParameterException(String.valueOf(sortBy)+" is not a valid id of sorting order");

        this.sortBy = sortBy;
        return this;
    }
    
    public static SharedPref loadPreferences(Context context)
    {
        SharedPref instance = new SharedPref();
        instance.loadFromSharedPreferences(context.getSharedPreferences(NAME, Context.MODE_PRIVATE));
        return instance;
	}
    
    public static String getWorkingFile(Context context) {
        return getPrefs(context).getString("working_file", EXTENSION);
    }

    public static String getWorkingFolder(Context context) {
        return getPrefs(context).getString("working_folder", SD_CARD_ROOT);
    }

    public static String[] getSavedPaths(Context context) {
        return getPrefs(context).getString("savedPaths", "").split(",");
    }

    public static void setWorkingFile(Context context, String value) {
        getEditor(context).putString("working_file", value).commit();
    }
    
    public static void setWorkingFolder(Context context, String value) {
        getEditor(context).putString("working_folder", value).commit();
    }

    public static void setSavedPaths(Context context, StringBuilder stringBuilder) {
        getEditor(context).putString("savedPaths", stringBuilder.toString()).commit();
    }
    
    private void loadFromSharedPreferences(SharedPreferences sharedPreferences)
    {
        String startPath = sharedPreferences.getString(PREF_START_FOLDER, null);
        if (startPath == null)
        {
            if (Environment.getExternalStorageDirectory().list() != null)
                startFolder = Environment.getExternalStorageDirectory();
            else 
                startFolder = new File("/");
        }
        else this.startFolder = new File(startPath);
        this.sortBy = sharedPreferences.getInt(PREF_SORT_BY, DEFAULT_SORT_BY);
    }

    private void saveToSharedPreferences(SharedPreferences sharedPreferences)
    {
        sharedPreferences.edit()
            .putString(PREF_START_FOLDER, startFolder.getAbsolutePath())
            .putInt(PREF_SORT_BY, sortBy)
            .apply();
    }

    public void saveChangesAsync(final Context context)
    {
        new Thread(new Runnable()
            {

                @Override
                public void run()
                {
                    saveChanges(context);

                }
            }).run();
    }

    public void saveChanges(Context context)
    {
        saveToSharedPreferences(context.getSharedPreferences(NAME, Context.MODE_PRIVATE));
    }

    
    public int getSortBy()
    {
        return sortBy;
    }

    public File getStartFolder()
    {
        if (startFolder.exists() == false)
            startFolder = new File("/");
        return startFolder;
    }

    public Comparator<File> getFileSortingComparator()
    {
        switch (sortBy)
        {
            case SORT_BY_SIZE:
                return new VideoFolderUtils.FileSizeComparator();

            case SORT_BY_TYPE:
                return new VideoFolderUtils.FileExtensionComparator();

            default:
                return new VideoFolderUtils.FileNameComparator();
        }
    }

    
}
