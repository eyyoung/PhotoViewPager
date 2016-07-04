package com.nd.android.sdp.photoviewpager.longclick.pojo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nd.android.sdp.common.photoviewpager.utils.Utils;
import com.nd.android.sdp.photoviewpager.longclick.R;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * The type System share click item.
 */
public class SystemShareClickItem implements ILongClickItem {
    @Override
    public String getLable(Context context) {
        return context.getString(R.string.photo_viewpager_share);
    }

    @Override
    public Observable<Boolean> isAvailable(@NonNull Context context,
                                           @NonNull String url,
                                           @NonNull File file,
                                           @Nullable Bitmap bitmap) {
        return Observable.just(file.exists());
    }

    @Override
    public void onClick(@NonNull final Context context, @NonNull String imageUrl, @NonNull final File file, @Nullable Bitmap bmp) {
        Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                File target = file;
                String name = file.getName();
                String absolutePath = file.getAbsolutePath();
                if (!name.contains(".")) {
                    if (Utils.isGifFile(absolutePath)) {
                        target = new File(absolutePath + ".gif");
                    } else {
                        target = new File(absolutePath + ".jpg");
                    }
                    Utils.copyfile(file, target, false);
                }
                subscriber.onNext(target);
                subscriber.onCompleted();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<File>() {
                    @Override
                    public void call(File file) {
                        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                        intent.setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.photo_viewpager_share)));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }
}
