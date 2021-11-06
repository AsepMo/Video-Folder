package com.file.evolution.application.folder.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

import com.file.evolution.R;
import com.file.evolution.engine.app.preview.MimeTypes;
import com.file.evolution.engine.app.folders.utils.Thumbnail;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.io.File;

public class VideoSelectorAdapter extends
ArrayAdapter<VideoSelectorAdapter.FileDetail> {

    // Layout Inflater
    private final LayoutInflater inflater;

    // List of file details
    private final LinkedList<FileDetail> fileDetails;

    public static class ViewHolder {

        // Name of the file
        public TextView nameLabel;

        // Size of the file
        public TextView sizeLabel;

        // Date of the file
        public TextView dateLabel;

        // Icon of the file
        public ImageView icon;
    }

    public VideoSelectorAdapter(final Context context, final LinkedList<FileDetail> fileDetails, final boolean isRoot) {
        super(context, R.layout.item_file_list, fileDetails);
        this.fileDetails = fileDetails;
        this.inflater = LayoutInflater.from(context);
        if (!isRoot) {
            this.fileDetails.addFirst(new FileDetail("..", "", context.getString(R.string.folder_parent), "", true));
        } else {
            this.fileDetails.addFirst(new FileDetail(context.getString(R.string.folder_home), "", context.getString(R.string.folder_parent), "", true));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.item_file_list, null);

            final ViewHolder hold = new ViewHolder();
            hold.nameLabel = (TextView) convertView.findViewById(android.R.id.title);
            hold.sizeLabel = (TextView) convertView.findViewById(android.R.id.text1);
            hold.dateLabel = (TextView) convertView.findViewById(android.R.id.text2);
            hold.icon = (ImageView) convertView.findViewById(android.R.id.icon);
            convertView.setTag(hold);
            final FileDetail fileDetail = fileDetails.get(position);
            final String fileName = fileDetail.getName();
            if (fileDetail.isFolder()) {
                Thumbnail.setThumbnail(fileDetail.getPath(), MICRO_KIND, hold.icon, true);
            } else {
                Thumbnail.setThumbnail(fileDetail.getPath(), MICRO_KIND, hold.icon, false);        
            }
            //setIcon(hold, fileDetail);
            hold.nameLabel.setText(fileName);
            hold.sizeLabel.setText(fileDetail.getSize());
            hold.dateLabel.setText(fileDetail.getDateModified());
        } else {
            final ViewHolder hold = ((ViewHolder) convertView.getTag());
            final FileDetail fileDetail = fileDetails.get(position);
            final String fileName = fileDetail.getName();
            if (fileDetail.isFolder()) {
                Thumbnail.setThumbnail(fileDetail.getPath(), MICRO_KIND, hold.icon, true);
            } else {
                Thumbnail.setThumbnail(fileDetail.getPath(), MICRO_KIND, hold.icon, false);        
            }
            //setIcon(hold, fileDetail);
            hold.nameLabel.setText(fileName);
            hold.sizeLabel.setText(fileDetail.getSize());
            hold.dateLabel.setText(fileDetail.getDateModified());
        }
        return convertView;
    }

    private void setIcon(final ViewHolder viewHolder, final FileDetail fileDetail) {
        final String fileName = fileDetail.getName();
        final String ext = FilenameUtils.getExtension(fileName);

        if (fileDetail.isFolder()) {
            viewHolder.icon.setImageResource(R.mipmap.ic_folder_gray_48dp);
        } else if (Arrays.asList(MimeTypes.MIME_HTML).contains(ext) || ext.endsWith("html")) {
            viewHolder.icon.setImageResource(R.mipmap.format_html);
        } else if (Arrays.asList(MimeTypes.MIME_CODE).contains(ext)
                   || fileName.endsWith("css")
                   || fileName.endsWith("js")) {
            viewHolder.icon.setImageResource(R.mipmap.format_unkown);
        } else if (Arrays.asList(MimeTypes.MIME_ARCHIVE).contains(ext)) {
            viewHolder.icon.setImageResource(R.mipmap.format_zip);
        } else if (Arrays.asList(MimeTypes.MIME_MUSIC).contains(ext)) {
            viewHolder.icon.setImageResource(R.mipmap.format_music);
        } else if (Arrays.asList(MimeTypes.MIME_PICTURE).contains(ext)) {
            viewHolder.icon.setImageResource(R.mipmap.format_picture);
        } else if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(ext)) {
            viewHolder.icon.setImageResource(R.mipmap.format_media);
        } else {
            viewHolder.icon.setImageResource(R.mipmap.ic_file_gray_48dp);
        }
    }

    public static class FileDetail {
        private String name;
        private String path;
        private String size;
        private String dateModified;
        private boolean isFolder;

        public FileDetail(String name, String path, String size, String dateModified, boolean isFolder) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.dateModified = dateModified;
            this.isFolder = isFolder;
            if (TextUtils.isEmpty(dateModified)) {
                isFolder = true;
            } else {
                isFolder = false;
            }
        }

        public String getDateModified() {
            return dateModified;
        }

        public String getSize() {
            return size;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }

        public boolean isFolder() {
            return isFolder;
        }
    }
}
