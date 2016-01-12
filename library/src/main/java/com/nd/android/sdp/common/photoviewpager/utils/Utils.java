package com.nd.android.sdp.common.photoviewpager.utils;

import android.graphics.Rect;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;

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

    public static Observable<Pair<Integer, File>> download(final String url, final File file) {
        return Observable.just(url)
                .flatMap(new Func1<String, Observable<Pair<Integer, File>>>() {
                    @Override
                    public Observable<Pair<Integer, File>> call(String s) {
                        return Observable.create(new Observable.OnSubscribe<Pair<Integer, File>>() {
                            @Override
                            public void call(final Subscriber<? super Pair<Integer, File>> subscriber) {
                                Request request = new Request.Builder().url(url).build();
                                final OkHttpClient okHttpClient = new OkHttpClient();
                                Call call = okHttpClient.newCall(request);
                                try {
                                    Response response = call.execute();
                                    if (response.code() == 200) {
                                        InputStream inputStream = null;
                                        OutputStream outputStream = null;
                                        try {
                                            File tempFile = new File(file.getParent(), file.getName() + "_temp");
                                            if (tempFile.exists()) {
                                                final boolean delete = tempFile.delete();
                                                if (!delete) {
                                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new IOException(), url));
                                                }
                                            }
                                            if (file.exists()) {
                                                final boolean delete = file.delete();
                                                if (!delete) {
                                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new IOException(), url));
                                                }
                                            }
                                            inputStream = response.body().byteStream();
                                            outputStream = new FileOutputStream(tempFile);
                                            byte[] buff = new byte[1024 * 4];
                                            long downloaded = 0;
                                            long target = response.body().contentLength();
                                            final Pair<Integer, File> filePair = new Pair<>(0, tempFile);
                                            subscriber.onNext(filePair);
                                            while (true) {
                                                int readed = inputStream.read(buff);
                                                if (readed == -1) {
                                                    break;
                                                }
                                                downloaded += readed;
                                                outputStream.write(buff, 0, readed);
                                                final Pair<Integer, File> filePairProgress = new Pair<>(((int) (downloaded * 100 / target)), tempFile);
                                                subscriber.onNext(filePairProgress);
                                            }
                                            if (downloaded == target) {
                                                outputStream.flush();
                                                final boolean result = tempFile.renameTo(file);
                                                if (!result) {
                                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new IOException(), url));
                                                }
                                                subscriber.onCompleted();
                                            } else {
                                                throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new Throwable("Http Error"), url));
                                            }
                                        } catch (IOException io) {
                                            throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, url));
                                        } finally {
                                            if (inputStream != null) {
                                                inputStream.close();
                                            }
                                            if (outputStream != null) {
                                                outputStream.close();
                                            }
                                        }
                                    } else {
                                        throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new Throwable("Http Error"), url));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(e, url));
                                }
                            }
                        });
                    }
                }).throttleLast(500, TimeUnit.MILLISECONDS);
    }
}
