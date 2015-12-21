package com.nd.android.sdp.common.photoviewpager.getter;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Young on 15/9/19.
 */
public interface ImageGetterCallback {

    void setImageToView(Bitmap bitmap);

    void setProgress(long current, long total);

    void error(String imageUri, View view, Throwable cause);
}
