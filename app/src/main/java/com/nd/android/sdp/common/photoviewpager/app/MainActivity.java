package com.nd.android.sdp.common.photoviewpager.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerFragment;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetter;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nd.android.sdp.common.photoviewpager.options.Builder;
import com.nd.android.sdp.common.photoviewpager.options.PhotoViewOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PhotoViewPagerFragment.Callback {

    String[] urls = new String[]{
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg"
    };

    String[] preview_urls = new String[]{
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg"
    };
    private ImageView mIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(this)
                .memoryCacheSize(20 * 1024 * 1024)
                .build();
        ImageLoader.getInstance().init(imageLoaderConfiguration);
        DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        mIv = ((ImageView) findViewById(R.id.iv));
        ImageLoader.getInstance().displayImage(preview_urls[1], mIv, displayImageOptions);
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
        PhotoViewOptions photoViewOptions = new Builder()
                .setDefaultPosition(1)
                .setImaggerClass(DemoImageGetter.class)
                .build();
        PhotoViewPagerFragment.start(this,
                (ImageView) view,
                new ArrayList<>(Arrays.asList(urls)),
                new ArrayList<>(Arrays.asList(preview_urls)),
                photoViewOptions);
    }

    @Override
    public ImageView getPreviewView(String url) {
        return mIv;
    }

    public static class DemoImageGetter implements ImageGetter {

        @Override
        public void startGetImage(String url, final ImageGetterCallback imageGetterCallback) {
            DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            ImageLoader.getInstance().loadImage(url,
                    new ImageSize(1080, 1980),
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

                        }
                    });
        }

        @Override
        public Bitmap getPreviewImage(String previewUrl) {
            final List<Bitmap> cachedBitmapsForImageUri = MemoryCacheUtils.findCachedBitmapsForImageUri(previewUrl,
                    ImageLoader.getInstance().getMemoryCache());
            if (cachedBitmapsForImageUri.size() > 0) {
                return cachedBitmapsForImageUri.get(0);
            }
            return null;
        }
    }
}
