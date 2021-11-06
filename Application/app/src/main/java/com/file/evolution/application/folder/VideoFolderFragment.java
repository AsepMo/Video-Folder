package com.file.evolution.application.folder;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.annotation.Nullable;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.FileObserver;
import android.util.Log;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.ref.WeakReference;

import org.apache.commons.io.FilenameUtils;

import com.file.evolution.R;
import com.file.evolution.application.ApplicationFolderActivity;
import com.file.evolution.application.folder.adapters.VideoFolderAdapter;
import com.file.evolution.application.folder.tasks.VideoFolderTask;
import com.file.evolution.application.folder.listeners.OnClickManager;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.application.settings.AppSettings;
import com.file.evolution.engine.app.models.ActionItem;
import com.file.evolution.engine.app.models.VideoData;
import com.file.evolution.engine.app.folders.utils.SharedPref;
import com.file.evolution.engine.app.folders.fileobserver.FileObserverCache;
import com.file.evolution.engine.app.folders.fileobserver.MultiFileObserver;
import com.file.evolution.engine.app.preview.MimeTypes;

public class VideoFolderFragment extends Fragment implements MultiFileObserver.OnEventListener{

    private static final String EXTRA_PATH = "video_path";
    public static final int PERMISSION_READ = 0;

    private Activity mActivity;
    private FragmentManager fm;
    private MultiFileObserver mObserver;
    private FileObserverCache mObserverCache;
    private Runnable mLastRunnable;
    private static Handler sHandler;
    
    private View rootView;

    private VideoFolder mVideoFolder;

    private static final int ID_FILE_APK = 1;
    private static final int ID_FILE_IMAGE = 2;
    private static final int ID_FILE_MUSIC = 3;
    private static final int ID_FILE_VIDEO = 4;

    private ActionMenuFolder mMenuAction;
    private String currentFolder;
    private String currentFile;
    private AppSettings settings;

