package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

public interface Callback {
    /**
     * 获取预览控件
     *
     * @param url url
     * @return 预览控件
     */
    ImageView getPreviewView(String url);

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
     * Fragment创建完成回调（用于添加自定义View）
     *
     * @param view Fragment View
     */
    void onViewCreated(View view);
}