package com.file.evolution.application.settings;

import android.app.Activity;
import android.app.Application;
import android.preference.PreferenceManager;

import com.file.evolution.EvolutionApplication;

public class AppSettings extends Settings {

	public static AppSettings getSettings(Activity activity) {
		return getSettings(activity.getApplication());
	}

	public static AppSettings getSettings(Application application) {
		return ((EvolutionApplication) application).settings;
	}

	private final EvolutionApplication application;

	public AppSettings(EvolutionApplication application) {
		this.application = application;
	}
    
    public void updatePreferences(Activity c) {
        updatePreferences(c);
    }
    
	public void load() {
		load(PreferenceManager.getDefaultSharedPreferences(application));
	}

	public void save() {
		save(PreferenceManager.getDefaultSharedPreferences(application));
	}

	public void saveDeferred() {
		saveDeferred(PreferenceManager.getDefaultSharedPreferences(application));
	}
}
