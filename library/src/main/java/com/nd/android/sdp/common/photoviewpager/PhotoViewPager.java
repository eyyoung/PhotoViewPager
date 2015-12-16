package com.nd.android.sdp.common.photoviewpager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * 图片浏览控件
 *
 * @author Young
 */
public class PhotoViewPager extends ViewPager {

    private ArrayList<String> mUrls;
    private ImagePagerAdapter mImagePagerAdapter;
    private ArrayList<String> mPreviewImgs;
    private Bundle mArguments;
    private int mDefaultPosition;
    private SparseArray<ViewPagerFragment> mFragmentMap = new SparseArray<>();

    public PhotoViewPager(Context context) {
        super(context);
        if (!(context instanceof FragmentActivity)) {
            throw new IllegalArgumentException();
        }
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!(context instanceof FragmentActivity)) {
            throw new IllegalArgumentException();
        }
    }

    public void init(ArrayList<String> images,
                     ArrayList<String> previewImgs,
                     Bundle arguments, int defaultPosition) {
        mUrls = images;
        mPreviewImgs = previewImgs;
        mArguments = arguments;
        mDefaultPosition = defaultPosition;

        mImagePagerAdapter = new ImagePagerAdapter(((FragmentActivity) getContext())
                .getSupportFragmentManager());
        setAdapter(mImagePagerAdapter);

        setCurrentItem(defaultPosition);
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            ViewPagerFragment fragment = ViewPagerFragment.newInstance(mArguments);
            mFragmentMap.put(position, fragment);
            fragment.setUrl(mUrls.get(position));
            fragment.setPreviewUrl(mPreviewImgs.get(position));
            if (position == mDefaultPosition) {
                fragment.startDefaultTransition();
            }
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mFragmentMap.remove(position);
        }

        @Override
        public int getCount() {
            return mUrls.size();
        }
    }

    public ViewPagerFragment getFragmentByPosition(int position) {
        return mFragmentMap.get(position);
    }

}
