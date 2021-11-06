package com.file.evolution.application;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.util.ArrayList;

import com.file.evolution.R;
import com.file.evolution.engine.app.folders.VideoFolder;
import com.file.evolution.engine.widget.SplashScreen;

public class ApplicationStarterActivity extends AppCompatActivity {

    public static String TAG = ApplicationStarterActivity.class.getSimpleName();
    private SplashScreen mSplashScreen;
    private View mSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);  
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mSplash = findViewById(R.id.splash);
        //mSplash.setVisibility(View.VISIBLE);

        mSplashScreen = (SplashScreen)findViewById(R.id.icon);
        //mSplashScreen.setVisibility(View.GONE);

        /**** START APP ****/
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean isFirstStart = SP.getBoolean("firstStart", true);
        if (isFirstStart) {
            SharedPreferences.Editor e = SP.edit();
            e.putBoolean("firstStart", false);
            e.apply();
            VideoFolder.initVideoBox(ApplicationStarterActivity.this);   
            setSplashScreen();               
        } else {
            setTransition();
        }
    }

    public void setTransition() {
        int SPLASH_TIME_OUT = 5000;
        new Handler().postDelayed(new Runnable() {

                /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */

                @Override
                public void run() {
                    Intent mIntent = new Intent(ApplicationStarterActivity.this, ApplicationFolderActivity.class);
                    startActivity(mIntent);    
                    ApplicationStarterActivity.this.finish();
                }
            }, SPLASH_TIME_OUT); 
    }

    public void setSplashScreen() {
        int SPLASH_TIME_OUT = 5000;
        new Handler().postDelayed(new Runnable() {

                /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start SplashScreen
                    mSplash.setVisibility(View.GONE);
                    // hide actionbar
                    getSupportActionBar().hide();
                    // hide navigation bar
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    mSplashScreen.setVisibility(View.VISIBLE);
                    mSplashScreen.start();
                    mSplashScreen.setOnSplashScreenListener(new SplashScreen.OnSplashScreenListener(){
                            @Override
                            public void OnStartActivity() {
                                Intent mIntent = new Intent(ApplicationStarterActivity.this, ApplicationFolderActivity.class);                          
                                setTransition();
                                mSplashScreen.createShortCut(ApplicationStarterActivity.this, mIntent);   
                                ApplicationStarterActivity.this.finish();
                            }
                        });

                }
            }, SPLASH_TIME_OUT); 
    }
}
