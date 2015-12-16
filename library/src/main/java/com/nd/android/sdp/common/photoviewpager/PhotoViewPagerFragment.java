package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nd.android.sdp.common.photoviewpager.menu.IBottomMenu;

import java.io.File;
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

    public static PhotoViewPagerFragment newInstance(ImageView imageView,
                                                     ArrayList<String> urls,
                                                     ArrayList<String> previewUrls,
                                                     int defaultPosition) {
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
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_viewpager_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setNavigationIcon(null);
        mToolBar.inflateMenu(R.menu.menu_photo_view_pager);
        mToolBar.setOnMenuItemClickListener(this);

        init();
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    private void init() {
        mVpPhoto = (PhotoViewPager) findViewById(R.id.vpPhoto);
        mVpPhoto.addOnPageChangeListener(this);
        mLlBottom = ((LinearLayout) findViewById(R.id.llBottomMenu));

        final Bundle arguments = getArguments();
        ArrayList<String> images = arguments.getStringArrayList(PARAM_URLS);
        ArrayList<String> previewImgs = arguments.getStringArrayList(PARAM_PREVIEW_URLS);
        final int defaultPosition = arguments.getInt(PARAM_DEFAULT_POSITION, 0);
        mVpPhoto.init(images,
                previewImgs,
                arguments,
                defaultPosition);

        mVpPhoto.post(new Runnable() {
            @Override
            public void run() {
                onPageSelected(defaultPosition);
            }
        });

        inflateMenu();
    }

    /**
     * Start
     *
     * @param activity the context
     */
    public static void start(FragmentActivity activity,
                             ImageView imageView,
                             ArrayList<String> urls,
                             ArrayList<String> previewUrls,
                             int defaultPosition,
                             IBottomMenu... menus) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(TAG_PHOTO)
                .add(Window.ID_ANDROID_CONTENT, newInstance(imageView, urls, previewUrls, defaultPosition), TAG_PHOTO)
                .commit();
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

    public interface Callback {
        ImageView getPreviewView(String url);

        void startGetImage(String url, ImageGetterCallback imageGetterCallback);

        File getFullsizePicDiskCache(String url);

        boolean onLongClick(View v, String mUrl, Bitmap bitmap);
    }


    private void inflateMenu() {
    }
}
