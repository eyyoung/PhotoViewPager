package com.nd.android.sdp.common.photoviewpager.iml;

import android.graphics.Bitmap;
import android.view.View;

import com.nd.android.sdp.common.photoviewpager.IPhotoViewPagerConfiguration;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.util.List;

/**
 * ImageLoader 初始化器
 */
public enum ImageLoaderIniter implements IPhotoViewPagerConfiguration {

    INSTANCE;

    public void init() {
        PhotoViewPagerManager.INSTANCE.init(this);
    }

    @Override
    public void startGetImage(String url, final ImageGetterCallback imageGetterCallback) {
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().loadImage(url,
                new ImageSize(0, 0),
                displayImageOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        imageGetterCallback.error(imageUri, view, failReason.getCause());
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageGetterCallback.setImageToView(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingProgress(long total, long current) {
                        imageGetterCallback.setProgress(current, total);
                    }
                });
    }

    @Override
    public File getPicDiskCache(String url) {
        final DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
        final File file = diskCache.get(url);
        return file;
    }

    @Override
    public Bitmap getPreviewBitmap(String url) {
        final MemoryCache memoryCache = ImageLoader.getInstance().getMemoryCache();
        final List<Bitmap> bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(url, memoryCache);
        if (bitmaps != null && !bitmaps.isEmpty()) {
            return bitmaps.get(0);
        } else {
            return null;
        }
    }
}
