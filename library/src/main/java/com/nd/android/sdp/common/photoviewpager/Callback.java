package com.nd.android.sdp.common.photoviewpager;

import android.widget.ImageView;

public interface Callback {
    /**
     * 获取预览控件
     *
     * @param url url
     * @return 预览控件
     */
    ImageView getPreviewView(String url);

}