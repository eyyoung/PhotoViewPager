package com.nd.android.sdp.common.photoviewpager;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.ability.IExternalView;
import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListenerV2;
import com.nd.android.sdp.common.photoviewpager.callback.OnViewCreatedListenerV2;
import com.nd.android.sdp.common.photoviewpager.downloader.ExtraDownloader;

/**
 * 图片视图可选配置
 * Created by Young on 2016/7/14.
 */
public class PhotoViewOptions {

    private ImageView imageView;
    private int defaultPosition;
    private Callback callback;
    private IPhotoViewPagerConfiguration photoViewPagerConfiguration;
    private boolean disableOrigin;
    private Class<? extends IExternalView> externalView;
    private OnPictureLongClickListenerV2 onPictureLongClickListenerV2;
    private OnViewCreatedListenerV2 onViewCreatedListenerV2;
    private OnFinishListener onFinishListener;
    private ExtraDownloader extraDownload;
    private int defaultResId;

    public ImageView getImageView() {
        return imageView;
    }

    public int getDefaultPosition() {
        return defaultPosition;
    }

    public Callback getCallback() {
        return callback;
    }

    public int getDefaultResId() {
        return defaultResId;
    }

    public ExtraDownloader getExtraDownload() {
        return extraDownload;
    }

    public OnViewCreatedListenerV2 getOnViewCreatedListenerV2() {
        return onViewCreatedListenerV2;
    }

    public IPhotoViewPagerConfiguration getPhotoViewPagerConfiguration() {
        return photoViewPagerConfiguration;
    }

    public boolean isDisableOrigin() {
        return disableOrigin;
    }

    public Class<? extends IExternalView> getExternalView() {
        return externalView;
    }

    public OnPictureLongClickListenerV2 getOnPictureLongClickListenerV2() {
        return onPictureLongClickListenerV2;
    }

    public OnFinishListener getOnFinishListener() {
        return onFinishListener;
    }

    private PhotoViewOptions(Builder builder) {
        imageView = builder.imageView;
        defaultPosition = builder.defaultPosition;
        callback = builder.callback;
        photoViewPagerConfiguration = builder.photoViewPagerConfiguration;
        disableOrigin = builder.disableOrigin;
        externalView = builder.extenralView;
        onPictureLongClickListenerV2 = builder.onPictureLongClickListenerV2;
        onViewCreatedListenerV2 = builder.onViewCreatedListenerV2;
        onFinishListener = builder.onFinishListener;
        extraDownload = builder.extraDownload;
        defaultResId = builder.defaultResId;
        if (callback == null) {
            callback = new Callback() {
                @Override
                public ImageView getPreviewView(String previewUrl) {
                    return null;
                }
            };
        }
    }

    public static final class Builder {
        private ImageView imageView;
        private int defaultPosition;
        private Callback callback;
        private IPhotoViewPagerConfiguration photoViewPagerConfiguration;
        private boolean disableOrigin;
        private Class<? extends IExternalView> extenralView;
        private OnPictureLongClickListenerV2 onPictureLongClickListenerV2;
        private OnViewCreatedListenerV2 onViewCreatedListenerV2;
        private OnFinishListener onFinishListener;
        private int defaultResId;
        private ExtraDownloader extraDownload;

        public Builder() {
        }

        public Builder imageView(ImageView val) {
            imageView = val;
            return this;
        }

        public Builder onPictureLongClick(OnPictureLongClickListenerV2 val) {
            this.onPictureLongClickListenerV2 = val;
            return this;
        }

        public Builder defaultResId(int val) {
            this.defaultResId = val;
            return this;
        }

        public Builder defaultPosition(int val) {
            defaultPosition = val;
            return this;
        }

        public Builder onFinishListener(OnFinishListener onFinishListener) {
            this.onFinishListener = onFinishListener;
            return this;
        }

        public Builder callback(Callback val) {
            callback = val;
            return this;
        }

        public Builder photoViewPagerConfiguration(IPhotoViewPagerConfiguration val) {
            photoViewPagerConfiguration = val;
            return this;
        }

        public Builder extraDownloader(ExtraDownloader extraDownloader) {
            this.extraDownload = extraDownloader;
            return this;
        }

        public Builder disableOrigin(boolean val) {
            disableOrigin = val;
            return this;
        }

        public Builder externalView(Class<? extends IExternalView> extenralView) {
            this.extenralView = extenralView;
            return this;
        }

        public Builder onViewCreatedListener(OnViewCreatedListenerV2 onViewCreatedListenerV2) {
            this.onViewCreatedListenerV2 = onViewCreatedListenerV2;
            return this;
        }

        public PhotoViewOptions build() {
            return new PhotoViewOptions(this);
        }
    }

    /**
     * 创建默认
     */
    @NonNull
    public static PhotoViewOptions createDefault() {
        return new PhotoViewOptions.Builder().build();
    }
}
