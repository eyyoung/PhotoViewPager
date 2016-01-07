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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;

import java.util.ArrayList;

/**
 * 图片浏览控件
 *
 * @author Young
 */
class PhotoViewPager extends ViewPager {

    private ArrayList<PicInfo> mPicInfos;
    private Bundle mArguments;
    private int mDefaultPosition;
    private SparseArray<ViewPagerFragment> mFragmentMap = new SparseArray<>();
    private Callback mCallback;
    private View mBg;
    private OnPictureLongClickListener mOnPictureLongClickListener;
    private OnFinishListener mOnFinishListener;
    private OnClickListener mOnPictureClickListener;
    private IPhotoViewPagerConfiguration mConfiguration;

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

    public void init(FragmentManager fragmentManager, ArrayList<PicInfo> images,
            Bundle arguments, int defaultPosition) {
        mPicInfos = images;
        mArguments = arguments;
        mDefaultPosition = defaultPosition;
        ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(fragmentManager);
        setAdapter(imagePagerAdapter);
        setCurrentItem(defaultPosition);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setBg(View bg) {
        mBg = bg;
    }

    public void setOnPictureLongClickListener(OnPictureLongClickListener onPictureLongClickListener) {
        mOnPictureLongClickListener = onPictureLongClickListener;
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
    }

    public void setOnPictureClickListener(OnClickListener onPictureClickListener) {
        mOnPictureClickListener = onPictureClickListener;
    }

    public void setConfigration(IPhotoViewPagerConfiguration configuration) {
        mConfiguration = configuration;
    }

    private class ImagePagerAdapter extends FragmentStatePagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            ViewPagerFragment fragment = ViewPagerFragment.newInstance(mArguments);
            mFragmentMap.put(position, fragment);
            fragment.setBg(mBg);
            fragment.setPicInfo(mPicInfos.get(position));
            fragment.setOnFinishListener(mOnFinishListener);
            fragment.setCallback(mCallback);
            fragment.setOnPictureClickListener(mOnPictureClickListener);
            fragment.setOnPictureLongClickListener(mOnPictureLongClickListener);
            fragment.setConfiguration(mConfiguration);
            if (position == mDefaultPosition) {
                fragment.startDefaultTransition();
                mDefaultPosition = -1;
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
            return mPicInfos.size();
        }
    }

    public ViewPagerFragment getFragmentByPosition(int position) {
        return mFragmentMap.get(position);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
        } catch (ArrayIndexOutOfBoundsException e) {

        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}
