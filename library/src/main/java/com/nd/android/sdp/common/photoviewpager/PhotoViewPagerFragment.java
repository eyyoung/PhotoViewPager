package com.nd.android.sdp.common.photoviewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnViewCreatedListener;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;

import java.util.ArrayList;
import java.util.List;

public class PhotoViewPagerFragment extends Fragment implements ViewPager.OnPageChangeListener {

    private List<ViewPager.OnPageChangeListener> mOnPageListeners = new ArrayList<>();

    /**
     * URL列表，支持本地路径与URL路径
     */
    public static final String PARAM_PICINFOS = "picinfos";
    public static final String PARAM_TOP = "top";
    public static final String PARAM_LEFT = "left";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_HEIGHT = "height";
    public static final String TAG_PHOTO = "tag_photo";
    private static final String PARAM_DEFAULT_POSITION = "default_position";
    private PhotoViewPager mVpPhoto;
    private ArrayList<PicInfo> mImages;
    private Callback mCallback;
    private OnViewCreatedListener mOnViewCreatedListener;
    private OnFinishListener mOnFinishListener;

    private OnPictureLongClickListener mOnPictureLongClickListener;

    static PhotoViewPagerFragment newInstance(ImageView imageView,
                                              ArrayList<PicInfo> picInfos,
                                              int defaultPosition,
                                              Callback callback) {
        Bundle args = new Bundle();
        PhotoViewPagerFragment fragment = new PhotoViewPagerFragment();
        args.putParcelableArrayList(PARAM_PICINFOS, picInfos);
        int[] locations = new int[2];
        if (imageView != null) {
            imageView.getLocationOnScreen(locations);
            args.putInt(PARAM_LEFT, locations[0]);
            args.putInt(PARAM_TOP, locations[1]);
            args.putInt(PARAM_WIDTH, imageView.getWidth());
            args.putInt(PARAM_HEIGHT, imageView.getHeight());
            args.putInt(PARAM_DEFAULT_POSITION, defaultPosition);
        }
        fragment.setCallbacks(callback);
        fragment.setArguments(args);
        return fragment;
    }

    private void setCallbacks(Callback callback) {
        mCallback = callback;
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
        init();

        mOnViewCreatedListener.onViewCreated(view);
    }

    @Nullable
    private View findViewById(int id) {
        final View view = getView();
        if (view == null) {
            return null;
        }
        return view.findViewById(id);
    }

    private void init() {
        mVpPhoto = (PhotoViewPager) findViewById(R.id.vpPhoto);
        if (mVpPhoto == null) {
            return;
        }
        mVpPhoto.addOnPageChangeListener(this);
        for (ViewPager.OnPageChangeListener onPageChange : mOnPageListeners) {
            mVpPhoto.addOnPageChangeListener(onPageChange);
        }

        final Bundle arguments = getArguments();
        mImages = arguments.getParcelableArrayList(PARAM_PICINFOS);
        final int defaultPosition = arguments.getInt(PARAM_DEFAULT_POSITION, 0);
//        mVpPhoto.setPageTransformer(true, new DrawFromBackTransformer());
        mVpPhoto.setPageMargin(20);
        mVpPhoto.setBg(findViewById(R.id.bg));
        // Use child fragment manager,prevent memory leak
        mVpPhoto.init(getChildFragmentManager(),
                mImages,
                arguments,
                defaultPosition);
        mVpPhoto.setCallback(mCallback);
        mVpPhoto.setOnPictureLongClickListener(mOnPictureLongClickListener);
        mVpPhoto.setOnFinishListener(mOnFinishListener);
        mVpPhoto.post(new Runnable() {
            @Override
            public void run() {
                onPageSelected(defaultPosition);
            }
        });
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

    public void exit() {
        final int currentItem = mVpPhoto.getCurrentItem();
        final ViewPagerFragment fragmentByPosition = mVpPhoto.getFragmentByPosition(currentItem);
        fragmentByPosition.finish();
    }

    /**
     * 下载并加载全尺寸图片
     */
//    public void downloadFullSize() {
//        final int currentItem = mVpPhoto.getCurrentItem();
//        final ViewPagerFragment fragmentByPosition = mVpPhoto.getFragmentByPosition(currentItem);
//        fragmentByPosition.downloadFullSize();
//    }

    /**
     * 跳到指定页面
     *
     * @param position 序号
     */
    public void goPage(int position) {
        mVpPhoto.setCurrentItem(position);
    }

    public int getCurrentPosition() {
        return mVpPhoto.getCurrentItem();
    }

    /**
     * Delete position.
     */
    public void deletePosition(int position) {
        mImages.remove(position);
        mVpPhoto.getAdapter().notifyDataSetChanged();
    }

    public void addOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageListeners.add(onPageChangeListener);
    }

    public void setOnViewCreatedListener(OnViewCreatedListener onViewCreatedListener) {
        mOnViewCreatedListener = onViewCreatedListener;
    }

    public void setOnPictureLongClickListener(OnPictureLongClickListener onPictureLongClickListener) {
        mOnPictureLongClickListener = onPictureLongClickListener;
    }


    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
