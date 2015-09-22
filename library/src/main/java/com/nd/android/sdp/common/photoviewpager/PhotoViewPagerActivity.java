package com.nd.android.sdp.common.photoviewpager;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.options.PhotoViewOptions;

import java.util.ArrayList;

public class PhotoViewPagerActivity extends AppCompatActivity {

    /**
     * URL列表，支持本地路径与URL路径
     */
    public static final String PARAM_URLS = "urls";
    private static final String PARAM_PREVIEW_URLS = "preview_urls";
    /**
     * 现实选项
     */
    public static final String PARAM_PHOTO_OPTIONS = "options";
    public static final String PARAM_TOP = "top";
    public static final String PARAM_LEFT = "left";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_HEIGHT = "height";

    private Toolbar mToolBar;
    private PhotoViewPager mVpPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.photo_viewpager_activity);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        init();
    }

    private void init() {
        mVpPhoto = (PhotoViewPager) findViewById(R.id.vpPhoto);

        final Intent intent = this.getIntent();
        ArrayList<String> images = intent.getStringArrayListExtra(PARAM_URLS);
        ArrayList<String> previewImgs = intent.getStringArrayListExtra(PARAM_PREVIEW_URLS);
        mVpPhoto.init(images,
                previewImgs,
                (PhotoViewOptions) intent.getSerializableExtra(PARAM_PHOTO_OPTIONS));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_photo_view_pager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start
     *
     * @param context the context
     * @author Young
     */
    public static void start(Activity context,
                             ImageView imageView,
                             ArrayList<String> urls,
                             ArrayList<String> previewUrls,
                             PhotoViewOptions photoViewOptions) {
        Intent intent = new Intent(context, PhotoViewPagerActivity.class);
        intent.putExtra(PARAM_URLS, urls);
        intent.putExtra(PARAM_PREVIEW_URLS, previewUrls);
        intent.putExtra(PARAM_PHOTO_OPTIONS, photoViewOptions);
        int[] locations = new int[2];
        imageView.getLocationOnScreen(locations);
        intent.putExtra(PARAM_LEFT, locations[0]);
        intent.putExtra(PARAM_TOP, locations[1]);
        intent.putExtra(PARAM_WIDTH, imageView.getWidth());
        intent.putExtra(PARAM_HEIGHT, imageView.getHeight());
        context.startActivity(intent);
        context.overridePendingTransition(0, 0);
    }
}
