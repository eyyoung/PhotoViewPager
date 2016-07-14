package com.nd.android.sdp.common.photoviewpager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.ability.IExternalView;
import com.nd.android.sdp.common.photoviewpager.callback.OnDetachCallBack;
import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListenerV2;
import com.nd.android.sdp.common.photoviewpager.callback.OnViewCreatedListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnViewCreatedListenerV2;
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
    public static final String PARAM_DEFAULT_POSITION = "default_position";
    private PhotoViewPager mVpPhoto;
    private ArrayList<Info> mImages;
    private Callback mCallback;
    private OnViewCreatedListener mOnViewCreatedListener;
    private OnViewCreatedListenerV2 mOnViewCreatedListenerV2;
    private OnFinishListener mOnFinishListener;
    private ExtraDownloader mExtraDownloader;

    private OnPictureLongClickListener mOnPictureLongClickListener;
    private OnPictureLongClickListenerV2 mOnPictureLongClickListenerV2;
    private View.OnClickListener mOnPictureClickListener;
    private IPhotoViewPagerConfiguration mConfiguration;
    private OnDetachCallBack mDetachCallBack;
    private int mDefaultResId;
    private Bitmap mDefaultBitmap;
    private boolean mNoBgAnim;
    private boolean mDisableOrigin;
    @Nullable
    private Class<? extends IExternalView> mExternalView;

    static PhotoViewPagerFragment newInstance(@Nullable ImageView imageView,
                                              @NonNull
                                              ArrayList<? extends Info> picInfos,
                                              PhotoViewOptions photoViewOptions) {
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
        args.putInt(PARAM_DEFAULT_POSITION, photoViewOptions.getDefaultPosition());
        fragment.setCallbacks(photoViewOptions.getCallback());
        fragment.setConfiguration(photoViewOptions.getPhotoViewPagerConfiguration());
        fragment.disableOrigin(photoViewOptions.isDisableOrigin());
        fragment.setArguments(args);
        fragment.setExternalView(photoViewOptions.getExternalView());
        fragment.setOnPictureLongClickListenerV2(photoViewOptions.getOnPictureLongClickListenerV2());
        fragment.setOnViewCreatedListenerV2(photoViewOptions.getOnViewCreatedListenerV2());
        fragment.setOnFinishListener(photoViewOptions.getOnFinishListener());
        fragment.setExtraDownloader(photoViewOptions.getExtraDownload());
        fragment.setDefaultRes(photoViewOptions.getDefaultResId());
        return fragment;
    }

    private void setExternalView(@Nullable Class<? extends IExternalView> externalView) {
        mExternalView = externalView;
    }

    private void disableOrigin(boolean disableOrigin) {
        mDisableOrigin = disableOrigin;
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
        if (mCallback == null) {
            mCallback = new Callback() {
                @Override
                public ImageView getPreviewView(String previewUrl) {
                    return null;
                }
            };
        }
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
        if (mOnViewCreatedListener != null) {
            mOnViewCreatedListener.onViewCreated(view);
        }
        RelativeLayout relativeLayout = (RelativeLayout) view;
        if (mOnViewCreatedListenerV2 != null) {
            mOnViewCreatedListenerV2.onViewCreated(relativeLayout);
        }
        initExternalView(relativeLayout);
    }

    private void initExternalView(RelativeLayout relativeLayout) {
        if (mExternalView == null) {
            return;
        }
        try {
            IExternalView externalView = mExternalView.newInstance();
            View view = externalView.getView(getActivity(), this);
            relativeLayout.addView(view);
        } catch (Exception e) {
            e.printStackTrace();
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
        final View bg = findViewById(R.id.bg);
        mVpPhoto.setBg(bg);
        // Use child fragment manager,prevent memory leak
        mVpPhoto.init(getChildFragmentManager(),
                mImages,
                arguments,
                defaultPosition);
        mVpPhoto.setCallback(mCallback);
        if (mNoBgAnim) {
            mVpPhoto.setNoBgAnim();
        }
        mVpPhoto.setExtraDownloader(mExtraDownloader);
        mVpPhoto.setOnPictureLongClickListenerV2(mOnPictureLongClickListenerV2);
        mVpPhoto.setOnPictureLongClickListener(mOnPictureLongClickListener);
        mVpPhoto.setOnFinishListener(mOnFinishListener);
        mVpPhoto.setOnPictureClickListener(mOnPictureClickListener);
        mVpPhoto.disableOrigin(mDisableOrigin);
        if (mDefaultResId > 0) {
            mDefaultBitmap = BitmapFactory.decodeResource(getResources(), mDefaultResId);
            mVpPhoto.setDefaultBitmap(mDefaultBitmap);
        }
        mVpPhoto.setConfigration(mConfiguration);
        mVpPhoto.post(new Runnable() {
            @Override
            public void run() {
                onPageSelected(defaultPosition);
                for (ViewPager.OnPageChangeListener onPageListeners : mOnPageListeners) {
                    onPageListeners.onPageSelected(defaultPosition);
                }
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

    @SuppressWarnings("unused")
    public void exitWithoutAnim() {
        final FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.finish();
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
    @SuppressWarnings("unused")
    public void goPage(int position) {
        mVpPhoto.setCurrentItem(position);
    }

    /**
     * Gets current position.
     *
     * @return the current position
     */
    public int getCurrentPosition() {
        if (mVpPhoto == null) {
            return 0;
        }
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
    @SuppressWarnings("unused")
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

    /**
     * Sets on view created listener.
     *
     * @param onViewCreatedListenerV2 the on view created listener v2
     */
    @SuppressWarnings("unused")
    public void setOnViewCreatedListenerV2(OnViewCreatedListenerV2 onViewCreatedListenerV2) {
        mOnViewCreatedListenerV2 = onViewCreatedListenerV2;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
        if (Utils.isGifFile(saveSource.getAbsolutePath())) {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PhotoViewPagerManager.INSTANCE.removeFragment(this);
        if (mDefaultBitmap != null) {
            mDefaultBitmap.recycle();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDetachCallBack) {
            mDetachCallBack = ((OnDetachCallBack) context);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mDetachCallBack != null) {
            mDetachCallBack.onDetach();
        }
    }

    public void setNoBgAnim() {
        mNoBgAnim = true;
    }

    public void setDefaultRes(@DrawableRes int resId) {
        mDefaultResId = resId;
    }
}
