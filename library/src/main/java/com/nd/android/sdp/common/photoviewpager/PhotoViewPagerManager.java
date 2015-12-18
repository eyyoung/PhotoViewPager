package com.nd.android.sdp.common.photoviewpager;

import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * 管理类
 */
public enum PhotoViewPagerManager {

    INSTANCE;

    private static IPhotoViewPagerConfiguration mConfiguration;

    public static void init(IPhotoViewPagerConfiguration configuration) {
        mConfiguration = configuration;
    }

    public static IPhotoViewPagerConfiguration getConfiguration() {
        return mConfiguration;
    }

    /**
     * Start
     *
     * @param activity the context
     */
    public static PhotoViewPagerFragment start(FragmentActivity activity,
                                               ImageView imageView,
                                               ArrayList<String> urls,
                                               ArrayList<String> previewUrls,
                                               int defaultPosition,
                                               Callback callback) {
        final PhotoViewPagerFragment fragment = PhotoViewPagerFragment.newInstance(imageView,
                urls,
                previewUrls,
                defaultPosition,
                callback);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(Window.ID_ANDROID_CONTENT, fragment, PhotoViewPagerFragment.TAG_PHOTO)
                .commitAllowingStateLoss();
        return fragment;
    }

}
