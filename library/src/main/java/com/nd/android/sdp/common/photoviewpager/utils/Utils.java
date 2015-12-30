package com.nd.android.sdp.common.photoviewpager.utils;

import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    /**
     * 判断某个图片是否GIF格式
     * <br>Created 2014-3-24下午2:10:14
     *
     * @param path 图片路径
     * @return 图片是GIF格式返回true, 否则返回false
     * @author cb
     */
    public static boolean isGifFile(String path) {
        final int HEAD_COUNT = 3; //gif扩展名的长度
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        boolean isGif = false;
        InputStream stream = null;
        try {
            stream = new FileInputStream(path);
            byte[] head = new byte[HEAD_COUNT];
            stream.read(head);
            String imgType = new String(head);
            isGif = imgType.equalsIgnoreCase("gif");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return isGif;
    }

}
