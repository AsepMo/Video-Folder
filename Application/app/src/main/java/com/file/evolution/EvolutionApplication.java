package com.file.evolution;


import android.app.Application;
import android.content.Context;
import java.io.File;

import com.singhajit.sherlock.core.Sherlock;
import com.singhajit.sherlock.core.investigation.AppInfo;
import com.singhajit.sherlock.core.investigation.AppInfoProvider;
import com.singhajit.sherlock.core.SherlockNotInitializedException;
import com.singhajit.sherlock.util.AppInfoUtil;

import com.file.evolution.application.settings.AppSettings;
import com.file.evolution.engine.app.folders.utils.SharedPref;

public class EvolutionApplication extends Application{
    private static SharedPref appPreferences = null;
    public final AppSettings settings = new AppSettings(this);
    private static Context mContext;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        settings.load();
        mContext = this;
        if (!settings.isInitialized()) {
            settings.setInitialized(true);
            File file = new File(SharedPref.SD_CARD_ROOT);
            if (file != null) settings.setStorePath(file.getPath());
            settings.save();
		}
        
       try {
            
            Sherlock.init(this); //Initializing Sherlock
            Sherlock.setAppInfoProvider(new AppInfoProvider() {
                    @Override
                    public AppInfo getAppInfo() {
                        return new AppInfo.Builder()
                            .with("Version", AppInfoUtil.getAppVersion(getApplicationContext())) //You can get the actual version using "AppInfoUtil.getAppVersion(context)"
                            .with("BuildNumber", "1")
                            .build();
                    }
                });
           
        } catch (SherlockNotInitializedException e) {
            e.printStackTrace();
        } 
    }

    public static Context getContext(){
        return mContext;
    }
    
    public static SharedPref getAppPreferences(Context c)
    {
        if (appPreferences == null)
            appPreferences = SharedPref.loadPreferences(c);

        return appPreferences;
	}
}
