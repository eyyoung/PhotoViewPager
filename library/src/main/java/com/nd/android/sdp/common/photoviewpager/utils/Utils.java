package com.nd.android.sdp.common.photoviewpager.utils;

import android.content.Context;

/**
 * Created by Administrator on 2015/9/22.
 */
public class Utils {

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
