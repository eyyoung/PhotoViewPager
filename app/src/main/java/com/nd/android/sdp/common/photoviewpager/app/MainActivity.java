package com.nd.android.sdp.common.photoviewpager.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerFragment;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements PhotoViewPagerFragment.Callback {

    String[] urls = new String[]{
            "http://ww4.sinaimg.cn/bmiddle/6106a4f0gw1ez18sesw2aj20r80r8juz.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg"
    };

    String[] preview_urls = new String[]{
            "http://ww4.sinaimg.cn/bmiddle/6106a4f0gw1ez18sesw2aj20r80r8juz.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg"
    };
    private ImageView mIv;
    private ImageView mIv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(20 * 1024 * 1024)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        mIv = ((ImageView) findViewById(R.id.iv));
        mIv2 = ((ImageView) findViewById(R.id.iv2));
        ImageLoader.getInstance().displayImage(preview_urls[0], mIv, displayImageOptions);
        ImageLoader.getInstance().displayImage(preview_urls[1], mIv2, displayImageOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toView(View view) {
        PhotoViewPagerFragment.start(this,
                (ImageView) view,
                new ArrayList<>(Arrays.asList(urls)),
                new ArrayList<>(Arrays.asList(preview_urls)),
                mIv == view ? 0 : 1);
    }

    @Override
    public ImageView getPreviewView(String url) {
        if (url.equals(preview_urls[0])) {
            return null;
        } else {
            return mIv2;
        }
    }

    @Override
    public void startGetImage(String url, final ImageGetterCallback imageGetterCallback) {
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().loadImage(url,
                new ImageSize(1440, 2560),
                displayImageOptions, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        imageGetterCallback.setImageToView(loadedImage);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                }, new ImageLoadingProgressListener() {
                    @Override
                    public void onProgressUpdate(String imageUri, View view, int current, int total) {
                        imageGetterCallback.setProgress(current, total);
                    }
                });
    }

    @Override
    public File getFullsizePicDiskCache(String url) {
        final DiskCache diskCache = ImageLoader.getInstance().getDiskCache();
        final File file = diskCache.get(url);
        return file;
    }

    @Override
    public boolean onLongClick(View v, String mUrl, Bitmap bitmap) {
        Toast.makeText(this, mUrl, Toast.LENGTH_SHORT).show();
        return true;
    }
}

