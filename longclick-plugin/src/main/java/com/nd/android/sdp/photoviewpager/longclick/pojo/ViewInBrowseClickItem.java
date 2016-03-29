package com.nd.android.sdp.photoviewpager.longclick.pojo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.nd.android.sdp.photoviewpager.longclick.R;

import java.io.File;

import rx.Observable;

/**
 * The type View in browse click item.
 */
public class ViewInBrowseClickItem implements ILongClickItem {
    @Override
    public String getLable(Context context) {
        return context.getString(R.string.photo_viewpager_view_picture_in_browse);
    }

    @Override
    public Observable<Boolean> isAvailable(@NonNull Context context, @NonNull String url, @NonNull File file, @NonNull Bitmap bitmap) {
        return Observable.just(url.startsWith("http") || url.startsWith("https"));
    }

    @Override
    public void onClick(@NonNull Context context, @NonNull String imageUrl, @NonNull File file, @NonNull Bitmap bmp) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(imageUrl));
        context.startActivity(intent);
    }
}
