package com.nd.android.sdp.common.photoviewpager.getter;

import android.graphics.Bitmap;

/**
 * Created by Young on 15/9/19.
 */
public interface ImageGetterCallback {

    void setImageToView(Bitmap bitmap);

    void setProgress(long current, long total);
}
