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
    public static PhotoViewPagerFragment getView(
            @NonNull
            ArrayList<? extends Info> picInfos,
            int defaultPosition,
            @Nullable
            Callback callback,
            @Nullable
            IPhotoViewPagerConfiguration photoViewPagerConfiguration) {
        if (callback == null) {
            callback = new Callback() {
                @Override
                public ImageView getPreviewView(String previewUrl) {
                    return null;
                }
            };
        }
        final PhotoViewPagerFragment fragment = PhotoViewPagerFragment.newInstance(null,
                picInfos,
                defaultPosition,
                callback,
                photoViewPagerConfiguration);
        return fragment;
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
        if (callback == null) {
            callback = new Callback() {
                @Override
                public ImageView getPreviewView(String previewUrl) {
                    return null;
                }
            };
        }
        final PhotoViewPagerFragment fragment = PhotoViewPagerFragment.newInstance(imageView,
                picInfos,
                defaultPosition,
                callback,
                photoViewPagerConfiguration);
        final long id = System.currentTimeMillis();
        PhotoViewPagerManager.INSTANCE.mFragmentMap.put(id, fragment);
        initStatusBarHeight(activity);
        ContainerActivity.start(activity, id);
        return fragment;
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
