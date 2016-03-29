package com.nd.android.sdp.common.photoviewpager.callback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.io.File;

/**
 * OnPictureLongClickListenerV2
 */
public interface OnPictureLongClickListenerV2 {

    /**
     * 长按图片事件
     *
     * @param v     长按控件
     * @param mUrl  url
     * @param cache 图片缓存
     * @return 是否处理
     */
    boolean onLongClick(@NonNull View v, @NonNull String mUrl,
                        @Nullable File cache);

}
