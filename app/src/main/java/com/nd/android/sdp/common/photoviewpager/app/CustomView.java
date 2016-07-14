package com.nd.android.sdp.common.photoviewpager.app;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nd.android.sdp.common.photoviewpager.PhotoViewPagerFragment;
import com.nd.android.sdp.common.photoviewpager.ability.IExternalView;

/**
 * 自定义附加图层
 * Created by Young on 2016/7/14.
 */
public class CustomView implements IExternalView, ViewPager.OnPageChangeListener {

    private View mView;
    private PhotoViewPagerFragment mFragment;
    private TextView mTvCurrentPage;

    @NonNull
    @Override
    public View getView(Context context, PhotoViewPagerFragment fragment) {
        mFragment = fragment;
        LayoutInflater from = LayoutInflater.from(context);
        mView = from.inflate(R.layout.view_custom, null);
        mTvCurrentPage = (TextView) mView.findViewById(R.id.tvCurrentPage);
        fragment.addOnPageChangeListener(this);
        return mView;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        mTvCurrentPage.setText(String.valueOf(i));
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
}
