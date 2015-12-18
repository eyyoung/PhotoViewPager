package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;

import java.io.File;

public interface Callback {
    /**
     * 获取预览控件
     *
     * @param url url
     * @return 预览控件
     */
    ImageView getPreviewView(String url);

    /**
     * 获取图片
     *
     * @param url                 url
     * @param imageGetterCallback 回调通知
     */
    void startGetImage(String url, ImageGetterCallback imageGetterCallback);

    /**
     * 获取指定Url的磁盘缓存
     *
     * @param url URL
     * @return 缓存文件（用于判定图片是否为GIF）
     */
    File getPicDiskCache(String url);

    /**
     * 长按图片事件
     *
     * @param v      长按控件
     * @param mUrl   url
     * @param bitmap 图片
     * @return 是否处理
     */
    boolean onLongClick(View v, String mUrl, Bitmap bitmap);

    /**
     * 获取内存中的缓存
     *
     * @param url url
     * @return 图片
     */
    Bitmap getPreviewBitmap(String url);

    /**
     * Fragment创建完成回调（用于添加自定义View）
     *
     * @param view Fragment View
     */
    void onViewCreated(View view);
}