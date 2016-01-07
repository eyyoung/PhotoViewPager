package com.nd.android.sdp.common.photoviewpager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;

import java.util.ArrayList;

/**
 * 管理类
 */
public enum PhotoViewPagerManager {

    INSTANCE;

    private IPhotoViewPagerConfiguration mConfiguration;

    public void init(IPhotoViewPagerConfiguration configuration) {
        mConfiguration = configuration;
    }

    public IPhotoViewPagerConfiguration getConfiguration() {
        return mConfiguration;
    }

    /**
     * Start
     *
     * @param activity        the context
     * @param imageView       the image view
     * @param picInfos        the pic infos
     * @param defaultPosition the default position
     * @param callback        the callback
     * @return the photo view pager fragment
     */
    @NonNull
    public static PhotoViewPagerFragment start(FragmentActivity activity,
                                               @Nullable
                                               ImageView imageView,
                                               @NonNull
                                               ArrayList<PicInfo> picInfos,
                                               int defaultPosition,
                                               @Nullable
                                               Callback callback) {
        return start(activity, imageView, picInfos, defaultPosition, callback, null);
    }

    /**
     * Start
     *
     * @param activity                    the context
     * @param imageView                   the image view
     * @param picInfos                    the pic infos
     * @param defaultPosition             the default position
     * @param callback                    the callback
     * @param photoViewPagerConfiguration Configuration
     * @return the photo view pager fragment
     */
    @NonNull
    public static PhotoViewPagerFragment start(FragmentActivity activity,
                                               @Nullable
                                               ImageView imageView,
                                               @NonNull
                                                   ArrayList<PicInfo> picInfos,
                                               int defaultPosition,
                                               @Nullable
                                               Callback callback,
                                               @Nullable
                                               IPhotoViewPagerConfiguration photoViewPagerConfiguration) {
        if (callback == null) {
            callback = new Callback() {
                @Override
                public ImageView getPreviewView(String url) {
                    return null;
                }
            };
        }
        final PhotoViewPagerFragment fragment = PhotoViewPagerFragment.newInstance(imageView,
                picInfos,
                defaultPosition,
                callback,
                photoViewPagerConfiguration);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(Window.ID_ANDROID_CONTENT, fragment, PhotoViewPagerFragment.TAG_PHOTO)
                .commitAllowingStateLoss();
        return fragment;
    }

}
