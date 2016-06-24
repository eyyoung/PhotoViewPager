package com.nd.android.sdp.photoviewpager.longclick.pojo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nd.android.sdp.photoviewpager.longclick.R;

import java.io.File;

import rx.Observable;

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
    public void onClick(@NonNull Context context, @NonNull String imageUrl, @NonNull File file, @Nullable Bitmap bmp) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.photo_viewpager_share)));
    }
}
