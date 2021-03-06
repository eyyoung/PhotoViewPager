package com.nd.android.sdp.common.photoviewpager.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.Callback;
import com.nd.android.sdp.common.photoviewpager.PhotoViewOptions;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nd.android.sdp.common.photoviewpager.iml.ImageLoaderIniter;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;
import com.nd.android.sdp.photoviewpager.longclick.PluginPictureLongClickListener;
import com.nd.android.sdp.photoviewpager.longclick.pojo.SaveClickItem;
import com.nd.android.sdp.photoviewpager.longclick.pojo.SystemShareClickItem;
import com.nd.android.sdp.photoviewpager.longclick.pojo.ViewInBrowseClickItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Callback {

    String[] urls = new String[]{
            "http://betacs.101.com/v0.1/download?dentryId=bd554eb7-fd48-407e-a834-9a3d903a0314&size=960",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
    };

    String[] preview_urls = new String[]{
            "http://betacs.101.com/v0.1/download?dentryId=bd554eb7-fd48-407e-a834-9a3d903a0314&size=160",
            "http://ww1.sinaimg.cn/bmiddle/6c7cbd31jw1ew7ibh0e7qj21kw11xe58.jpg",
    };
    private ImageView mIv;
    private ImageView mIv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ImageLoaderIniter.INSTANCE.init();
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            ListActivity.start(this);
            return true;
        }


        if (id == R.id.action_only_view) {
            OnlyViewActivity.start(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public ImageView getPreviewView(String previewUrl) {
        if (previewUrl.equals(preview_urls[0])) {
            return mIv;
        } else {
            return mIv2;
        }
    }

    public void onCustomView(View view) {
        ArrayList<PicInfo> picInfos = new ArrayList<>();
        for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
            PicInfo picInfo = new PicInfo(urls[i],
                    preview_urls[i],
                    null,
                    110
            );
            picInfos.add(picInfo);
        }
        Bundle bundle = new Bundle();
        bundle.putString("TEST", "TEST");
        PhotoViewOptions viewOptions = new PhotoViewOptions.Builder()
                .callback(this)
                .externalView(CustomView.class)
                .imageView((ImageView) view)
                .externalViewArg(bundle)
                .build();
        PhotoViewPagerManager.startView(this,
                picInfos, viewOptions);
    }

    public void onCustomLongClick(View view) {
        ArrayList<PicInfo> picInfos = new ArrayList<>();
        for (int i = 0, urlsLength = urls.length; i < urlsLength; i++) {
            PicInfo picInfo = new PicInfo(urls[i],
                    preview_urls[i],
                    null,
                    110
            );
            picInfos.add(picInfo);
        }
        final PluginPictureLongClickListener longClickListener = new PluginPictureLongClickListener.Builder()
                .addLongClickItem(new ViewInBrowseClickItem())
                .addLongClickItem(new SaveClickItem())
                .addLongClickItem(new SystemShareClickItem())
                .build();
        PhotoViewOptions viewOptions = new PhotoViewOptions.Builder()
                .callback(this)
                .defaultPosition(1)
                .onPictureLongClick(longClickListener)
                .imageView((ImageView) view)
                .build();
        PhotoViewPagerManager.startView(this,
                picInfos, viewOptions);
    }
}

