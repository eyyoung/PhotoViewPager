package com.nd.android.sdp.common.photoviewpager;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 管理类
 */
public enum PhotoViewPagerManager {

    INSTANCE;

    private int mStatusHeight;

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
     * @param defaultPosition the default position
     * @param callback        the callback
     * @return the photo view pager fragment
     */
    @NonNull
    @Deprecated
    public static PhotoViewPagerFragment start(FragmentActivity activity,
                                               @Nullable
                                               ImageView imageView,
                                               @NonNull
                                               ArrayList<String> picList,
                                               ArrayList<String> previewList,
                                               int defaultPosition,
                                               @Nullable
                                               Callback callback) {
        if (picList.size() != previewList.size()) {
            throw new IllegalArgumentException("Url and Preview size not same!");
        }
        ArrayList<PicInfo> picInfos = new ArrayList<>();
        for (int i = 0, picListSize = picList.size(); i < picListSize; i++) {
            String url = picList.get(i);
            PicInfo picInfo = new PicInfo(url, picList.get(i), null, 0);
            picInfos.add(picInfo);
        }
        return startView(activity, imageView, picInfos, defaultPosition, callback, null);
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
    @Deprecated
    public static PhotoViewPagerFragment start(FragmentActivity activity,
                                               @Nullable
                                               ImageView imageView,
                                               @NonNull
                                               ArrayList<PicInfo> picInfos,
                                               int defaultPosition,
                                               @Nullable
                                               Callback callback) {
        return startView(activity, imageView, picInfos, defaultPosition, callback, null);
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
    @Deprecated
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
        return startView(activity, imageView, picInfos, defaultPosition, callback, photoViewPagerConfiguration);
    }

    @NonNull
    @Deprecated
    public static PhotoViewPagerFragment startView(FragmentActivity activity,
                                                   @Nullable
                                                   ImageView imageView,
                                                   @NonNull
                                                   ArrayList<? extends Info> picInfos,
                                                   int defaultPosition,
                                                   @Nullable
                                                   Callback callback) {
        return startView(activity, imageView, picInfos, defaultPosition, callback, null);
    }

    /**
     * Get view photo view pager fragment.
     *
     * @param picInfos        the pic infos
     * @param defaultPosition the default position
     * @return the photo view pager fragment
     */
    @Deprecated
    public static PhotoViewPagerFragment getView(
            @NonNull
            ArrayList<? extends Info> picInfos,
            int defaultPosition) {
        return getView(picInfos, defaultPosition, null, null);
    }

    /**
     * Get view photo view pager fragment.
     *
     * @param picInfos        the pic infos
     * @param defaultPosition the default position
     * @param callback        the callback
     * @return the photo view pager fragment
     */
    @Deprecated
    public static PhotoViewPagerFragment getView(
            @NonNull
            ArrayList<? extends Info> picInfos,
            int defaultPosition,
            @Nullable
            Callback callback) {
        return getView(picInfos, defaultPosition, callback, null);
    }

    /**
     * Get view photo view pager fragment.
     *
     * @param picInfos                    the pic infos
     * @param defaultPosition             the default position
     * @param callback                    the callback
     * @param photoViewPagerConfiguration the photo view pager configuration
     * @return the photo view pager fragment
     */
    @Deprecated
    public static PhotoViewPagerFragment getView(
            @NonNull
            ArrayList<? extends Info> picInfos,
            int defaultPosition,
            @Nullable
            Callback callback,
            @Nullable
            IPhotoViewPagerConfiguration photoViewPagerConfiguration) {
        PhotoViewOptions photoViewOptions = new PhotoViewOptions.Builder()
                .photoViewPagerConfiguration(photoViewPagerConfiguration)
                .callback(callback)
                .defaultPosition(defaultPosition)
                .build();
        return PhotoViewPagerFragment.newInstance(null,
                picInfos, photoViewOptions);
    }

    public static PhotoViewPagerFragment getView(
            @NonNull
            ArrayList<? extends Info> picInfos,
            @Nullable
            PhotoViewOptions photoViewOptions) {
        if (photoViewOptions == null) {
            photoViewOptions = PhotoViewOptions.createDefault();
        }
        return PhotoViewPagerFragment.newInstance(null,
                picInfos, photoViewOptions);
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
    @Deprecated
    public static PhotoViewPagerFragment startView(@NonNull FragmentActivity activity,
                                                   @Nullable
                                                   ImageView imageView,
                                                   @NonNull
                                                   ArrayList<? extends Info> picInfos,
                                                   int defaultPosition,
                                                   @Nullable
                                                   Callback callback,
                                                   @Nullable
                                                   IPhotoViewPagerConfiguration photoViewPagerConfiguration) {
        return startView((Activity) activity, imageView, picInfos, defaultPosition, callback, photoViewPagerConfiguration, false);
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
    @Deprecated
    public static PhotoViewPagerFragment startView(@NonNull Activity activity,
                                                   @Nullable
                                                   ImageView imageView,
                                                   @NonNull
                                                   ArrayList<? extends Info> picInfos,
                                                   int defaultPosition,
                                                   @Nullable
                                                   Callback callback,
                                                   @Nullable
                                                   IPhotoViewPagerConfiguration photoViewPagerConfiguration) {
        return startView(activity, imageView, picInfos, defaultPosition, callback, photoViewPagerConfiguration, false);
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
     * @param disableOrigin
     * @return the photo view pager fragment
     */
    @Deprecated
    @NonNull
    public static PhotoViewPagerFragment startView(@NonNull Activity activity,
                                                   @Nullable
                                                   ImageView imageView,
                                                   @NonNull
                                                   ArrayList<? extends Info> picInfos,
                                                   int defaultPosition,
                                                   @Nullable
                                                   Callback callback,
                                                   @Nullable
                                                   IPhotoViewPagerConfiguration photoViewPagerConfiguration,
                                                   boolean disableOrigin) {
        PhotoViewOptions photoViewOptions = new PhotoViewOptions.Builder()
                .imageView(imageView)
                .defaultPosition(defaultPosition)
                .callback(callback)
                .photoViewPagerConfiguration(photoViewPagerConfiguration)
                .disableOrigin(disableOrigin)
                .build();
        final PhotoViewPagerFragment fragment = PhotoViewPagerFragment.newInstance(imageView,
                picInfos, photoViewOptions);
        final long id = System.currentTimeMillis();
        PhotoViewPagerManager.INSTANCE.mFragmentMap.put(id, fragment);
        initStatusBarHeight(activity);
        ContainerActivity.start(activity, id);
        return fragment;
    }

    public static void startView(@NonNull Activity activity, @NonNull ArrayList<? extends Info> picInfos, @Nullable PhotoViewOptions photoViewOptions) {
        if (photoViewOptions == null) {
            photoViewOptions = PhotoViewOptions.createDefault();
        }
        final PhotoViewPagerFragment fragment = PhotoViewPagerFragment.newInstance(photoViewOptions.getImageView(),
                picInfos, photoViewOptions);
        final long id = System.currentTimeMillis();
        PhotoViewPagerManager.INSTANCE.mFragmentMap.put(id, fragment);
        initStatusBarHeight(activity);
        ContainerActivity.start(activity, id);
    }

    private static void initStatusBarHeight(Activity activity) {
        if (PhotoViewPagerManager.INSTANCE.mStatusHeight == 0) {
            PhotoViewPagerManager.INSTANCE.mStatusHeight = Utils.getStatusBarHeightFix(activity.getWindow());
        }
    }

    private final Map<Long, PhotoViewPagerFragment> mFragmentMap = new HashMap<>();

    public void removeFragment(PhotoViewPagerFragment photoViewPagerFragment) {
        Iterator<Map.Entry<Long, PhotoViewPagerFragment>> iterator = mFragmentMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, PhotoViewPagerFragment> entry = iterator.next();
            if (entry.getValue() == photoViewPagerFragment) {
                iterator.remove();
            }
        }
    }

    public int getStatusHeight(Activity activity) {
        if (mStatusHeight == 0) {
            initStatusBarHeight(activity);
        }
        return mStatusHeight;
    }

    public PhotoViewPagerFragment getFragmentById(long id) {
        return mFragmentMap.get(id);
    }

}
