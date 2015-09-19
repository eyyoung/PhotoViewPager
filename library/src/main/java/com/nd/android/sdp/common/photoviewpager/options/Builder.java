package com.nd.android.sdp.common.photoviewpager.options;

import com.nd.android.sdp.common.photoviewpager.getter.ImageGetter;

public class Builder {
    private int mDefaultPosition = 0;
    private Class<? extends ImageGetter> mImaggerClass;

    public Builder setDefaultPosition(int defaultPosition) {
        mDefaultPosition = defaultPosition;
        return this;
    }

    public Builder setImaggerClass(Class<? extends ImageGetter> imaggerClass) {
        mImaggerClass = imaggerClass;
        return this;
    }

    public PhotoViewOptions build() {
        return new PhotoViewOptions(mDefaultPosition, mImaggerClass);
    }
}