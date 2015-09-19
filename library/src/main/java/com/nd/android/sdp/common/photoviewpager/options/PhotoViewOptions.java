package com.nd.android.sdp.common.photoviewpager.options;

import com.nd.android.sdp.common.photoviewpager.getter.ImageGetter;

import java.io.Serializable;

/**
 * Created by Young on 15/9/19.
 */
public class PhotoViewOptions implements Serializable {

    private int mDefaultPosition;
    private Class<? extends ImageGetter> mImaggerClass;

    public Class<? extends ImageGetter> getImaggerClass() {
        return mImaggerClass;
    }

    public int getDefaultPosition() {
        return mDefaultPosition;
    }

    public PhotoViewOptions(int defaultPosition,
                            Class<? extends ImageGetter> imaggerClass) {
        mDefaultPosition = defaultPosition;
        mImaggerClass = imaggerClass;
    }
}
