package com.nd.android.sdp.common.photoviewpager.getter;

import android.graphics.Bitmap;

/**
 * Created by Young on 15/9/19.
 */
public interface ImageGetter {


    void startGetImage(String url, ImageGetterCallback imageGetterCallback);

    Bitmap getPreviewImage(String previewUrl);
}
