package com.file.evolution.application.folder;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.content.res.AppCompatResources;
import android.support.v4.widget.NestedScrollView;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import org.apache.commons.io.FilenameUtils;
import static android.provider.MediaStore.Images.Thumbnails.MICRO_KIND;

import java.io.File;
import java.util.Arrays;

import com.file.evolution.R;
import com.file.evolution.engine.app.models.ActionItem;
import com.file.evolution.engine.app.preview.MimeTypes;
import com.file.evolution.application.folder.utils.VideoFolderUtils;
import com.file.evolution.engine.app.folders.utils.Thumbnail;
import net.dongliu.apk.parser.ApkParser;
import java.net.URI;
import com.file.evolution.engine.app.preview.IconPreview;

public class ActionMenuItem extends RelativeLayout {

    private Activity mActivity;
    private Context mContext;
    private Animation mTrackAnim;
    private LayoutInflater inflater;
    private ViewGroup mTrack;
    private View mRootView;
    private View mView;

    private ImageView mVideoThumbnail;
    private TextView mVideoTitle; 
    private TextView mVideoDuration;
    private TextView mVideoSize;
    private TextView mVideoLastModified;
    private PackageManager mPackageManager;
    private LinearLayout lyt_expand_text;
    private NestedScrollView nested_content;

    private OnActionItemClickListener mItemClickListener;

    private List<ActionItem> mActionItemList = new ArrayList<ActionItem>();

    private boolean mDidAction;
    private boolean mAnimateTrack;

    private int mChildPos;
    private int mAnimStyle;
    private boolean isOpened = true;

    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_AUTO = 4;
    private OnThumbnailClickListener mOnThumbnailClickListener;

    public interface OnThumbnailClickListener {
        void onThumbnailClick(View v);  
    }

    public void setOnThumbnailClickListener(OnThumbnailClickListener mOnThumbanilClickListener) {
        this.mOnThumbnailClickListener = mOnThumbanilClickListener;
    }
    public ActionMenuItem(Context context) {
        super(context);
        init(context, null);
    }

