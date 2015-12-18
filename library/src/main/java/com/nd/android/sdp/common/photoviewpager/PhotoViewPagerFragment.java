package com.nd.android.sdp.common.photoviewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nd.android.sdp.common.photoviewpager.menu.IBottomMenu;
import com.nd.android.sdp.common.photoviewpager.menu.OnMenuClick;

import java.util.ArrayList;

public class PhotoViewPagerFragment extends Fragment implements Toolbar.OnMenuItemClickListener, ViewPager.OnPageChangeListener {

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
    public static final String TAG_PHOTO = "tag_photo";
    private static final String PARAM_DEFAULT_POSITION = "default_position";

    private Toolbar mToolBar;
    private PhotoViewPager mVpPhoto;
    private LinearLayout mLlBottom;
    private ArrayList<String> mImages;
    private Callback mCallback;

    public static PhotoViewPagerFragment newInstance(ImageView imageView,
                                                     ArrayList<String> urls,
                                                     ArrayList<String> previewUrls,
                                                     int defaultPosition,
                                                     Callback callback) {
        Bundle args = new Bundle();
        PhotoViewPagerFragment fragment = new PhotoViewPagerFragment();
        args.putStringArrayList(PARAM_URLS, urls);
        args.putStringArrayList(PARAM_PREVIEW_URLS, previewUrls);
        int[] locations = new int[2];
        imageView.getLocationOnScreen(locations);
        args.putInt(PARAM_LEFT, locations[0]);
        args.putInt(PARAM_TOP, locations[1]);
        args.putInt(PARAM_WIDTH, imageView.getWidth());
        args.putInt(PARAM_HEIGHT, imageView.getHeight());
        args.putInt(PARAM_DEFAULT_POSITION, defaultPosition);
        fragment.setCallbacks(callback);
        fragment.setArguments(args);
        return fragment;
    }

    private void setCallbacks(Callback callback) {
        mCallback = callback;
    }

    private void inflateMenu(final IBottomMenu menu) {
        final int resIcon = menu.getResIcon();
        final TextView menuView = ((TextView) getActivity().getLayoutInflater().inflate(R.layout.photo_viewpager_item_bottom_menu, mLlBottom, false));
        menuView.setText(menu.getDescRes());
        menuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final OnMenuClick onClickListener = menu.getOnClickListener();
                if (onClickListener != null) {
                    final int currentItem = mVpPhoto.getCurrentItem();
                    onClickListener.onClick(v, mImages.get(currentItem));
                }
            }
        });
        mLlBottom.addView(menuView);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_viewpager_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            Log.d("PhotoViewPagerFragment", "not support save instance");
            final FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
            final Fragment fragment = supportFragmentManager.findFragmentByTag(PhotoViewPagerFragment.TAG_PHOTO);
            if (fragment != null) {
                supportFragmentManager
                        .beginTransaction()
                        .remove(fragment)
                        .commit();
            }
            return;
        }
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setNavigationIcon(null);
        init();

        mCallback.onViewCreated(view);
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    private void init() {
        mVpPhoto = (PhotoViewPager) findViewById(R.id.vpPhoto);
        mVpPhoto.addOnPageChangeListener(this);
        mLlBottom = ((LinearLayout) findViewById(R.id.llBottomMenu));

        final Bundle arguments = getArguments();
        mImages = arguments.getStringArrayList(PARAM_URLS);
        ArrayList<String> previewImgs = arguments.getStringArrayList(PARAM_PREVIEW_URLS);
        final int defaultPosition = arguments.getInt(PARAM_DEFAULT_POSITION, 0);
        mVpPhoto.init(mImages,
                previewImgs,
                arguments,
                defaultPosition);
        mVpPhoto.setCallback(mCallback);

        mVpPhoto.post(new Runnable() {
            @Override
            public void run() {
                onPageSelected(defaultPosition);
            }
        });
    }

    /**
     * Start
     *
     * @param activity the context
     */
    public static PhotoViewPagerFragment start(FragmentActivity activity,
                                               ImageView imageView,
                                               ArrayList<String> urls,
                                               ArrayList<String> previewUrls,
                                               int defaultPosition,
                                               Callback callback) {
        final PhotoViewPagerFragment fragment = newInstance(imageView,
                urls,
                previewUrls,
                defaultPosition,
                callback);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(Window.ID_ANDROID_CONTENT, fragment, TAG_PHOTO)
                .commitAllowingStateLoss();
        return fragment;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_download) {
            final int currentItem = mVpPhoto.getCurrentItem();
            final ViewPagerFragment fragment = mVpPhoto.getFragmentByPosition(currentItem);
            fragment.downloadFullSize();
        }
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        final ViewPagerFragment fragment = mVpPhoto.getFragmentByPosition(position);
        if (fragment != null) {
            fragment.selected();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class FullImageSize implements IBottomMenu {

        @Override
        public int getResIcon() {
            return 0;
        }

        @Override
        public int getDescRes() {
            return R.string.photo_viewpager_download_full_size;
        }

        @Override
        public OnMenuClick getOnClickListener() {
            return new OnMenuClick() {
                @Override
                public void onClick(View v, String url) {
                    final int currentItem = mVpPhoto.getCurrentItem();
                    final ViewPagerFragment fragmentByPosition = mVpPhoto.getFragmentByPosition(currentItem);
                    fragmentByPosition.downloadFullSize();
                }
            };
        }
    }

    /**
     * 下载并加载全尺寸图片
     */
    public void downloadFullSize() {
        final int currentItem = mVpPhoto.getCurrentItem();
        final ViewPagerFragment fragmentByPosition = mVpPhoto.getFragmentByPosition(currentItem);
        fragmentByPosition.downloadFullSize();
    }

}
