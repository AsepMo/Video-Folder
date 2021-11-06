package com.file.evolution.application.folder.utils;

import android.annotation.TargetApi;
import android.support.v4.app.ShareCompat;
import android.support.v4.provider.DocumentFile;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.file.evolution.R;
import com.file.evolution.application.ApplicationFolderActivity;
import com.file.evolution.engine.app.folders.utils.MediaStoreUtils;
import com.file.evolution.engine.app.preview.IconPreview;
import com.file.evolution.engine.app.root.RootCommands;
import com.file.evolution.engine.app.preview.MimeTypes;
import com.file.evolution.application.folder.tasks.TheTask;
import com.file.evolution.application.settings.Settings;
import com.file.evolution.engine.app.models.VideoData;
import com.file.evolution.application.folder.ActionMenuItem;

public class VideoFolderUtils {

    private static String TAG = VideoFolderUtils.class.getSimpleName();
    public static final String APK = ".apk", MP4 = ".mp4", MP3 = ".mp3", JPG = ".jpg", JPEG = ".jpeg", PNG = ".png", DOC = ".doc", DOCX = ".docx", XLS = ".xls", XLSX = ".xlsx", PDF = ".pdf";
    public static Drawable getApkIcon(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            try {
                return pm.getApplicationIcon(appInfo);
            } catch (OutOfMemoryError e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    public static void setApkInfo(Context c, VideoData data, ActionMenuItem mMenuAction) {
        if (data != null) {

            File apkFile = new File(data.getVideoPath());
            final String PackageDir = apkFile.getAbsolutePath();
           
            final String PackageName;
            final String PackageId;
            final String PackagePath;
            if (FilenameUtils.isExtension(PackageDir, "apk")) {
                PackageManager pm = c.getPackageManager();
                PackageInfo info = pm.getPackageArchiveInfo(PackageDir, PackageManager.GET_ACTIVITIES);
                if (info != null) {
                    ApplicationInfo appInfo = info.applicationInfo;

                    if (Build.VERSION.SDK_INT >= 8) {
                        appInfo.sourceDir = PackageDir;
                        appInfo.publicSourceDir = PackageDir;
                    }
                    PackageName = info.applicationInfo.loadLabel(c.getPackageManager()).toString();
                    PackageId = info.packageName;
                    PackagePath = appInfo.sourceDir;
                    
                    mMenuAction.setVideoThumbnail(getApkIcon(c, PackagePath));      
                    mMenuAction.setVideoTitle(PackageName);     
                    //mMenuAction.setVideoDuration(video.getVideoDuration());
                   mMenuAction.setVideoSize(data.getVideoSize());
                   mMenuAction.setVideoLastModified(data.getVideoDate());
                    
                } else {
                    PackageName = "";
                    PackageId = "";
                }
            }
        }
    }

    private static final int BUFFER = 16384;
    private static final long ONE_KB = 1024;
    private static final BigInteger KB_BI = BigInteger.valueOf(ONE_KB);
    private static final BigInteger MB_BI = KB_BI.multiply(KB_BI);
    private static final BigInteger GB_BI = KB_BI.multiply(MB_BI);
    private static final BigInteger TB_BI = KB_BI.multiply(GB_BI);

    public final static int 
    KILOBYTE = 1024,
    MEGABYTE = KILOBYTE * 1024,
    GIGABYTE = MEGABYTE * 1024,
    MAX_BYTE_SIZE = KILOBYTE / 2,
    MAX_KILOBYTE_SIZE = MEGABYTE / 2,
    MAX_MEGABYTE_SIZE = GIGABYTE / 2;

    public static final String MIME_TYPE_ANY = "*/*";

    public static final FileFilter DEFAULT_FILE_FILTER = new FileFilter()
    {

        @Override
        public boolean accept(File pathname) {
            return pathname.isHidden() == false;
        }
    };

    /**
     * Compares files by name, where directories come always first
     */
    public static class FileNameComparator implements Comparator<File> {
        protected final static int 
        FIRST = -1,
        SECOND = 1;
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() || rhs.isDirectory()) {
                if (lhs.isDirectory() == rhs.isDirectory())
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                else if (lhs.isDirectory()) return FIRST;
                else return SECOND;
            }
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }       
    }

