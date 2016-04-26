package com.nd.android.sdp.common.photoviewpager.utils;

import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * The type Animate utils.
 */
public final class AnimateUtils {

    private static final int ANIMATION_DURATION = 300;

    /**
     * Fade out view.
     *
     * @param view the view
     */
    public static void fadeInView(View view) {
        if (view == null) {
            return;
        }
        view.setAlpha(0);
        view.animate()
                .alpha(1.0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    /**
     * Fade out view.
     *
     * @param view the view
     */
    public static void fadeOutView(View view) {
        view.setAlpha(1.0f);
        view.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

}
