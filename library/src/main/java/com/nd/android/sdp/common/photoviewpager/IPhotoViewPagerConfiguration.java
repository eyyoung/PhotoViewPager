package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Bitmap;

import java.io.File;

/**
 */
public interface IPhotoViewPagerConfiguration {

    /**
     * 获取指定Url的磁盘缓存
     *
     * @param url URL
     * @return 缓存文件（用于判定图片是否为GIF）
     */
    File getPicDiskCache(String url);

    /**
     * 获取内存中的缓存
     *
     * @param url url
     * @return 图片
     */
    Bitmap getPreviewBitmap(String url);

}
