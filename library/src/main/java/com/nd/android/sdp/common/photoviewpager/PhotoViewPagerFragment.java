package com.nd.android.sdp.common.photoviewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.options.PhotoViewOptions;

import java.util.ArrayList;

public class PhotoViewPagerFragment extends Fragment {

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

    private Toolbar mToolBar;
    private PhotoViewPager mVpPhoto;

    public static PhotoViewPagerFragment newInstance(ImageView imageView,
                                                     ArrayList<String> urls,
                                                     ArrayList<String> previewUrls,
                                                     PhotoViewOptions photoViewOptions) {
        Bundle args = new Bundle();
        PhotoViewPagerFragment fragment = new PhotoViewPagerFragment();
        args.putStringArrayList(PARAM_URLS, urls);
        args.putStringArrayList(PARAM_PREVIEW_URLS, previewUrls);
        args.putSerializable(PARAM_PHOTO_OPTIONS, photoViewOptions);
        int[] locations = new int[2];
        imageView.getLocationOnScreen(locations);
        args.putInt(PARAM_LEFT, locations[0]);
        args.putInt(PARAM_TOP, locations[1]);
        args.putInt(PARAM_WIDTH, imageView.getWidth());
        args.putInt(PARAM_HEIGHT, imageView.getHeight());
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
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        init();
    }

    private void exit() {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .remove(PhotoViewPagerFragment.this)
                .commit();
    }

    private View findViewById(int id) {
        return getView().findViewById(id);
    }

    private void init() {
        mVpPhoto = (PhotoViewPager) findViewById(R.id.vpPhoto);

        final Bundle arguments = getArguments();
        ArrayList<String> images = arguments.getStringArrayList(PARAM_URLS);
        ArrayList<String> previewImgs = arguments.getStringArrayList(PARAM_PREVIEW_URLS);
        mVpPhoto.init(images,
                previewImgs,
                arguments,
                (PhotoViewOptions) arguments.getSerializable(PARAM_PHOTO_OPTIONS));

    }

    /**
     * Start
     *
     * @param activity the context
     * @author Young
     */
    public static void start(FragmentActivity activity,
                             ImageView imageView,
                             ArrayList<String> urls,
                             ArrayList<String> previewUrls,
                             PhotoViewOptions photoViewOptions) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(TAG_PHOTO)
                .add(Window.ID_ANDROID_CONTENT, newInstance(imageView, urls, previewUrls, photoViewOptions), TAG_PHOTO)
                .commit();
    }

    public interface Callback {
        ImageView getPreviewView(String url);
    }
}
