package com.file.evolution.application;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.file.evolution.R;
import com.file.evolution.EvolutionApplication;
import com.file.evolution.application.folder.adapters.VideoSelectorAdapter;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.engine.app.folders.utils.SharedPref;
import com.file.evolution.application.settings.AppSettings;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class ApplicationFileActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private String currentFolder;
    private ListView listView;
    private boolean wantAFile, wantAFolder;
    private static AppSettings settings;
    // The android SD card root path
    public static final String SD_CARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final int SELECT_FILE_CODE = 121;
    public static final int SELECT_FOLDER_CODE = 122;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_file);
        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        settings = AppSettings.getSettings(this);

        final Actions action = (Actions) getIntent().getExtras().getSerializable("action");
        wantAFile = action == Actions.SelectFile;
        wantAFolder = action == Actions.SelectFolder;
        
        this.listView = (ListView) findViewById(android.R.id.list);
        this.listView.setOnItemClickListener(this);
        this.listView.setTextFilterEnabled(true);
        
        String path = getIntent().getExtras().getString("path");       
        if (TextUtils.isEmpty(path)) {
            String lastNavigatedPath = settings.getStorePath();

            File file = new File(lastNavigatedPath);
            if (!file.exists()) {
                settings.setStorePath(SharedPref.SD_CARD_ROOT);
                settings.saveDeferred();    
                file = new File(SharedPref.SD_CARD_ROOT);
            }

            new UpdateList().execute(file.getAbsolutePath());
            
        } else {
            new UpdateList().execute(path);
        }
    }

    void returnData(String path) {
        final Intent returnIntent = new Intent();
        returnIntent.putExtra("path", path);
        setResult(RESULT_OK, returnIntent);
        // finish the activity
        finish();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String name = ((TextView) view.findViewById(android.R.id.title)).getText().toString();
        if (name.equals("..")) {
            navigateTo();
            return;
        } else if (name.equals(getString(R.string.folder_home))) {
            new UpdateList().execute(SharedPref.getWorkingFolder(this));
            return;
        }

        final File selectedFile = new File(currentFolder, name);

        if (selectedFile.isFile() && wantAFile) {
            returnData(selectedFile.getAbsolutePath());
        } else if (selectedFile.isDirectory()) {
            new UpdateList().execute(selectedFile.getAbsolutePath());
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFolder.isEmpty() || currentFolder.equals("/")) {
            finish();
        } else {
            File file = new File(currentFolder);
            String parentFolder = file.getParent();
            new UpdateList().execute(parentFolder);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_file, menu);
        menu.findItem(R.id.im_button).setTitle(getString(wantAFolder ? R.string.folder_select: android.R.string.cancel));
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.im_button) {
            if (wantAFolder) {
                returnData(currentFolder);
            } else if (wantAFile) {
                returnData("");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    void navigateTo() {
        if (currentFolder.equals("/")) {
            new UpdateList().execute(SD_CARD_ROOT);
        } else {
            File tempFile = new File(currentFolder);
            if (tempFile.isFile()) {
                tempFile = tempFile.getParentFile().getParentFile();
            } else {
                tempFile = tempFile.getParentFile();
            }
            new UpdateList().execute(tempFile.getAbsolutePath());
        }
    }

    private class UpdateList extends AsyncTask<String, Void, LinkedList<VideoSelectorAdapter.FileDetail>> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected LinkedList<VideoSelectorAdapter.FileDetail> doInBackground(final String... params) {
            try {

                final String path = params[0];
                if (TextUtils.isEmpty(path)) {
                    return null;
                }

                File tempFile = new File(path);
                if (tempFile.isFile()) {
                    tempFile = tempFile.getParentFile();
                }

                final File[] files = tempFile.listFiles(VideoFolderUtils.DEFAULT_FILE_FILTER);
                Arrays.sort(files, EvolutionApplication.getAppPreferences(ApplicationFileActivity.this).getFileSortingComparator());

                final LinkedList<VideoSelectorAdapter.FileDetail> fileDetails = new LinkedList<VideoSelectorAdapter.FileDetail>();
                final LinkedList<VideoSelectorAdapter.FileDetail> folderDetails = new LinkedList<VideoSelectorAdapter.FileDetail>();
                final AbstractMap<String, File> tempList = new HashMap<String, File>();
                currentFolder = tempFile.getAbsolutePath();

                if (files != null) {
                    for (final File f : files) {
                        if (f.isHidden()) {
                            continue;
                        } else if (f.isDirectory() && f.canRead()) {
                            String[] fileList = f.list();
                            if (fileList != null) {
                                if (fileList.length > 0) {
                                    SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                                    String date = format.format(f.lastModified());
                                    fileDetails.add(new VideoSelectorAdapter.FileDetail(f.getName(), f.getAbsolutePath(), "Total : " + fileList.length, date, true));                                    
                                }
                            }
                        } else if (f.isFile()) {
                            final long fileSize = f.length();
                            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                            String date = format.format(f.lastModified());
                            fileDetails.add(new VideoSelectorAdapter.FileDetail(f.getName(), f.getAbsolutePath(), FileUtils.byteCountToDisplaySize(fileSize), date, false));
                        }
                        tempList.put(f.getName(), f);
                    }
                }

                folderDetails.addAll(fileDetails);
                return folderDetails;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(final LinkedList<VideoSelectorAdapter.FileDetail> names) {
            boolean isRoot = currentFolder.equals("/");
            if (names != null) {
                listView.setAdapter(new VideoSelectorAdapter(getBaseContext(), names, isRoot));
            }
            super.onPostExecute(names);
        }

    }
    
    public enum Actions {
        SelectFile, SelectFolder
        }
}
