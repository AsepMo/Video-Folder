package com.file.evolution.application.folder.listeners;

import android.support.v7.content.res.AppCompatResources;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.ArrayList;

import com.file.evolution.R;
import com.file.evolution.application.ApplicationFolderActivity;
import com.file.evolution.application.folder.ActionMenuItem;
import com.file.evolution.application.folder.dialogs.VideoInfoDialogFragment;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.engine.app.models.VideoData;
import com.file.evolution.engine.app.models.ActionItem;
import com.file.evolution.engine.app.preview.MimeTypes;

public class OnClickManager {

    private static volatile OnClickManager Instance = null;
    private static final int ID_INFO = 1;
    private static final int ID_PLAYER = 2;
    private static final int ID_SHORTCUT = 3;
    private static final int ID_RENAME = 4;
    private static final int ID_DELETE = 5;
    private static final int ID_COPY = 6;
    private static final int ID_MOVE = 7;
    private static final int ID_SHARE = 8;

    public static OnClickManager getInstance() {
        OnClickManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (OnClickManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new OnClickManager();
                }
            }
        }
        return localInstance;
    }

    public void onClickListener(Context mContext, ActionMenuItem mMenuAction, VideoData video) {
        final String ext = FilenameUtils.getExtension(video.getVideoPath()); 
        video.initialise(video);
        if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(ext)) {
            OnClickManager.getInstance().onVideoMenuClick(mContext, mMenuAction, video);
        } else {
            OnClickManager.getInstance().onFileMenuClick(mContext, mMenuAction, video);
        }
    }

    public void onVideoMenuClick(final Context mContext, final ActionMenuItem mMenuAction, final VideoData video) {
        final String ext = FilenameUtils.getExtension(video.getVideoPath());
        final ApplicationFolderActivity act = (ApplicationFolderActivity)mContext;
        
        ActionItem infoItem      = new ActionItem(ID_INFO, "Info", AppCompatResources.getDrawable(mContext, R.drawable.ic_video_info));
        ActionItem playerItem   = new ActionItem(ID_PLAYER, "Player", AppCompatResources.getDrawable(mContext, R.drawable.ic_video_player));
        ActionItem shortcutItem   = new ActionItem(ID_SHORTCUT, "Shortcut", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_short));      
        ActionItem renameItem   = new ActionItem(ID_RENAME, "Rename", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_rename));
        ActionItem deleteItem   = new ActionItem(ID_DELETE, "Delete", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_delete));
        ActionItem copyItem   = new ActionItem(ID_COPY, "Copy", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_copy));
        ActionItem moveItem   = new ActionItem(ID_MOVE, "Move", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_move));
        ActionItem shareItem   = new ActionItem(ID_SHARE, "Share", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_share));

        mMenuAction.addActionItem(infoItem);
        mMenuAction.addActionItem(playerItem);
        mMenuAction.addActionItem(shortcutItem);
        mMenuAction.addActionItem(renameItem);
        mMenuAction.addActionItem(deleteItem);
        mMenuAction.addActionItem(copyItem);
        mMenuAction.addActionItem(moveItem);
        mMenuAction.addActionItem(shareItem);   
        mMenuAction.setOnActionItemClickListener(new ActionMenuItem.OnActionItemClickListener() {
                @Override
                public void onItemClick(ActionMenuItem quickAction, int pos, int actionId) {
                    ActionItem actionItem = quickAction.getActionItem(pos);
                    File file = new File(video.getPath());
                    if (actionId == ID_INFO) {
                       act.getImageInfo(file.getAbsolutePath());
                    } else if (actionId == ID_PLAYER) {
                        VideoFolderUtils.openFile(mContext, file);
                    } else if (actionId == ID_RENAME) {

                    } else if (actionId == ID_SHORTCUT) {
                       VideoFolderUtils.createShortcut(act, video.getVideoPath());
                    } else if (actionId == ID_DELETE) {

                    } else if (actionId == ID_COPY) {

                    } else if (actionId == ID_MOVE) {

                    } else if (actionId == ID_SHARE) {
                        VideoFolderUtils.shareFile(act, video.getVideoPath());
                    } 
                    //MrToast.getInstance().createInstance(mContext).sendShortMessage(actionItem.getTitle());
                    // Toast.makeText(MainActivity.this, actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void onFileMenuClick(final Context mContext, final ActionMenuItem mMenuAction, final VideoData video) {
        final String ext = FilenameUtils.getExtension(video.getVideoPath());
        final ApplicationFolderActivity act = (ApplicationFolderActivity)mContext;
        
        ActionItem infoItem = new ActionItem(ID_INFO, "Info", AppCompatResources.getDrawable(mContext, R.drawable.ic_video_info));
        ActionItem playerItem;
        if (Arrays.asList(MimeTypes.MIME_APK).contains(ext)) {           
            playerItem  = new ActionItem(ID_PLAYER, "Install", AppCompatResources.getDrawable(mContext, R.drawable.ic_android_installer));
        } else {
            playerItem   = new ActionItem(ID_PLAYER, "Player", AppCompatResources.getDrawable(mContext, R.drawable.ic_video_player));         
        }
        ActionItem shortcutItem   = new ActionItem(ID_SHORTCUT, "Shortcut", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_short));      
        ActionItem renameItem   = new ActionItem(ID_RENAME, "Rename", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_rename));
        ActionItem deleteItem   = new ActionItem(ID_DELETE, "Delete", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_delete));
        ActionItem copyItem   = new ActionItem(ID_COPY, "Copy", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_copy));
        ActionItem moveItem   = new ActionItem(ID_MOVE, "Move", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_move));
        ActionItem shareItem   = new ActionItem(ID_SHARE, "Share", AppCompatResources.getDrawable(mContext, R.drawable.ic_file_share));

        mMenuAction.addActionItem(infoItem);
        mMenuAction.addActionItem(playerItem);
        mMenuAction.addActionItem(shortcutItem);
        mMenuAction.addActionItem(renameItem);
        mMenuAction.addActionItem(deleteItem);
        mMenuAction.addActionItem(copyItem);
        mMenuAction.addActionItem(moveItem);
        mMenuAction.addActionItem(shareItem);
        mMenuAction.setOnActionItemClickListener(new ActionMenuItem.OnActionItemClickListener() {
                @Override
                public void onItemClick(ActionMenuItem quickAction, int pos, int actionId) {
                    //ActionItem actionItem = quickAction.getActionItem(pos);
                    File file = new File(video.getPath());
                    if (actionId == ID_INFO) {
                       act.getImageInfo(file.getAbsolutePath());
                    } else if (actionId == ID_PLAYER) {
                        VideoFolderUtils.openFile(mContext, file);
                    } else if (actionId == ID_RENAME) {

                    } else if (actionId == ID_SHORTCUT) {
                        VideoFolderUtils.createShortcut(act, video.getVideoPath());
                    } else if (actionId == ID_DELETE) {

                    } else if (actionId == ID_COPY) {

                    } else if (actionId == ID_MOVE) {

                    } else if (actionId == ID_SHARE) {
                        VideoFolderUtils.shareFile(act, video.getVideoPath());
                    } 
                    //MrToast.getInstance().createInstance(mContext).sendShortMessage(actionItem.getTitle());
                    // Toast.makeText(MainActivity.this, actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
                }
            });
    }


}