    /**
     * Compares files by extension. 
     * Falls back to sort by name if extensions are the same or one of the objects is a Directory
     * @author Michal
     *
     */
    public static class FileExtensionComparator extends FileNameComparator {
        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() || rhs.isDirectory())
                return super.compare(lhs, rhs);

            String ext1 = getFileExtension(lhs),
                ext2 = getFileExtension(rhs);

            if (ext1.equals(ext2))
                return super.compare(lhs, rhs);
            else
                return ext1.compareToIgnoreCase(ext2);
        }
    }

    public static class FileSizeComparator extends FileNameComparator {
        private final boolean ascending = false;

        @Override
        public int compare(File lhs, File rhs) {
            if (lhs.isDirectory() || rhs.isDirectory())
                return super.compare(lhs, rhs);

            if (lhs.length() > rhs.length())
                return ascending ? SECOND : FIRST;
            else if (lhs.length() < rhs.length())
                return ascending ? FIRST : SECOND;
            else return super.compare(lhs, rhs);
        }
    }

    public static String formatFileSize(File file) {
        return formatFileSize(file.length());       
    }

    public static String formatFileSize(long size) {
        if (size < MAX_BYTE_SIZE)
            return String.format(Locale.ENGLISH, "%d bytes", size);
        else if (size < MAX_KILOBYTE_SIZE)
            return String.format(Locale.ENGLISH, "%.2f kb", (float)size / KILOBYTE);
        else if (size < MAX_MEGABYTE_SIZE)
            return String.format(Locale.ENGLISH, "%.2f mb", (float)size / MEGABYTE);
        else 
            return String.format(Locale.ENGLISH, "%.2f gb", (float)size / GIGABYTE);
    }

    public static String formatFileSize(Collection<File> files) {
        return formatFileSize(getFileSize(files));
    }

    public static long getFileSize(File... files) {
        if (files == null) return 0l;
        long size=0;
        for (File file : files) {
            if (file.isDirectory())
                size += getFileSize(file.listFiles());
            else size += file.length();
        }
        return size;
    }

    public static long getFileSize(Collection<File> files) {
        return getFileSize(files.toArray(new File[files.size()]));
    }

    /**
     *get the internal or outside sd card path
     * @param is_removale true is is outside sd card
     * */
    public static String getExternalStorageDirectory(Context mContext, boolean is_removale) {
        return Environment.getExternalStorageDirectory().getAbsolutePath();   
    }

    public static String getInternalStorageDirectory(Context mContext, boolean is_removale) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void moveToDirectory(File old_file, File target, Context c) {
        if (!old_file.renameTo(target)) {
            if (copyFile(old_file, target, c))
                deleteTarget(old_file.getAbsolutePath());
        }
    }

    // TODO: fix copy to sdcard root
    public static boolean copyFile(final File source, final File target, Context context) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            File temp_dir = target.getParentFile();

            if (source.isFile())
                inStream = new FileInputStream(source);

            if (source.canRead() && temp_dir.isDirectory()) {
                if (source.isFile()) {
                    outStream = new FileOutputStream(target);
                    inChannel = inStream.getChannel();
                    outChannel = ((FileOutputStream) outStream).getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } else if (source.isDirectory()) {
                    File[] files = source.listFiles();

                    if (createDir(target)) {
                        for (File file : files) {
                            copyFile(new File(source, file.getName()), new File(target, file.getName()), context);
                        }
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    DocumentFile targetDocument = DocumentFile.fromFile(temp_dir);
                    outStream = context.getContentResolver().openOutputStream(targetDocument.getUri());
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
                    Uri uri = MediaStoreUtils.getUriFromFile(target.getAbsolutePath(), context);
                    outStream = context.getContentResolver().openOutputStream(uri);
                } else {
                    return false;
                }

                if (outStream != null) {
                    byte[] buffer = new byte[BUFFER];
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                } else {
                    RootCommands.moveCopyRoot(source.getAbsolutePath(), target.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (inStream != null && outStream != null && inChannel != null && outChannel != null) {
                    inStream.close();
                    outStream.close();
                    inChannel.close();
                    outChannel.close();
                }
            } catch (Exception e) {
                // ignore exception
            }
        }
        return true;
    }

    // filePath = currentDir + "/" + item
    // newName = new name
    public static boolean renameTarget(String filePath, String newName) {
        File src = new File(filePath);

        String temp = filePath.substring(0, filePath.lastIndexOf("/"));
        File dest = new File(temp + "/" + newName);

        if (src.renameTo(dest)) {
            return true;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DocumentFile document = DocumentFile.fromFile(src);

                if (document.renameTo(dest.getAbsolutePath())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean createFile(File file) {
        if (file.exists()) {
            return !file.isDirectory();
        }

        try {
            if (file.createNewFile()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DocumentFile document = DocumentFile.fromFile(file.getParentFile());

            try {
                return document.createFile(MimeTypes.getMimeType(file), file.getName()) != null;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public static boolean createDir(File folder) {
        if (folder.exists())
            return false;

        if (folder.mkdir())
            return true;
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DocumentFile document = DocumentFile.fromFile(folder.getParentFile());
                if (document.exists())
                    return true;
            }

            if (Settings.rootAccess()) {
                return RootCommands.createRootdir(folder);
            }
        }

        return false;
    }

    public static void deleteTarget(String path) {
        File target = new File(path);

        if (target.isFile() && target.canWrite()) {
            target.delete();
        } else if (target.isDirectory() && target.canRead() && target.canWrite()) {
            String[] file_list = target.list();

            if (file_list != null && file_list.length == 0) {
                target.delete();
                return;
            } else if (file_list != null && file_list.length > 0) {
                for (String aFile_list : file_list) {
                    File temp_f = new File(target.getAbsolutePath() + "/"
                                           + aFile_list);

                    if (temp_f.isDirectory())
                        deleteTarget(temp_f.getAbsolutePath());
                    else if (temp_f.isFile()) {
                        temp_f.delete();
                    }
                }
            }

            if (target.exists())
                target.delete();
        } else if (!target.delete() && Settings.rootAccess()) {
            RootCommands.deleteRootFileOrDir(target);
        }
    }

    public static Intent createFileOpenIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);     
        intent.setDataAndType(Uri.fromFile(file), MimeTypes.getMimeType(file));
        return intent;
    }

    public static void openFile(final Context c, final File file) {
        new TheTask(c, file.getName(), new Runnable() {
                @Override
                public void run() {

                    if (file.isDirectory())
                        throw new IllegalArgumentException("File cannot be a directory!");

                    Intent intent = createFileOpenIntent(file);

                    try {
                        c.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        c.startActivity(Intent.createChooser(intent, c.getString(R.string.folder_open_file_with_, file.getName())));
                    } catch (Exception e) {
                        new AlertDialog.Builder(c)
                            .setMessage(e.getMessage())
                            .setTitle(R.string.folder_cant_open_file)
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                    }
                }
            }).execute();   
    }

    public static void shareFile(Activity c, String path) {
        File file = new File(path);
        Intent shareIntent = ShareCompat.IntentBuilder.from(c)
            .addStream(Uri.fromFile(file))
            .setType(MimeTypes.getMimeType(file))
            .getIntent();

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String title = c.getString(R.string.folder_share, file.getName());
        if (shareIntent.resolveActivity(c.getPackageManager()) != null) {
            c.startActivity(Intent.createChooser(shareIntent, title));
        } else {
            String error = c.getString(R.string.folder_share_error, file.getName());
            Toast.makeText(c, error, Toast.LENGTH_SHORT).show();
        }
    }

    public static List<ResolveInfo> getAppsThatHandleFile(File file, Context context) {
        return getAppsThatHandleIntent(createFileOpenIntent(file), context);
    }

    public static List<ResolveInfo> getAppsThatHandleIntent(Intent intent, Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.queryIntentActivities(intent, 0);
    }

    public static Drawable getAppIconForFile(File file, Context context) {
        List<ResolveInfo> infos = getAppsThatHandleFile(file, context);
        PackageManager packageManager = context.getPackageManager();
        for (ResolveInfo info : infos) {
            Drawable drawable = info.loadIcon(packageManager);
            if (drawable != null)
                return drawable;
        }
        return null;
	}

    // get MD5 or SHA1 checksum from a file
    public static String getChecksum(File file, String algorithm) {
        try {
            InputStream fis = new FileInputStream(file);
            MessageDigest digester = MessageDigest.getInstance(algorithm);
            byte[] bytes = new byte[2 * BUFFER];
            int byteCount;
            String result = "";

            while ((byteCount = fis.read(bytes)) > 0) {
                digester.update(bytes, 0, byteCount);
            }

            for (byte aB : digester.digest()) {
                result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // save current string in ClipBoard
    public static void savetoClipBoard(final Context co, String dir1) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) co
            .getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(
            "Copied Text", dir1);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(co,
                       "'" + dir1 + "' " + co.getString(R.string.folder_copied_to_clipboard),
                       Toast.LENGTH_SHORT).show();
    }

    public static void createShortcut(Activity main, String path) {
        File file = new File(path);
        Intent shortcutIntent;

        try {
            // Create the intent that will handle the shortcut
            if (file.isFile()) {
                shortcutIntent = new Intent(Intent.ACTION_VIEW);
                shortcutIntent.setDataAndType(Uri.fromFile(file), MimeTypes.getMimeType(file));
                shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                shortcutIntent = new Intent(main, ApplicationFolderActivity.class);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //shortcutIntent.putExtra(BrowserActivity.EXTRA_SHORTCUT, path);
            }

            // The intent to send to broadcast for register the shortcut intent
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());

            if (file.isFile()) {
                BitmapDrawable bd = (BitmapDrawable) IconPreview.getBitmapDrawableFromFile(file);

                if (bd != null) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap());
                } else {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                    Intent.ShortcutIconResource.fromContext(main, R.mipmap.format_unkown));
                }
            } else {
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource.fromContext(main, R.mipmap.ic_launcher));
            }

            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            main.sendBroadcast(intent);

            Toast.makeText(main, main.getString(R.string.folder_create_shortcut),
                           Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(main, main.getString(R.string.folder_create_shortcut_error), Toast.LENGTH_SHORT).show();
        }
    }

    public static void createShortcut(Activity main, String path, Drawable drawable) {
        File file = new File(path);
        Intent shortcutIntent;

        try {
            // Create the intent that will handle the shortcut
            if (file.isFile()) {
                shortcutIntent = new Intent(Intent.ACTION_VIEW);
                shortcutIntent.setDataAndType(Uri.fromFile(file), MimeTypes.getMimeType(file));
                shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                shortcutIntent = new Intent(main, ApplicationFolderActivity.class);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //shortcutIntent.putExtra(BrowserActivity.EXTRA_SHORTCUT, path);
            }

            // The intent to send to broadcast for register the shortcut intent
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, file.getName());

            if (file.isFile()) {
                Bitmap bd = getBitmapFromDrawable(drawable);
                if (bd != null) {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd);
                } else {
                    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                    Intent.ShortcutIconResource.fromContext(main, R.mipmap.format_unkown));
                }
            } else {
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                                Intent.ShortcutIconResource.fromContext(main, R.mipmap.ic_launcher));
            }

            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            main.sendBroadcast(intent);

            Toast.makeText(main, main.getString(R.string.folder_create_shortcut),
                           Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(main, main.getString(R.string.folder_create_shortcut_error), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Got reference from stackoverflow.com Url:
     * https://stackoverflow.com/questions/44447056/convert-adaptiveicondrawable-to-bitmap-in-android-o-preview?utm_medium=organic&utm_source=
     * google_rich_qa&utm_campaign=google_rich_qa
     */
    private static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }


    private static void createShortcut(Context context, String appName, Drawable draw, Intent intent, String iconResourceName) {
        Toast.makeText(context, String.format(context.getText(R.string.creating_application_shortcut).toString(), appName),
                       Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= 26) {
            doCreateShortcut(context, appName, draw, intent);
        } else {
            doCreateShortcut(context, appName, intent, iconResourceName);
        }
    }

    @TargetApi(14)
    private static void doCreateShortcut(Context context, String appName, Intent intent, String iconResourceName) {
        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
        if (iconResourceName != null) {
            Intent.ShortcutIconResource ir = new Intent.ShortcutIconResource();
            if (intent.getComponent() == null) {
                ir.packageName = intent.getPackage();
            } else {
                ir.packageName = intent.getComponent().getPackageName();
            }
            ir.resourceName = iconResourceName;
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, ir);
        }
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(shortcutIntent);
    }

    @TargetApi(26)
    private static void doCreateShortcut(Context context, String appName, Drawable draw, Intent intent) {
        ShortcutManager shortcutManager = Objects.requireNonNull(context.getSystemService(ShortcutManager.class));

        if (shortcutManager.isRequestPinShortcutSupported()) {
            Bitmap bitmap = getBitmapFromDrawable(draw);
            intent.setAction(Intent.ACTION_CREATE_SHORTCUT);


            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, appName)
                .setShortLabel(appName)
                .setLongLabel(appName)
                .setIcon(Icon.createWithBitmap(bitmap))
                .setIntent(intent)
                .build();

            shortcutManager.requestPinShortcut(shortcutInfo, null);
        } else {
            new AlertDialog.Builder(context)
                .setTitle(context.getText(R.string.error_creating_shortcut))
                .setMessage(context.getText(R.string.error_verbose_pin_shortcut))
                .setPositiveButton(context.getText(android.R.string.ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Just close dialog don't do anything
                        dialog.cancel();
                    }
                })
                .show();
        }
    }
    
    public static String formatCalculatedSize(long ls) {
        BigInteger size = BigInteger.valueOf(ls);
        String displaySize;

        if (size.divide(TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(TB_BI)) + " TB";
        } else if (size.divide(GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(GB_BI)) + " GB";
        } else if (size.divide(MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(MB_BI)) + " MB";
        } else if (size.divide(KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(KB_BI)) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    public static long getDirectorySize(File directory) {
        final File[] files = directory.listFiles();
        long size = 0;

        if (files == null) {
            return 0L;
        }

        for (final File file : files) {
            try {
                if (!isSymlink(file)) {
                    size += sizeOf(file);
                    if (size < 0) {
                        break;
                    }
                }
            } catch (IOException ioe) {
                // ignore exception when asking for symlink
            }
        }

        return size;
    }

    private static boolean isSymlink(File file) throws IOException {
        File fileInCanonicalDir;

        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    private static long sizeOf(File file) {
        if (file.isDirectory()) {
            return getDirectorySize(file);
        } else {
            return file.length();
        }
    }

    public static String getExtension(String name) {
        String ext;

        if (name.lastIndexOf(".") == -1) {
            ext = "";

        } else {
            int index = name.lastIndexOf(".");
            ext = name.substring(index + 1, name.length());
        }
        return ext;
    }

    public static String getFileExtension(File file) {
        return getFileExtension(file.getName());
    }

    /**
     * Gets extension of the file name excluding the . character
     */
    public static String getFileExtension(String fileName) {
        if (fileName.contains("."))
            return fileName.substring(fileName.lastIndexOf('.') + 1);
        else 
            return "";
    }

    public static boolean isSupportedVideo(File file) {
        String ext = getExtension(file.getName());
        return ext.equalsIgnoreCase("video");
    }

    public static boolean isSupportedArchive(File file) {
        String ext = getExtension(file.getName());
        return ext.equalsIgnoreCase("zip");
    }
}
