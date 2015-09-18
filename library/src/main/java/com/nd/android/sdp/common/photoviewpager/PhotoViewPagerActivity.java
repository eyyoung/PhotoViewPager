package com.nd.android.sdp.common.photoviewpager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class PhotoViewPagerActivity extends AppCompatActivity {

    /**
     * URL列表，支持本地路径与URL路径
     */
    public static final String PARAM_URLS = "urls";
    /**
     * 初始位置
     */
    public static final String PARAM_POSITION = "position";

    private Toolbar mToolBar;
    private PhotoViewPager mVpPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view_pager);
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
        mVpPhoto.init(images, intent.getIntExtra(PARAM_POSITION, 0));

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
    public static void start(Context context, ArrayList<String> urls, int position) {
        Intent intent = new Intent(context, PhotoViewPagerActivity.class);
        intent.putExtra(PARAM_URLS, urls);
        intent.putExtra(PARAM_POSITION, position);
        context.startActivity(intent);
    }
}
