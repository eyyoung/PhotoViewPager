package com.nd.android.sdp.common.photoviewpager.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 图片信息
 */
public class PicInfo implements Parcelable {

    @NonNull
    public String url;
    @NonNull
    public String previewUrl;
    @Nullable
    public String origUrl;
    public boolean isVideo;
    public long size;

    public PicInfo(@NonNull String url,
                   @NonNull String previewUrl,
                   @Nullable String origUrl,
                   long size) {
        this(url, previewUrl, origUrl, size, false);
    }

    public PicInfo(@NonNull String url,
                   @NonNull String previewUrl,
                   @Nullable String origUrl,
                   long size,
                   boolean isVideo) {
        this.url = url;
        this.previewUrl = previewUrl;
        this.origUrl = origUrl;
        this.size = size;
        this.isVideo = isVideo;
    }

    protected PicInfo(Parcel in) {
        url = in.readString();
        previewUrl = in.readString();
        origUrl = in.readString();
        size = in.readLong();
        isVideo = in.readInt() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(previewUrl);
        dest.writeString(origUrl);
        dest.writeLong(size);
        dest.writeInt(isVideo ? 1 : 0);
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
}
