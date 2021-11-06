package com.file.evolution.application;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import com.ls.directoryselector.DirectoryDialog;
import com.singhajit.sherlock.core.Sherlock;
import com.singhajit.sherlock.core.SherlockNotInitializedException;

import com.file.evolution.R;
import com.file.evolution.application.folder.FileSelector;
import com.file.evolution.application.folder.VideoFolderFragment;
import com.file.evolution.application.folder.dialogs.VideoInfoDialogFragment;
import com.file.evolution.application.folder.dialogs.FileInfoDialogFragment;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.application.folder.utils.FileChecker;
import com.file.evolution.application.settings.Settings;
import com.file.evolution.application.settings.AppSettings;
import com.file.evolution.engine.app.folders.utils.AnimationUtils;
import com.file.evolution.engine.app.folders.utils.SharedPref;
import com.file.evolution.engine.app.preview.IconPreview;
import com.file.evolution.application.folder.VideoInfo;

public class ApplicationFolderActivity extends AppCompatActivity implements DirectoryDialog.Listener {

    public static final String EXTRA_SHORTCUT = "shortcut_path";
    private static AppSettings settings;
    private Handler mHandler = new Handler();
    private Runnable mRunner = new Runnable(){
        @Override
        public void run(){
            showFragment(VideoFolderFragment.currentFolder(settings.getStorePath()));       
        }
    };
    public VideoFolderFragment getCurrentBrowserFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Settings.updatePreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);  
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        settings = AppSettings.getSettings(this);
        try {
            Sherlock.getInstance().getAllCrashes();
            Sherlock.getInstance().getAppInfoProvider();
        } catch (SherlockNotInitializedException e) {
            e.printStackTrace();
        }
        boolean isFile = FileChecker.isDataExist();
        if(!isFile){
            Toast.makeText(ApplicationFolderActivity.this, "File Exist", Toast.LENGTH_SHORT).show();          
        }else{
            Toast.makeText(ApplicationFolderActivity.this, "File No Exist", Toast.LENGTH_SHORT).show();        
        }
        // start IconPreview class to get thumbnails if BrowserListAdapter
        // request them
        new IconPreview(this); 
        mHandler.postDelayed(mRunner, 1200);     
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO: Implement this method

        menu.add("Memory")
            .setIcon(R.drawable.ic_folder_memory)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    /*Intent intent = new Intent(getApplication(), ApplicationFileActivity.class);
                    intent.putExtra("path" , "");
                    intent.putExtra("action", ApplicationFileActivity.Actions.SelectFolder);
                    startActivityForResult(intent, ApplicationFileActivity.SELECT_FOLDER_CODE);*/
                  
                    String path = VideoFolderUtils.getExternalStorageDirectory(ApplicationFolderActivity.this, true);                 
                    if (path != null) {
                        settings.setStorePath(path);
                        settings.saveDeferred();      
                        mHandler.postDelayed(mRunner, 1200);          
                    } else {
                        Toast.makeText(ApplicationFolderActivity.this, "can't detect the extend sdcard", Toast.LENGTH_SHORT).show();
                    }
                    mHandler.postDelayed(mRunner, 1200);  
                    return true;
                }
            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add("SdCard")
            .setIcon(R.drawable.ic_folder_sdcard)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String path = VideoFolderUtils.getInternalStorageDirectory(ApplicationFolderActivity.this, true);
                    if (path != null) {
                        settings.setStorePath(path);
                        settings.saveDeferred();      
                        mHandler.postDelayed(mRunner, 1200);          
                    } else {
                        Toast.makeText(ApplicationFolderActivity.this, "can't detect the extend sdcard", Toast.LENGTH_SHORT).show();
                    }
                    mHandler.postDelayed(mRunner, 1200);  
                    return true;
                }
            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add("Settings")
            .setIcon(R.drawable.ic_settings)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    ApplicationSettingActivity.startThisActivity(ApplicationFolderActivity.this);
                    return true;
                }
            }).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ApplicationFileActivity.SELECT_FOLDER_CODE) {
            String path = data.getStringExtra("path");
            if (TextUtils.isEmpty(path)) {
                String lastNavigatedPath = settings.getStorePath();
                File file = new File(lastNavigatedPath);
                if (!file.exists()) {
                    settings.setStorePath(file.getPath());
                    settings.saveDeferred();
                    file = new File(settings.getStorePath());
                }
                
                mHandler.postDelayed(mRunner, 1200);               
            } else {
                settings.setStorePath(path);
                settings.saveDeferred();
                mHandler.postDelayed(mRunner, 1200);          
            }               
            Toast.makeText(ApplicationFolderActivity.this, path, Toast.LENGTH_SHORT).show();
        }
    }

    public void getImageInfo(final String file) {

        final FileInfoDialogFragment fileInfoDialogFragment = FileInfoDialogFragment.newInstance(file);
        fileInfoDialogFragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ApplicationFolderActivity.this, "Edit Video Ini " + file, Toast.LENGTH_SHORT).show();

                    fileInfoDialogFragment.dismiss();
                }
            });
        if (getSupportFragmentManager().findFragmentByTag(FileInfoDialogFragment.TAG) == null) {
            fileInfoDialogFragment.show(getSupportFragmentManager(), FileInfoDialogFragment.TAG);
        }

    }
    
    public void getVideoInfo(final String file) {
        
        final VideoInfoDialogFragment videoInfoDialogFragment = VideoInfoDialogFragment.newInstance(file);
        videoInfoDialogFragment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(ApplicationFolderActivity.this, "Edit Video Ini " + file, Toast.LENGTH_SHORT).show();
                    
                    videoInfoDialogFragment.dismiss();
                }
            });
        if (getSupportFragmentManager().findFragmentByTag(VideoInfoDialogFragment.TAG) == null) {
            videoInfoDialogFragment.show(getSupportFragmentManager(), VideoInfoDialogFragment.TAG);
        }
    }
    
    public void onArchive() {
        new FileSelector(ApplicationFolderActivity.this, new String[]{FileSelector.ZIP})
            .selectFile(new FileSelector.OnSelectListener() {
                @Override
                public void onSelect(final String path) {

                    new FileSelector.ZipTask(path, new Runnable() {
                            @Override
                            public void run() {
                                try {   File file = new File(path);
                                    Toast.makeText(ApplicationFolderActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).execute();   
                }
            });
	}

    public static void setDefaultFolder(Activity c) {
        DialogFragment dialog = DirectoryDialog.newInstance(settings.getStorePath());
        dialog.show(c.getFragmentManager(), "directoryDialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        settings.load();
        mHandler.postDelayed(mRunner, 1200);    
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(sharedPrefsChangeListener);
    }


    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunner);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(sharedPrefsChangeListener);     
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunner);
    }
    
    //region DirectoryDialog.Listener interface
    @Override
    public void onDirectorySelected(File dir) {
        settings.setStorePath(dir.getPath());
        settings.saveDeferred();
        mHandler.postDelayed(mRunner, 1200);    
    }

    @Override
    public void onCancelled() {
    }
    //endregion

    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPrefsChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            settings.load();
        }
	};

    @Override
    public void onTrimMemory(int level) {
        IconPreview.clearCache();
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.content_frame, fragment)
            .commit();
    }
}
