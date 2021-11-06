package com.file.evolution.engine.app.folders.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

public class AnimationUtils {
    public static Bundle getScaleBundle(View view) {
        return ActivityOptionsCompat.makeScaleUpAnimation(
                view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
    }

    public static void startActivityWithScale(@NonNull Activity startActivity, @NonNull Intent subActivity, @NonNull boolean forResult, @Nullable int code, @NonNull View view) {
        if(forResult){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                startActivity.startActivityForResult(subActivity, code, AnimationUtils.getScaleBundle
                        (view));
            else
                startActivity.startActivityForResult(subActivity, code);
        }
        else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                startActivity.startActivity(subActivity, AnimationUtils.getScaleBundle
                        (view));
            else
                startActivity.startActivity(subActivity);
        }
    }
}
