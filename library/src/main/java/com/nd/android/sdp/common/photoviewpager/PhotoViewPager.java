package com.nd.android.sdp.common.photoviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
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

    public PhotoViewPager(Context context) {
        super(context);
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(ArrayList<String> images, int position) {
        mUrls = images;

        mImagePagerAdapter = new ImagePagerAdapter();
        setAdapter(mImagePagerAdapter);

        setCurrentItem(position);
    }

    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mUrls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final String url = mUrls.get(position);
            return super.instantiateItem(container, position);
        }
    }
}
