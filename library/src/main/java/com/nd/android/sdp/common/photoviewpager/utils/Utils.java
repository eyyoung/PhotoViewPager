package com.nd.android.sdp.common.photoviewpager.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.Window;

/**
 * Created by Administrator on 2015/9/22.
 */
public class Utils {

    public static int getStatusBarHeightFix(Window window) {
        Rect rectangle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentViewTop - statusBarHeight;
        return titleBarHeight;
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
