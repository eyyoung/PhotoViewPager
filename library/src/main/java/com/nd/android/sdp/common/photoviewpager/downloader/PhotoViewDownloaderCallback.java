package com.nd.android.sdp.common.photoviewpager.downloader;

/**
 * 下载会掉
 */
public interface PhotoViewDownloaderCallback {

    /**
     * 更新进度
     *
     * @param url     url
     * @param current current
     * @param total   total
     */
    void updateProgress(String url, long current, long total);

    /**
     * 下载遭遇取消
     *
     * @param url url
     */
    void cancelDownload(String url);

    void onComplete(String url);

    void onError(String url, int httpCode);

    void onPause(String url);

    void onCancel(String url);
}
