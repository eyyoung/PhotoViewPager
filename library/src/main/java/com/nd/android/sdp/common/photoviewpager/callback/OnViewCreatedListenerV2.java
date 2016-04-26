package com.nd.android.sdp.common.photoviewpager.callback;

import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2015/12/23.
 */
public interface OnViewCreatedListenerV2 {

    /**
     * Fragment创建完成回调（用于添加自定义View）
     */
    void onViewCreated(RelativeLayout viewGroup);

}
