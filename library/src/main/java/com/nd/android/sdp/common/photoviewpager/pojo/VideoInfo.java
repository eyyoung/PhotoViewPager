package com.nd.android.sdp.common.photoviewpager.pojo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.nd.android.sdp.common.photoviewpager.BasePagerFragment;
import com.nd.android.sdp.common.photoviewpager.VideoPagerFragment;
import com.nd.android.sdp.common.photoviewpager.downloader.ExtraDownloader;

/**
 * 视频信息
 *
 * @author Young
 */
public class VideoInfo implements Parcelable, Info {

    @NonNull
    public String thumb;
    @NonNull
    public String bigthumb;
    @NonNull
    public String videoUrl;
    @Nullable
    public String md5;
    public long size;

    private VideoInfo(Builder builder) {
        thumb = builder.thumb;
        videoUrl = builder.videoUrl;
        bigthumb = builder.bigthumb;
        md5 = builder.md5;
        size = builder.size;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String getPreviewUrl() {
        return thumb;
    }

    @Override
    public String getUrl() {
        return TextUtils.isEmpty(bigthumb) ? thumb : bigthumb;
    }

    @Override
    public String getOrigUrl() {
        return videoUrl;
    }

    @Override
    public BasePagerFragment getFragment(Bundle bundle,
                                         ExtraDownloader extraDownloader) {
        VideoPagerFragment videoPagerFragment = VideoPagerFragment.newVideoInstance(bundle);
        videoPagerFragment.setExtraDownloader(extraDownloader);
        videoPagerFragment.setInfo(this);
        return videoPagerFragment;
    }

    public static final class Builder {
        private String thumb;
        private String videoUrl;
        private String md5;
        private long size;
        private String bigthumb;

        private Builder() {
        }

        @NonNull
        public Builder thumb(@NonNull String val) {
            thumb = val;
            return this;
        }

        @NonNull
        public Builder videoUrl(@NonNull String val) {
            videoUrl = val;
            return this;
        }

        @NonNull
        public Builder md5(@NonNull String val) {
            md5 = val;
            return this;
        }

        @NonNull
        public Builder size(long val) {
            size = val;
            return this;
        }

        @NonNull
        public Builder bigthumb(@NonNull String val){
            bigthumb = val;
            return this;
        }

        @NonNull
        public VideoInfo build() {
            return new VideoInfo(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.thumb);
        dest.writeString(this.bigthumb);
        dest.writeString(this.videoUrl);
        dest.writeString(this.md5);
        dest.writeLong(this.size);
    }

    protected VideoInfo(Parcel in) {
        this.thumb = in.readString();
        this.bigthumb = in.readString();
        this.videoUrl = in.readString();
        this.md5 = in.readString();
        this.size = in.readLong();
    }

    public static final Creator<VideoInfo> CREATOR = new Creator<VideoInfo>() {
        public VideoInfo createFromParcel(Parcel source) {
            return new VideoInfo(source);
        }

        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }
    };
}
