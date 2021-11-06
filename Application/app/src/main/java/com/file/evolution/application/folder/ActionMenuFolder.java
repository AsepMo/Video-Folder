package com.file.evolution.application.folder;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import android.support.v4.widget.NestedScrollView;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.widget.HorizontalScrollView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.file.evolution.R;
import com.file.evolution.engine.app.models.ActionItem;
import com.file.evolution.engine.widget.DirectoryNavigationView;

public class ActionMenuFolder extends RelativeLayout implements DirectoryNavigationView.OnNavigateListener, DirectoryNavigationView.OnUpdatePathListener{

    private Activity mActivity;
    private Context mContext;
    private Animation mTrackAnim;
    private LayoutInflater inflater;
    private ViewGroup mTrack;
    private View mRootView;
    private ImageButton bt_toggle_text;
    private ImageView mFolderSettings;
    
    
    private LinearLayout lyt_expand_text;
    private NestedScrollView nested_content;

    private OnActionItemClickListener mItemClickListener;
    private OnFolderSettingClickListener mOnFolderSettingClickListener;
    private OnToggleMenuListener mOnToggleMenuListener;
    private OnNavigateListener mOnNavigateListener;
    private List<ActionItem> mActionItemList = new ArrayList<ActionItem>();
   
    public interface OnNavigateListener {
        void onNavigate(String path);
    }
    
    public interface OnUpdatePathListener {
        void onUpdatePath(String path);
    }
    
    private boolean mDidAction;
    private boolean mAnimateTrack;

    private int mChildPos;
    private int mAnimStyle;

    public static final int ANIM_GROW_FROM_LEFT = 1;
    public static final int ANIM_GROW_FROM_RIGHT = 2;
    public static final int ANIM_GROW_FROM_CENTER = 3;
    public static final int ANIM_AUTO = 4;
    
    private static DirectoryNavigationView mNavigation;
    
    public ActionMenuFolder(Context context) {
        super(context);
        init(context, null);
    }

    public ActionMenuFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ActionMenuFolder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public ActionMenuFolder(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mActivity = (Activity)context;  
        mContext = context;
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

        setRootViewId(R.layout.action_menu_folder_layout);
        collapse(lyt_expand_text);
        mAnimStyle = ANIM_AUTO;
        mAnimateTrack = true;
		mChildPos = 0;
        if (mAnimateTrack)
			mTrack.startAnimation(mTrackAnim);
            
        mNavigation = new DirectoryNavigationView(mActivity);

        // add listener for navigation view
        if (mNavigation.listeners.isEmpty())
            mNavigation.addonNavigateListener(this);

        mNavigation.setOnUpdatePath(this);
    }
    
    
    public void setDirectoryButtons(String path) {
        mNavigation.mOnUpdatePathListener.onUpdatePath(path);
    }
   
    public void onStart(){
         // add listener for navigation view
        if (mNavigation.listeners.isEmpty())
            mNavigation.addonNavigateListener(this);
    }
    
    public void onResume(){
         // add listener for navigation view
        if (mNavigation.listeners.isEmpty())
            mNavigation.addonNavigateListener(this);
    }
    
    public void onPause(){
        if (mNavigation != null)
            mNavigation.removeOnNavigateListener(this);
    }
    
    public void onDestroy(){
        if (mNavigation != null)
            mNavigation.removeOnNavigateListener(this);
    }
    
    @Override
    public void onNavigate(String path) {
        if(mOnNavigateListener != null)    
        {
            mOnNavigateListener.onNavigate(path);
        }
    }
    
    @Override
    public void onUpdatePath(String path) {
        
        mNavigation.setDirectoryButtons(path);    
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
        
        bt_toggle_text = (ImageButton)mRootView.findViewById(R.id.bt_toggle_text);
        bt_toggle_text.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    toggleMenu(bt_toggle_text);
                }
            });
        mFolderSettings = (ImageView)mRootView.findViewById(R.id.folder_icon_settings);
        final int pos = mChildPos;
        mFolderSettings.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v) {
                    if(mOnFolderSettingClickListener != null){
                        mOnFolderSettingClickListener.onFolderSettingClick(v);
                    }
                    
                    if (!getActionItem(pos).isSticky()) {
                        mDidAction = true;

                        // workaround for transparent background bug
                        // thx to Roman Wozniak <roman.wozniak@gmail.com>
                        v.post(new Runnable() {
                                @Override
                                public void run() {
                                    toggleMenu(bt_toggle_text);
                                }
                            });
                       }
                    }
                
            });
        lyt_expand_text = (LinearLayout)mRootView.findViewById(R.id.lyt_expand_text);
        nested_content = (NestedScrollView)mRootView.findViewById(R.id.nested_content);

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
            icon = mContext.getResources().getDrawable(drawable);
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
            icon = mContext.getResources().getDrawable(drawable);
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
                public void onClick(final View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(ActionMenuFolder.this, pos, actionId);
                    }

                    if (!getActionItem(pos).isSticky()) {
                        mDidAction = true;

                        // workaround for transparent background bug
                        // thx to Roman Wozniak <roman.wozniak@gmail.com>
                        v.post(new Runnable() {
                                @Override
                                public void run() {
                                    
                                    toggleMenu(bt_toggle_text);
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

    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(90);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }

    public void toggleMenu(View view) {
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
    

    // save current string in ClipBoard
    public static void savetoClipBoard(final Context co, String dir1) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) co.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText( "Copied Text", dir1);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(co, "'" + dir1 + "' " + co.getString(R.string.folder_copied_to_clipboard), Toast.LENGTH_SHORT).show();
    }
  
    public void setOnNavigationListener(OnNavigateListener listener) {
        mOnNavigateListener = listener;
    }
    /**
     * Listener for item click
     * 
     */
    public void setOnFolderSettingListener(OnFolderSettingClickListener listener) {
        mOnFolderSettingClickListener = listener;
    } 
    
    public interface OnFolderSettingClickListener {
        public abstract void onFolderSettingClick(View view);
	}
    
    public interface OnToggleMenuListener {
        public abstract void OnToggleMenu(View view);
    }
    
    public void setOnToggleMenuListener(OnToggleMenuListener listener){
       mOnToggleMenuListener =  listener;
    }
    public void setOnActionItemClickListener(OnActionItemClickListener listener) {
        mItemClickListener = listener;
    }
    /**
     * Listener for item click
     * 
     */
    public interface OnActionItemClickListener {
        public abstract void onItemClick(ActionMenuFolder source, int pos, int actionId);
	}
}
