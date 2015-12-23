package com.nd.android.sdp.common.photoviewpager.callback;

import android.view.View;

/**
 * Created by Administrator on 2015/12/23.
 */
public interface OnViewCreatedListener {

    /**
     * Fragment创建完成回调（用于添加自定义View）
     *
     * @param view Fragment View
     */
    void onViewCreated(View view);

}
