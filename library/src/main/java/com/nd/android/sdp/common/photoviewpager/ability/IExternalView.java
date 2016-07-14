package com.nd.android.sdp.common.photoviewpager.ability;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerFragment;

/**
 * 附加视图层
 * Created by Young on 2016/7/14.
 */
public interface IExternalView {

    /**
     * 获取视图叠加层
     *
     * @param context  上下文
     * @param fragment Fragment
     * @return
     */
    @NonNull
    View getView(Context context, PhotoViewPagerFragment fragment, Bundle args);

}
