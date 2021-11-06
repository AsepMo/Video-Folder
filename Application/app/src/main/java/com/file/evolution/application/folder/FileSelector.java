package com.file.evolution.application.folder;
 
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Environment;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import com.file.evolution.R;
import com.file.evolution.application.folder.utils.VideoFolderUtils;

public class FileSelector
{
	private static Activity context;
	private String[] extensions;
	private ArrayList<SelectedFile> itemsData = new ArrayList<>();
	public static final String ZIP = ".zip", MP4 = ".mp4", MP3 = ".mp3", JPG = ".jpg", JPEG = ".jpeg", PNG = ".png", DOC = ".doc", DOCX = ".docx", XLS = ".xls", XLSX = ".xlsx", PDF = ".pdf";


	public FileSelector(Activity context, String[] extensions) {
		this.context = context;
		this.extensions = extensions;
	}

	public interface OnSelectListener {
		void onSelect(String path);
	}

	public void selectFile(OnSelectListener listener) {
		//String ExternalStorage = VideoFolderUtils.getInternalStorageDirectory().getAbsolutePath();
        String InternalStorage = VideoFolderUtils.getExternalStorageDirectory(context, true);
		listOfFile(new File(InternalStorage));
		dialogFileList(listener);
	}

	private void listOfFile(File dir) {
		File[] list = dir.listFiles();

		for (File file : list) {
			if (file.isDirectory()) {
				if (!new File(file, ".nomedia").exists() && !file.getName().startsWith(".")) {
					Log.w("LOG", "IS DIR " + file);
					listOfFile(file);
				}
			} else {
				String path = file.getAbsolutePath();

				for (String ext : extensions) {
					if (path.endsWith(ext)) {
						SelectedFile selectedFile = new SelectedFile();

						selectedFile.path = path;
						String[] split = path.split("/");
						selectedFile.name = split[split.length - 1];
						itemsData.add(selectedFile);
						Log.i("LOG", "ADD " + selectedFile.path + " " + selectedFile.name);
					}
				}
			}
		}
		Log.d("LOG", itemsData.size() + " DONE");
	}

	private void dialogFileList(OnSelectListener listener) {
		LinearLayout lytMain = new LinearLayout(context);
		lytMain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		lytMain.setOrientation(LinearLayout.VERTICAL);
		int p = convertToPixels(12);
		lytMain.setPadding(p, p, p, p);
		lytMain.setGravity(Gravity.CENTER);
		lytMain.setBackgroundResource(R.drawable.background_holo_blue);

		TextView textView = new TextView(context);
		textView.setLayoutParams(new LinearLayout.LayoutParams(screenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setGravity(Gravity.CENTER);
		textView.setText("MP3 FOLDER");
		textView.setTextColor(Color.WHITE);
		textView.setBackgroundResource(R.drawable.background_holo_blue);

		ListView mZipFolder = new ListView(context);

		lytMain.addView(textView);
		lytMain.addView(mZipFolder);

		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(lytMain);
		dialog.setCancelable(true);
		dialog.show();

		ZipAdapter mZipAdapter = new ZipAdapter(context,dialog, listener, itemsData);
		mZipFolder.setAdapter(mZipAdapter);
	}

	private int convertToPixels(int dp) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dp * scale + 0.5f);
	}

	private int screenWidth() {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	private class SelectedFile {
		public String path = "";
		public String name = "";
	}

	private class ZipAdapter extends ArrayAdapter<SelectedFile>{
		// Layout Inflater
		private LayoutInflater inflater;
		private ArrayList<SelectedFile> itemsData;
		private OnSelectListener listener;
		private Dialog dialog;
		// Name of the file
		public TextView nameLabel;

		// Size of the file
		public TextView detailLabel;

		// Icon of the file
		public ImageView icon;
		public ZipAdapter(Context c, Dialog dialog, OnSelectListener listener, ArrayList<SelectedFile> itemsData) {
			super(c, R.layout.item_file_list_selector, itemsData);

			this.itemsData = itemsData;
			this.listener = listener;
			this.dialog = dialog;
			this.inflater = LayoutInflater.from(context);

		}

		@Override
		public int getCount()
		{
			// TODO: Implement this method
			return itemsData.size();
		}

		@Override
		public FileSelector.SelectedFile getItem(int position)
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public long getItemId(int position)
		{
			// TODO: Implement this method
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			// TODO: Implement this method
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View inflateView = inflater.inflate(R.layout.item_file_list_selector, parent, false);

			final SelectedFile selectedFile = itemsData.get(position);
			nameLabel = inflateView.findViewById(R.id.tvFileName);
			detailLabel = inflateView.findViewById(R.id.tvFileDetails);
			detailLabel.setTypeface(detailLabel.getTypeface(), Typeface.ITALIC);
			detailLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);		
			icon = inflateView.findViewById(R.id.imgFileIcon);

			nameLabel.setText(selectedFile.name);
			detailLabel.setText(selectedFile.path);
			icon.setImageResource(R.drawable.type_package);

			inflateView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						listener.onSelect(selectedFile.path);
					}
				});
			return inflateView;
		}
	}

	public static class ZipTask extends AsyncTask<Void, Void, Void>
	{
        private String mTips;
        private Runnable mTargetTask;
        private ProgressDialog mDialog;

        public ZipTask(String tips, Runnable task)
		{
            mTips = tips;
            mTargetTask = task;
        }

        @Override
        protected void onPreExecute()
		{
            mDialog = new ProgressDialog(context);
            mDialog.setMessage(mTips);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params)
		{
            mTargetTask.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
		{
            mDialog.dismiss();
        }
    }
}
