package com.nd.android.sdp.photoviewpager.longclick.pojo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.utils.Utils;
import com.nd.android.sdp.photoviewpager.longclick.R;

import java.io.File;

import rx.Observable;

/**
 * The type Save click item.
 */
public class SaveClickItem implements ILongClickItem {

    @Override
    public String getLable(Context context) {
        return context.getString(R.string.photo_viewpager_save);
    }

    @Override
    public Observable<Boolean> isAvailable(@NonNull Context context, @NonNull String url, @NonNull File file, @Nullable Bitmap bitmap) {
        return Observable.just(file.exists());
    }

    @Override
    public void onClick(@NonNull Context context, @NonNull String imageUrl, @NonNull File inCache, @Nullable Bitmap bmp) {
        if (!inCache.exists()) {
            Toast.makeText(context, R.string.photo_viewpager_save_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        final File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String ext;
        if (Utils.isGifFile(inCache.getAbsolutePath())) {
            ext = ".gif";
        } else {
            ext = ".jpg";
        }
        File desFile = new File(directory, inCache.getName() + ext);
        Utils.copyfile(inCache, desFile, true);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(desFile);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
        Toast.makeText(context, R.string.photo_viewpager_save_completed, Toast.LENGTH_LONG).show();
    }
}
