package com.nd.android.sdp.common.photoviewpager.utils;

import android.content.Context;
import android.view.View;

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

    public static float sin(double degree) {
        return (float) Math.sin(Math.toRadians(degree));
    }

    public static float cos(double degree) {
        return (float) Math.cos(Math.toRadians(degree));
    }

    public static float asin(double value) {
        return (float) Math.toDegrees(Math.asin(value));
    }

    public static float acos(double value) {
        return (float) Math.toDegrees(Math.acos(value));
    }

    public static float centerX(View view) {
        return view.getX() + view.getWidth() / 2;
    }

    public static float centerY(View view) {
        return view.getY() + view.getHeight() / 2;
    }

}
