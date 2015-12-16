package com.nd.android.sdp.common.photoviewpager.menu;

import android.view.View;

/**
 * 底部菜单接口
 */
public interface IBottomMenu {

    int getResIcon();

    View.OnClickListener getOnClickListener(String url);

}
