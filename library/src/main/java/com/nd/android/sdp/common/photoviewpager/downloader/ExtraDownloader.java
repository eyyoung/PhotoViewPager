package com.nd.android.sdp.common.photoviewpager.downloader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by Administrator on 2016/1/13.
 */
public interface ExtraDownloader {

    void startDownload(
            @NonNull
            String url,
            @NonNull
            File file,
            @Nullable
            String md5,
            @NonNull
            PhotoViewDownloaderCallback photoViewDownloaderCallback);

    void cancelCallBack(
            @NonNull
            String url);

    void confirmDownload(PhotoViewConfirmDownloadCallback callback);

}