    public static VideoFolderFragment currentFolder(String path) {
        VideoFolderFragment fragment = new VideoFolderFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_PATH, path);
        fragment.setArguments(args);
        return fragment;
    }

    private Handler mHandler = new Handler();
    private Runnable mRunner = new Runnable(){
        @Override
        public void run() {
            onRefreshFolder();       
        }
    };

    @Override
    public void onActivityCreated(Bundle state) {
        super.onActivityCreated(state);
        mActivity = getActivity();
        Intent intent = mActivity.getIntent();
        mObserverCache = FileObserverCache.getInstance();
        if (sHandler == null) {
            sHandler = new Handler(mActivity.getMainLooper());
        }
        initDirectory(state, intent);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_video_folder, container, false);
        settings = AppSettings.getSettings(getActivity());

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        currentFolder = getArguments().getString(EXTRA_PATH);
        currentFile = SharedPref.getWorkingFile(getActivity());

        mMenuAction = (ActionMenuFolder)view.findViewById(R.id.navigation_menu);
        mVideoFolder = (VideoFolder) view.findViewById(R.id.videoFolder);


        ActionItem fileApkItem      = new ActionItem(ID_FILE_APK, "Apk", getResources().getDrawable(R.drawable.ic_file_apk));
        ActionItem fileImageItem   = new ActionItem(ID_FILE_IMAGE, "Image", getResources().getDrawable(R.drawable.ic_file_image));
        ActionItem fileMp3Item   = new ActionItem(ID_FILE_MUSIC, "Mp3", getResources().getDrawable(R.drawable.ic_file_music));
        ActionItem fileVideoItem   = new ActionItem(ID_FILE_VIDEO, "Mp4", getResources().getDrawable(R.drawable.ic_file_video));

        //use setSticky(true) to disable QuickAction dialog being dismissed after an item is clicked
        fileVideoItem.setSticky(true);

        mMenuAction.addActionItem(fileApkItem);
        mMenuAction.addActionItem(fileImageItem);
        mMenuAction.addActionItem(fileMp3Item);
        mMenuAction.addActionItem(fileVideoItem);
        mMenuAction.setOnActionItemClickListener(new ActionMenuFolder.OnActionItemClickListener() {
                @Override
                public void onItemClick(ActionMenuFolder quickAction, int pos, int actionId) {
                    ActionItem actionItem = quickAction.getActionItem(pos);
                    if (actionId == ID_FILE_APK) {  
                        currentFile = VideoFolderUtils.APK;      
                        Toast.makeText(getActivity(), "File Apk", Toast.LENGTH_SHORT).show();
                    } else if (actionId == ID_FILE_IMAGE) {
                        currentFile = VideoFolderUtils.JPG;      
                        Toast.makeText(getActivity(), "File Image", Toast.LENGTH_SHORT).show();
                    } else if (actionId == ID_FILE_MUSIC) {
                        currentFile = VideoFolderUtils.MP3;      
                        Toast.makeText(getActivity(), "File Audio", Toast.LENGTH_SHORT).show();
                    } else if (actionId == ID_FILE_VIDEO) {
                        currentFile = VideoFolderUtils.MP4;            
                        Toast.makeText(getActivity(), "File Video", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
                    }
                    mHandler.postDelayed(mRunner, 1200);            
                    SharedPref.setWorkingFile(getActivity(), currentFile);        
                }
            });
        mMenuAction.setOnFolderSettingListener(new ActionMenuFolder.OnFolderSettingClickListener(){
                @Override
                public void onFolderSettingClick(View view) {
                    ApplicationFolderActivity.setDefaultFolder(getActivity());
                    Toast.makeText(getActivity(), "Folder", Toast.LENGTH_SHORT).show();             
                }
            });
        //mMenuAction.setDirectoryButtons(currentFolder);
        mMenuAction.setOnNavigationListener(new ActionMenuFolder.OnNavigateListener(){
                @Override
                public void onNavigate(String path) {
                    currentFolder = path;
                    if (currentFolder.isEmpty() || currentFolder.equals("/")) {
                        mVideoFolder.setMessage("Warning..!", R.drawable.warning_icon, "Folder Ini Tidak Bisa DiBuka.Coba Folder Lain");   
                    } else if (currentFolder.isEmpty() || currentFolder.equals("/storage")) {
                        mVideoFolder.setMessage("Warning..!", R.drawable.warning_icon, "Folder Ini Tidak Bisa DiBuka.Coba Folder Lain");
                    } else if (currentFolder.isEmpty() || currentFolder.equals("/storage/emulated")) {
                        mVideoFolder.setMessage("Warning..!", R.drawable.warning_icon, "Folder Ini Tidak Bisa DiBuka.Coba Folder Lain");                     
                    } else if (currentFolder.isEmpty() || currentFolder.equals("/storage/extSdCard")) {
                        mVideoFolder.setMessage("Warning..!", R.drawable.warning_icon, "Folder Ini Tidak Bisa DiBuka.Coba Folder Lain");                       
                    } else {
                        UpdateList(currentFolder, new String[]{currentFile});                                                     
                    }
                    //Toast.makeText(getActivity(), path, Toast.LENGTH_SHORT).show();             
                }
            });

        /*if (!TextUtils.isEmpty(currentFolder)) {
         String lastNavigatedPath = settings.getStorePath();
         String file = SharedPref.getWorkingFile(getActivity());
         if (file.endsWith(VideoFolderUtils.JPG)) {
         UpdateList(settings.getStorePath(), new String[]{SharedPref.getWorkingFile(getActivity()), VideoFolderUtils.JPEG, VideoFolderUtils.PNG});                    
         } else
         UpdateList(lastNavigatedPath, new String[]{currentFile});

         } else {
         if (currentFile.endsWith(VideoFolderUtils.JPG)) {
         UpdateList(currentFolder, new String[]{SharedPref.getWorkingFile(getActivity()), VideoFolderUtils.JPEG, VideoFolderUtils.PNG});                    
         } else
         UpdateList(currentFolder, new String[]{currentFile});

         SharedPref.setWorkingFile(getActivity(), currentFile);                   
         }*/

    }

    private void initDirectory(Bundle savedInstanceState, Intent intent) {
        String defaultdir;

        if (savedInstanceState != null) {
            // get directory when you rotate your phone
            defaultdir = savedInstanceState.getString("location");
        } else {
            try {
                File dir = new File(intent.getStringExtra(ApplicationFolderActivity.EXTRA_SHORTCUT));

                if (dir.exists() && dir.isDirectory()) {
                    defaultdir = dir.getAbsolutePath();
                } else {
                    if (dir.exists() && dir.isFile())
                        listItemAction(dir);
                    // you need to call it when shortcut-dir not exists
                    defaultdir = settings.getStorePath();
                }
            } catch (Exception e) {
                defaultdir = settings.getStorePath();
            }
        }

        File dir = new File(defaultdir);

        if (dir.exists() && dir.isDirectory())
            navigateTo(dir.getAbsolutePath());
    }


    public void navigateTo(String path) {
        currentFolder = path;
        if (currentFile.endsWith(VideoFolderUtils.JPG)) {
            UpdateList(path, new String[]{SharedPref.getWorkingFile(getActivity()), VideoFolderUtils.JPEG, VideoFolderUtils.PNG});                    
        } else
            UpdateList(path, new String[]{currentFile});
        if (mObserver != null) {
            mObserver.stopWatching();
            mObserver.removeOnEventListener(this);
        }

        
        mObserver = mObserverCache.getOrCreate(path);

        // add listener for FileObserver and start watching
        if (mObserver.listeners.isEmpty())
            mObserver.addOnEventListener(this);
        mObserver.startWatching();
        
        SharedPref.setWorkingFile(getActivity(), currentFile);                        
    }
    
    @Override
    public void onEvent(int event, String path) {
        // this will automatically update the directory when an action like this
        // will be performed
        switch (event & FileObserver.ALL_EVENTS) {
            case FileObserver.CREATE:
            case FileObserver.CLOSE_WRITE:
            case FileObserver.MOVE_SELF:
            case FileObserver.MOVED_TO:
            case FileObserver.MOVED_FROM:
            case FileObserver.ATTRIB:
            case FileObserver.DELETE:
            case FileObserver.DELETE_SELF:
                sHandler.removeCallbacks(mLastRunnable);
                sHandler.post(mLastRunnable = new NavigateRunnable((ApplicationFolderActivity) getActivity(), path));
                break;
        }
    }

    // this will be overwritten in picker fragment
    public void listItemAction(File file) {

    }
    
    private static final class NavigateRunnable implements Runnable {
        private final WeakReference<ApplicationFolderActivity> abActivityWeakRef;
        private final String target;

        NavigateRunnable(final ApplicationFolderActivity abActivity, final String path) {
            this.abActivityWeakRef = new WeakReference<>(abActivity);
            this.target = path;
        }

        @Override
        public void run() {
            // TODO: Ensure WeakReference approach is free of both bugs and leaks
            //BrowserTabsAdapter.getCurrentBrowserFragment().navigateTo(target);
            ApplicationFolderActivity abActivity = abActivityWeakRef.get();
            if (abActivity != null) {
                abActivity.getCurrentBrowserFragment.navigateTo(target);
            } else {
                Log.w(this.getClass().getName(), "NavigateRunnable: activity weakref returned null, can't navigate");
            }
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("location", currentFolder);
    }

    public void onRefreshFolder() {
        String file = SharedPref.getWorkingFile(getActivity());
        if (file.endsWith(VideoFolderUtils.JPG)) {
            UpdateList(currentFolder, new String[]{SharedPref.getWorkingFile(getActivity()), VideoFolderUtils.JPEG, VideoFolderUtils.PNG});       
        } else
            UpdateList(currentFolder, new String[]{SharedPref.getWorkingFile(getActivity())});       
    }

    @Override
    public void onStart() {
        super.onResume();
        mMenuAction.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        settings.load();
        mMenuAction.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mObserver.stopWatching();
        mHandler.removeCallbacks(mRunner);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunner);
        // mMenuAction.onDestroy();
    }

    private void UpdateList(String path, String[] extensions) {
        setVideoFolderTask(path, extensions);
    }

    public void setVideoFolderTask(final String folder, String[] extensions) {
        mVideoFolder.setVideoScanning(mMenuAction, folder, extensions);
    }

    //runtime storage permission
    public boolean checkPermission() {
        int READ_EXTERNAL_PERMISSION = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if ((READ_EXTERNAL_PERMISSION != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ: {
                    if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            Toast.makeText(getActivity(), "Please allow storage permission", Toast.LENGTH_LONG).show();
                        } else {
                            setVideoFolderTask(currentFolder, new String[]{currentFile});
                        }
                    }
                }
        }
    }

}


