package com.nd.android.sdp.common.photoviewpager.pojo;

import android.os.Bundle;
import android.os.Parcelable;

import com.nd.android.sdp.common.photoviewpager.BasePagerFragment;
import com.nd.android.sdp.common.photoviewpager.downloader.ExtraDownloader;

/**
 * 抽象接口
 */
public interface Info extends Parcelable {

    String getPreviewUrl();

    String getUrl();

    String getOrigUrl();

    BasePagerFragment getFragment(Bundle bundle,
                                  ExtraDownloader extraDownloader);

}
