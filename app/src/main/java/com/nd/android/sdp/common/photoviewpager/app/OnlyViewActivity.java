package com.nd.android.sdp.common.photoviewpager.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerFragment;
import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerManager;
import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;

import java.util.ArrayList;

public class OnlyViewActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, OnlyViewActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_only_view);
        ArrayList<Info> infos = new ArrayList<>();
        final PicInfo picInfo = PicInfo.newBuilder()
                .md5(null)
                .url("http://ww2.sinaimg.cn/bmiddle/6f9303b5gw1ezhod4l65wj20pe0zkjxq.jpg")
                .previewUrl("http://ww2.sinaimg.cn/bmiddle/6f9303b5gw1ezhod4l65wj20pe0zkjxq.jpg")
                .build();
        infos.add(picInfo);
        final PhotoViewPagerFragment view = PhotoViewPagerManager.getView(infos, 0);
        view.setNoBgAnim();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, view)
                .commit();
    }

}
