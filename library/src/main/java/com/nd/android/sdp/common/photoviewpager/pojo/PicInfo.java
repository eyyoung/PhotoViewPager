package com.nd.android.sdp.common.photoviewpager.pojo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nd.android.sdp.common.photoviewpager.BasePagerFragment;
import com.nd.android.sdp.common.photoviewpager.PhotoPagerFragment;
import com.nd.android.sdp.common.photoviewpager.downloader.ExtraDownloader;

/**
 * 图片信息
 */
public class PicInfo implements Parcelable, Info {

    @NonNull
    public String url;
    @NonNull
    public String previewUrl;
    @Nullable
    public String origUrl;
    @Nullable
    public String md5;
    public long size;

    @Deprecated
    public PicInfo(@NonNull String url,
                   @NonNull String previewUrl,
                   @Nullable String origUrl,
                   long size) {
        this(url, previewUrl, origUrl, size, null);
    }

    @Deprecated
    public PicInfo(@NonNull String url,
                   @NonNull String previewUrl,
                   @Nullable String origUrl,
                   long size,
                   @Nullable
                   String md5) {
        this.url = url;
        this.previewUrl = previewUrl;
        this.origUrl = origUrl;
        this.size = size;
        this.md5 = md5;
    }

    protected PicInfo(Parcel in) {
        url = in.readString();
        previewUrl = in.readString();
        origUrl = in.readString();
        size = in.readLong();
        md5 = in.readString();
    }

    private PicInfo(Builder builder) {
        url = builder.url;
        previewUrl = builder.previewUrl;
        origUrl = builder.origUrl;
        md5 = builder.md5;
        size = builder.size;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(previewUrl);
        dest.writeString(origUrl);
        dest.writeLong(size);
        dest.writeString(md5);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PicInfo> CREATOR = new Creator<PicInfo>() {
        @Override
        public PicInfo createFromParcel(Parcel in) {
            return new PicInfo(in);
        }

        @Override
        public PicInfo[] newArray(int size) {
            return new PicInfo[size];
        }
    };

    @NonNull
    @Override
    public String getPreviewUrl() {
        return previewUrl;
    }

    @NonNull
    @Override
    public String getUrl() {
        return url;
    }

    @Nullable
    @Override
    public String getOrigUrl() {
        return origUrl;
    }

    @Override
    public BasePagerFragment getFragment(Bundle bundle,
                                         ExtraDownloader extraDownloader) {
        BasePagerFragment basePagerFragment = PhotoPagerFragment.newPhotoInstance(bundle);
        basePagerFragment.setInfo(this);
        return basePagerFragment;
    }

    public static final class Builder {
        private String url;
        private String previewUrl;
        @Nullable
        private String origUrl;
        @Nullable
        private String md5;
        private long size;

        private Builder() {
        }

        @NonNull
        public Builder url(@NonNull String val) {
            url = val;
            return this;
        }

        @NonNull
        public Builder previewUrl(@NonNull String val) {
            previewUrl = val;
            return this;
        }

        @NonNull
        public Builder origUrl(@Nullable String val) {
            origUrl = val;
            return this;
        }

        @NonNull
        public Builder md5(@Nullable String val) {
            md5 = val;
            return this;
        }

        @NonNull
        public Builder size(long val) {
            size = val;
            return this;
        }

        @NonNull
        public PicInfo build() {
            return new PicInfo(this);
        }
    }
}
