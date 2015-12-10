package com.nd.android.sdp.common.photoviewpager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.nd.android.sdp.common.photoviewpager.options.PhotoViewOptions;

import java.util.ArrayList;

/**
 * 图片浏览控件
 *
 * @author Young
 */
public class PhotoViewPager extends ViewPager {

    private ArrayList<String> mUrls;
    private ImagePagerAdapter mImagePagerAdapter;
    private PhotoViewOptions mPhotoViewOptions;
    private ArrayList<String> mPreviewImgs;
    private Bundle mArguments;

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
                     Bundle arguments, PhotoViewOptions photoViewOptions) {
        mUrls = images;
        mPreviewImgs = previewImgs;
        mArguments = arguments;

        mImagePagerAdapter = new ImagePagerAdapter(((FragmentActivity) getContext())
                .getSupportFragmentManager());
        setAdapter(mImagePagerAdapter);

        mPhotoViewOptions = photoViewOptions;
        setCurrentItem(photoViewOptions.getDefaultPosition());
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            ViewPagerFragment fragment = ViewPagerFragment.newInstance(mPhotoViewOptions.getImaggerClass(), mArguments);
            fragment.setUrl(mUrls.get(position));
            fragment.setPreviewUrl(mPreviewImgs.get(position));
            if (position == mPhotoViewOptions.getDefaultPosition()) {
                fragment.startDefaultTransition();
            }
            return fragment;
        }


        @Override
        public int getCount() {
            return mUrls.size();
        }
    }
}
