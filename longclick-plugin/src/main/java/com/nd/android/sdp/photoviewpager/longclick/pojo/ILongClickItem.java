package com.nd.android.sdp.photoviewpager.longclick.pojo;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.File;

import rx.Observable;

/**
 * 长按菜单接口
 */
public interface ILongClickItem {

    /**
     * 菜单标题
     *
     * @param context context
     * @return 标题
     */
    String getLable(Context context);

    /**
     * 是否可用
     *
     *
     * @param context
     * @param url    图片url
     * @param file
     *@param bitmap bitmap  @return 可用流
     */
    Observable<Boolean> isAvailable(@NonNull Context context, @NonNull String url, @NonNull File file, @NonNull Bitmap bitmap);

    /**
     * 图片点击
     *
     * @param context  context
     * @param imageUrl 图片完整URL
     * @param file     图片缓存文件
     * @param bmp      图片BMP
     */
    void onClick(@NonNull Context context,
                 @NonNull String imageUrl,
                 @NonNull File file,
                 @NonNull Bitmap bmp);

}
