package com.nd.android.sdp.common.photoviewpager.callback;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Administrator on 2015/12/23.
 */
public interface OnPictureLongClickListener {

    /**
     * 长按图片事件
     *
     * @param v      长按控件
     * @param mUrl   url
     * @param bitmap 图片
     * @return 是否处理
     */
    boolean onLongClick(View v, String mUrl, Bitmap bitmap);

}
