package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;

import java.io.File;

public interface Callback {
    ImageView getPreviewView(String url);

    void startGetImage(String url, ImageGetterCallback imageGetterCallback);

    File getFullsizePicDiskCache(String url);

    boolean onLongClick(View v, String mUrl, Bitmap bitmap);

    Bitmap getPreviewBitmap(String url);
}