    public ActionMenuItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ActionMenuItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public ActionMenuItem(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        mActivity = (Activity)context;  

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mTrackAnim = AnimationUtils.loadAnimation(context, R.anim.rail);

        mTrackAnim.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float t) {
                    // Pushes past the target area, then snaps back into place.
                    // Equation for graphing: 1.2-((x*1.6)-1.1)^2
                    final float inner = (t * 1.55f) - 1.1f;

                    return 1.2f - inner * inner;
                }
            });

        setRootViewId(R.layout.video_folder_items);
        collapse(lyt_expand_text);
        mAnimStyle = ANIM_AUTO;
        mAnimateTrack = true;
        mChildPos = 0;
        if (mAnimateTrack)
            mTrack.startAnimation(mTrackAnim);

        mPackageManager = mContext.getPackageManager();
    }

    /**
     * Get action item at an index
     * 
     * @param index
     *            Index of item (position from callback)
     * 
     * @return Action Item at the position
     */
    public ActionItem getActionItem(int index) {
        return mActionItemList.get(index);
    }

    /**
     * Set root view.
     * 
     * @param id
     *            Layout resource id
     */
    @SuppressWarnings("deprecation")
    public void setRootViewId(int id) {
        mRootView = (ViewGroup) inflater.inflate(id, null);
        mTrack = (ViewGroup) mRootView.findViewById(R.id.tracks);
        mView = mRootView.findViewById(R.id.video_library_layout);
        mView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {  
                    menuToggle(isOpened); 
                    isOpened = !isOpened;               
                } 

            });

        lyt_expand_text = (LinearLayout)mRootView.findViewById(R.id.lyt_expand_text);
        nested_content = (NestedScrollView)mRootView.findViewById(R.id.nested_content);

        mVideoThumbnail = (ImageView) mRootView.findViewById(R.id.video_thumbnail);
        mVideoThumbnail.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if (mOnThumbnailClickListener != null) {
                        mOnThumbnailClickListener.onThumbnailClick(v);
                    }
                }
            });


        mVideoTitle = (TextView) mRootView.findViewById(R.id.video_title);   
        mVideoDuration = (TextView) mRootView.findViewById(R.id.video_duration);    
        mVideoSize = (TextView) mRootView.findViewById(R.id.video_size);       
        mVideoLastModified = (TextView) mRootView.findViewById(R.id.video_last_modified);

        // This was previously defined on show() method, moved here to prevent
        // force close that occured
        // when tapping fastly on a view to show quickaction dialog.
        // Thanx to zammbi (github.com/zammbi)
        if (VERSION.SDK_INT >= VERSION_CODES.FROYO) {
            mRootView.setLayoutParams(new LayoutParams(
                                          LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        } else {
            mRootView.setLayoutParams(new LayoutParams(
                                          LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        }

        addView(mRootView);
    }

    public void setVideoThumbnail(String thumbnail) {
        /*final String ext = FilenameUtils.getExtension(thumbnail); 
        if (Arrays.asList(MimeTypes.MIME_APK).contains(ext)) {
            PackageInfo info = mPackageManager.getPackageArchiveInfo(thumbnail, 1);
            ApplicationInfo appInfo = info.applicationInfo;
            IconAsyncTask iconLoader = new IconAsyncTask(mVideoThumbnail, appInfo);
            iconLoader.execute();
            
        } else {

            File f = new File(thumbnail);
            if (f.isDirectory() && f.canRead()) {                  
                Thumbnail.setThumbnail(thumbnail, MICRO_KIND, mVideoThumbnail, true);
            } 
            if (f.isFile()) {
                Thumbnail.setThumbnail(thumbnail, MICRO_KIND, mVideoThumbnail, false);        
            }
        }*/
         // get icon
        File file = new File(thumbnail);
        IconPreview.getFileIcon(file, mVideoThumbnail);
    }

    public void setVideoThumbnail(int thumbnail) {
        mVideoThumbnail.setImageResource(thumbnail);
    }

    public void setVideoThumbnail(Drawable thumbnail) {
        mVideoThumbnail.setImageDrawable(thumbnail);
    }

    public void setVideoTitle(String title) {
        mVideoTitle.setText(title);
    }

    public void setVideoDuration(String duration) {
        
        mVideoDuration.setText(duration);
    }

    public void setVideoSize(String size) {
        mVideoSize.setText(size);
    }

    public void setVideoLastModified(String lastModified) {
        mVideoLastModified.setText(lastModified);
    }


    /**
     * Animate track.
     * 
     * @param mAnimateTrack
     *            flag to animate track
     */
    public void mAnimateTrack(boolean mAnimateTrack) {
        this.mAnimateTrack = mAnimateTrack;
    }

    /**
     * Set animation style.
     * 
     * @param mAnimStyle
     *            animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int mAnimStyle) {
        this.mAnimStyle = mAnimStyle;
    }

    /**
     * Quick way to add an action item
     * 
     * @param id
     *            the item id
     * @param drawable
     *            the drawable resource id (or 0 to ignore)
     */
    public void addActionItem(int id, int drawable) {
        Drawable icon = null;

        if (drawable != 0) {
            icon = AppCompatResources.getDrawable(mContext, drawable);
        }

        addActionItem(new ActionItem(id, icon));
    }

    /**
     * Quick way to add an action item
     * 
     * @param id
     *            the item id
     * @param title
     *            the string resource id (or 0 to ignore)
     * @param drawable
     *            the drawable resource id (or 0 to ignore)
     */
    public void addActionItem(int id, int title, int drawable) {
        String strTitle = null;
        Drawable icon = null;

        if (title != 0) {
            strTitle = mContext.getString(title);
        }

        if (drawable != 0) {
            icon = AppCompatResources.getDrawable(mContext, drawable);
        }

        addActionItem(new ActionItem(id, strTitle, icon));
    }

    /**
     * Add action item
     * 
     * @param action
     *            {@link ActionItem}
     */
    public void addActionItem(ActionItem action) {
        mActionItemList.add(action);

        String title = action.getTitle();
        Drawable icon = action.getIcon();

        View container = (View) inflater.inflate(R.layout.action_item, null);

        ImageView img = (ImageView) container.findViewById(R.id.iv_icon);
        TextView text = (TextView) container.findViewById(R.id.tv_title);

        if (icon != null) {
            img.setImageDrawable(icon);
        } else {
            img.setVisibility(View.GONE);
        }

        if (title != null) {
            text.setText(title);
            text.setSelected(true);
        } else {
            text.setVisibility(View.GONE);
        }

        final int pos = mChildPos;
        final int actionId = action.getActionId();

        container.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(ActionMenuItem.this, pos, actionId);
                    }

                    if (!getActionItem(pos).isSticky()) {
                        mDidAction = true;

                        // workaround for transparent background bug
                        // thx to Roman Wozniak <roman.wozniak@gmail.com>
                        v.post(new Runnable() {
                                @Override
                                public void run() {
                                    menuToggle(false);
                                }
                            });
                    }
                }
            });

        container.setFocusable(true);
        container.setClickable(true);

        mTrack.addView(container, mChildPos + 1);

        mChildPos++;
    }


    private void menuToggle(boolean show) {
        if (show) {
            expand(lyt_expand_text, new AnimListener(){
                    @Override
                    public void onFinish() {
                        nestedScrollTo(nested_content, lyt_expand_text);
                    }
                });
        } else {
            collapse(lyt_expand_text);
        }
    }


    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(90);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }

    private void toggleSectionText(View view) {
        boolean show = toggleArrow(view);
        if (show) {
            expand(lyt_expand_text, new AnimListener(){
                    @Override
                    public void onFinish() {
                        nestedScrollTo(nested_content, lyt_expand_text);
                    }
                });
        } else {
            collapse(lyt_expand_text);
        }
    }

    public interface AnimListener {
        void onFinish();
    }

    public static void expand(final View v, final AnimListener animListener) {
        Animation a = expandAction(v);
        a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animListener.onFinish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        v.startAnimation(a);
    }

    private static Animation expandAction(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetedHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                    ? ViewGroup.LayoutParams.WRAP_CONTENT
                    : (int) (targetedHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (targetedHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
        return a;
    }

    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {
        nested.post(new Runnable(){
                @Override
                public void run() {
                    nested.scrollTo(500, targetView.getBottom());
                }
            });
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public void setOnActionItemClickListener(OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }
    /**
     * Listener for item click
     * 
     */
    public interface OnActionItemClickListener {
        public abstract void onItemClick(ActionMenuItem source, int pos, int actionId);
    }

    class IconAsyncTask extends AsyncTask<Void, Integer, Drawable> {

        ImageView imageView;
        ApplicationInfo info;

        IconAsyncTask(ImageView imageView, ApplicationInfo info) {
            this.imageView = imageView;
            this.info = info;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Drawable doInBackground(Void... voids) {
            if (!isCancelled())
                return info.loadIcon(mPackageManager);
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            imageView.setImageDrawable(drawable);
            imageView.setVisibility(View.VISIBLE);
        }
    }
}


