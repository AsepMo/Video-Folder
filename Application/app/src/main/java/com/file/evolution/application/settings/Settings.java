package com.file.evolution.application.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.stericson.RootTools.RootTools;

public class Settings {

	private static final String INITIALIZED_KEY = "initialized";
	private static final String STORE_PATH_KEY = "store_path";

	private boolean initialized;
	private String storePath;
    private static SharedPreferences mPrefs;
    public static void updatePreferences(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        rootAccess();
    }
    
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
    
    public static boolean showThumbnail() {
        return mPrefs.getBoolean("showpreview", true);
    }

    public static boolean showHiddenFiles() {
        return mPrefs.getBoolean("displayhiddenfiles", true);
    }

    public static boolean rootAccess() {
        return mPrefs.getBoolean("enablerootaccess", false) && RootTools.isAccessGiven();
    }
    
	public String getStorePath() {
		return storePath;
	}

	public void setStorePath(String storePath) {
		this.storePath = storePath;
	}

	public void load(SharedPreferences prefs) {
		initialized = prefs.getBoolean(INITIALIZED_KEY, false);
		storePath = prefs.getString(STORE_PATH_KEY, null);
	}

	public void save(SharedPreferences prefs) {
		SharedPreferences.Editor editor = prefs.edit();
		save(editor);
		editor.commit();
	}

	public void saveDeferred(SharedPreferences prefs) {
		SharedPreferences.Editor editor = prefs.edit();
		save(editor);
		editor.apply();
	}

	public void save(SharedPreferences.Editor editor) {
		editor.putBoolean(INITIALIZED_KEY, initialized);
		editor.putString(STORE_PATH_KEY, storePath);
	}
}
