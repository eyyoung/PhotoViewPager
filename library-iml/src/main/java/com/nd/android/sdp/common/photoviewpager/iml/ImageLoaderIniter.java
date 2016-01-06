package com.nd.android.sdp.common.photoviewpager.iml;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.nd.android.sdp.common.photoviewpager.IPhotoViewPagerConfiguration;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageLoader 初始化器
 */
public enum ImageLoaderIniter implements IPhotoViewPagerConfiguration {

    INSTANCE;

    private boolean mIsInited = false;

    public void init() {
        if (mIsInited) {
            return;
        }
        PhotoViewPagerManager.INSTANCE.init(this);
        mMemoryCaches.add(ImageLoader.getInstance().getMemoryCache());
        mIsInited = true;
    }

    private List<MemoryCache> mMemoryCaches = new ArrayList<>();

    @Override
    public File getPicDiskCache(String url) {
        final DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
        return diskCache.get(url);
    }

    @Override
    public Bitmap getPreviewBitmap(String url) {
        for (MemoryCache memoryCache : mMemoryCaches) {
            final Bitmap bitmapInMemoryCache = getBitmapInMemoryCache(url, memoryCache);
            if (bitmapInMemoryCache != null) {
                return bitmapInMemoryCache;
            }
        }
        return null;
    }

    @Nullable
    private Bitmap getBitmapInMemoryCache(String url, MemoryCache memoryCache) {
        final List<Bitmap> bitmaps = MemoryCacheUtils.findCachedBitmapsForImageUri(url, memoryCache);
        if (bitmaps != null && !bitmaps.isEmpty()) {
            return bitmaps.get(0);
        } else {
            return null;
        }
    }

    /**
     * 添加内存缓存
     *
     * @param memoryCache 内存缓存
     */
    public void addMemoryCache(MemoryCache memoryCache) {
        mMemoryCaches.add(memoryCache);
    }
}
