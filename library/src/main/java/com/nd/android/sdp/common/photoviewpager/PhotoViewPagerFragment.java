package com.nd.android.sdp.common.photoviewpager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListenerV2;
import com.nd.android.sdp.common.photoviewpager.callback.OnViewCreatedListener;
import com.nd.android.sdp.common.photoviewpager.downloader.ExtraDownloader;
import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Photo view pager fragment.
 */
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
    private ArrayList<Info> mImages;
    private Callback mCallback;
    private OnViewCreatedListener mOnViewCreatedListener;
    private OnFinishListener mOnFinishListener;
    private ExtraDownloader mExtraDownloader;

    private OnPictureLongClickListener mOnPictureLongClickListener;
    private OnPictureLongClickListenerV2 mOnPictureLongClickListenerV2;
    private View.OnClickListener mOnPictureClickListener;
    private IPhotoViewPagerConfiguration mConfiguration;

    static PhotoViewPagerFragment newInstance(ImageView imageView,
                                              ArrayList<? extends Info> picInfos,
                                              int defaultPosition,
                                              Callback callback,
                                              IPhotoViewPagerConfiguration configuration) {
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
        }
        args.putInt(PARAM_DEFAULT_POSITION, defaultPosition);
        fragment.setCallbacks(callback);
        fragment.setConfiguration(configuration);
        fragment.setArguments(args);
        return fragment;
    }

    private void setConfiguration(IPhotoViewPagerConfiguration configuration) {
        if (configuration == null) {
            configuration = PhotoViewPagerManager.INSTANCE.getConfiguration();
        }
        if (configuration == null) {
            throw new IllegalArgumentException("Must Have a Configuration");
        }
        mConfiguration = configuration;
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
                        .addToBackStack(TAG_PHOTO)
                        .commit();
            }
            return;
        }
        init();
        if (mOnViewCreatedListener != null) {
            mOnViewCreatedListener.onViewCreated(view);
        }
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
        mVpPhoto.setExtraDownloader(mExtraDownloader);
        mVpPhoto.setOnPictureLongClickListenerV2(mOnPictureLongClickListenerV2);
        mVpPhoto.setOnPictureLongClickListener(mOnPictureLongClickListener);
        mVpPhoto.setOnFinishListener(mOnFinishListener);
        mVpPhoto.setOnPictureClickListener(mOnPictureClickListener);
        mVpPhoto.setConfigration(mConfiguration);
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
        final BasePagerFragment fragment = mVpPhoto.getFragmentByPosition(position);
        if (fragment != null) {
            fragment.selected();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        final int currentItem = mVpPhoto.getCurrentItem();
        final BasePagerFragment fragment = mVpPhoto.getFragmentByPosition(currentItem - 1);
        if (fragment != null) {
            fragment.onParentScroll();
        }
        final BasePagerFragment fragment2 = mVpPhoto.getFragmentByPosition(currentItem + 1);
        if (fragment2 != null) {
            fragment2.onParentScroll();
        }
    }

    public void exit() {
        final int currentItem = mVpPhoto.getCurrentItem();
        final BasePagerFragment fragmentByPosition = mVpPhoto.getFragmentByPosition(currentItem);
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
        if (mImages.size() == 1) {
            final FragmentManager supportFragmentManager = getFragmentManager();
            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .remove(this)
                    .commitAllowingStateLoss();
        } else {
            mImages.remove(position);
            final PagerAdapter adapter = mVpPhoto.getAdapter();
            adapter.notifyDataSetChanged();
            mVpPhoto.post(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }
                    final int currentItem = mVpPhoto.getCurrentItem();
                    final BasePagerFragment fragmentByPosition = mVpPhoto.getFragmentByPosition(currentItem);
                    if (fragmentByPosition != null) {
                        fragmentByPosition.selected();
                    }
                }
            });
        }
    }

    /**
     * Add on page change listener.
     *
     * @param onPageChangeListener the on page change listener
     */
    public void addOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageListeners.add(onPageChangeListener);
        if (mVpPhoto != null) {
            mVpPhoto.addOnPageChangeListener(onPageChangeListener);
        }
    }

    /**
     * Sets on view created listener.
     *
     * @param onViewCreatedListener the on view created listener
     */
    public void setOnViewCreatedListener(OnViewCreatedListener onViewCreatedListener) {
        mOnViewCreatedListener = onViewCreatedListener;
    }

    @Deprecated
    public void setOnPictureLongClickListener(OnPictureLongClickListener onPictureLongClickListener) {
        mOnPictureLongClickListener = onPictureLongClickListener;
    }

    /**
     * Sets on picture long click listener v 2.
     *
     * @param onPictureLongClickListenerV2 the on picture long click listener v 2
     */
    public void setOnPictureLongClickListenerV2(OnPictureLongClickListenerV2 onPictureLongClickListenerV2) {
        mOnPictureLongClickListenerV2 = onPictureLongClickListenerV2;
        if (mVpPhoto != null) {
            mVpPhoto.setOnPictureLongClickListenerV2(onPictureLongClickListenerV2);
        }
    }

    /**
     * Sets on picture click listener.
     *
     * @param onPictureClickListener the on picture click listener
     */
    public void setOnPictureClickListener(View.OnClickListener onPictureClickListener) {
        mOnPictureClickListener = onPictureClickListener;
        if (mVpPhoto != null) {
            mVpPhoto.setOnPictureClickListener(onPictureClickListener);
        }
    }

    /**
     * Sets on finish listener.
     *
     * @param onFinishListener the on finish listener
     */
    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
        if (mVpPhoto != null) {
            mVpPhoto.setOnFinishListener(onFinishListener);
        }
    }


    /**
     * Sets extra downloader.
     *
     * @param extraDownloader the extra downloader
     */
    public void setExtraDownloader(ExtraDownloader extraDownloader) {
        mExtraDownloader = extraDownloader;
        if (mVpPhoto != null) {
            mVpPhoto.setExtraDownloader(mExtraDownloader);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Gets current info.
     *
     * @return the current info
     */
    public Info getCurrentInfo() {
        final int currentPosition = getCurrentPosition();
        return mImages.get(currentPosition);
    }

    /**
     * 保存当前图片
     *
     * @param targetParentFile 保存父级位置
     */
    public void saveCurrentPhoto(File targetParentFile) {
        final Info currentInfo = getCurrentInfo();
        final String origUrl = currentInfo.getOrigUrl();
        File saveSource = null;
        if (origUrl != null) {
            final File origCache = mConfiguration.getPicDiskCache(origUrl);
            if (origCache != null && origCache.exists()) {
                saveSource = origCache;
            }
        }
        if (saveSource == null) {
            saveSource = mConfiguration.getPicDiskCache(currentInfo.getUrl());
        }
        if (saveSource == null || !saveSource.exists()) {
            Toast.makeText(getContext(), R.string.photo_viewpager_save_failed, Toast.LENGTH_LONG).show();
            return;
        }
        String ext;
        if (Utils.isGifFile(targetParentFile.getAbsolutePath())) {
            ext = ".gif";
        } else {
            ext = ".jpg";
        }
        File desFile = new File(targetParentFile, saveSource.getName() + ext);
        Utils.copyfile(saveSource, desFile, true);
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(desFile);
        mediaScanIntent.setData(contentUri);
        getContext().sendBroadcast(mediaScanIntent);
        Toast.makeText(getContext(), R.string.photo_viewpager_save_completed, Toast.LENGTH_LONG).show();
    }

}
