package com.nd.android.sdp.photoviewpager.longclick.pojo;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.nd.android.sdp.photoviewpager.longclick.R;
import com.nd.smartcan.appfactory.AppFactory;
import com.nd.smartcan.appfactory.nativejs.util.MapScriptable;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * QR detect click item.
 */
public class QRDetectClickItem implements ILongClickItem {

    /**
     * 参数
     */
    private static final String KEY_IMAGE = "image";
    public static final String KEY_IS_QRCODE = "isQrcode";
    /**
     * 触发识别二维码的动作
     */
    private static final String EVENT_TRIGGER_IDENTIFY_QRCODE = "qrcode_detect";
    /**
     * 触发执行二维码的动作
     */
    private static final String EVENT_TRIGGER_DECODE_QRCODE = "qrcode_decode";


    @Override
    public String getLable(Context context) {
        return context.getString(R.string.photo_viewpager_detect_qr_code);
    }

    @Override
    public Observable<Boolean> isAvailable(@NonNull final Context context,
                                           @NonNull String url,
                                           @NonNull File file, @NonNull final Bitmap bitmap) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    MapScriptable param = new MapScriptable();
                    param.put(KEY_IMAGE, bitmap);
                    final MapScriptable[] mapScriptables = AppFactory.instance().triggerEventSync(context, EVENT_TRIGGER_IDENTIFY_QRCODE, param);
                    boolean isQrCode = false;
                    if (mapScriptables != null && mapScriptables.length > 0) {
                        MapScriptable mapScriptable = mapScriptables[0];
                        isQrCode = false;
                        if (mapScriptable.containsKey(KEY_IS_QRCODE)) {
                            isQrCode = (boolean) mapScriptable.get(KEY_IS_QRCODE);
                        }
                    }
                    subscriber.onNext(isQrCode);
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void onClick(@NonNull Context context, @NonNull String imageUrl, @NonNull File file, @NonNull Bitmap bmp) {
        MapScriptable param = new MapScriptable();
        param.put(KEY_IMAGE, bmp);
        AppFactory.instance().triggerEvent(context, EVENT_TRIGGER_DECODE_QRCODE, param);
    }
}
