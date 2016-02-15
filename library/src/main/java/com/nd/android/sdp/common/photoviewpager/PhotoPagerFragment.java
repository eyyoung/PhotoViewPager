package com.nd.android.sdp.common.photoviewpager;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.TextView;
import android.widget.Toast;

import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;
import com.nd.android.sdp.common.photoviewpager.utils.AnimateUtils;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;

import java.io.File;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 静态图片页面
 */
public class PhotoPagerFragment extends BasePagerFragment {

    private Subscription mFullSizeSubscription;
    private TextView mTvOrig;
    private PicInfo mPicInfo;
    private GifImageView mIvGif;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.photo_viewpager_fragment_static_photo_page, container, false);
    }

    @Override
    protected void initView(ViewGroup view) {
        super.initView(view);
        mIvGif.setOnLongClickListener(this);
    }

    @Override
    protected void findView(View view) {
        super.findView(view);
        mTvOrig = ((TextView) mView.findViewById(R.id.tvOrig));
        mTvOrig.setOnClickListener(mViewOrig);
        mIvGif = ((GifImageView) mView.findViewById(R.id.ivGif));
    }

    @Override
    protected File getShowFileCache() {
        final boolean origAvailable = isOrigAvailable();
        return mConfiguration.getPicDiskCache(origAvailable ? mPicInfo.origUrl : mPicInfo.url);
    }

    private View.OnClickListener mViewOrig = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFullSizeSubscription != null
                    && !mFullSizeSubscription.isUnsubscribed()) {
                return;
            }
            downloadFullSize();
        }
    };

    private boolean isOrigAvailable() {
        if (TextUtils.isEmpty(mPicInfo.origUrl)) {
            mTvOrig.setVisibility(View.GONE);
            return false;
        } else {
            final File origCache = mConfiguration.getPicDiskCache(mPicInfo.origUrl);
            if (origCache != null && origCache.exists()) {
                mTvOrig.setVisibility(View.GONE);
                return true;
            } else {
                mTvOrig.setVisibility(View.VISIBLE);
                if (mPicInfo.size > 0) {
                    mTvOrig.setText(getString(R.string.photo_viewpager_view_origin_with_size, Formatter.formatFileSize(getContext(), mPicInfo.size)));
                } else {
                    mTvOrig.setText(R.string.photo_viewpager_view_origin);
                }
                return false;
            }
        }
    }

    public static BasePagerFragment newPhotoInstance(Bundle arguments) {
        PhotoPagerFragment viewPagerFragment = new PhotoPagerFragment();
        if (arguments == null) {
            arguments = new Bundle();
        }
        viewPagerFragment.setArguments(arguments);
        return viewPagerFragment;
    }

    public void downloadFullSize() {
        final File diskCache = mConfiguration.getPicDiskCache(mPicInfo.origUrl);
        // 下载完成
        mTvOrig.setText(String.format(Locale.ENGLISH, "%d%%", 0));
        mFullSizeSubscription = Utils.download(getActivity(), mPicInfo.origUrl, diskCache)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer progress) {
                        if (progress > 0) {
                            if (progress != 100) {
                                mTvOrig.setText(String.format(Locale.ENGLISH, "%d%%", progress));
                            } else {
                                mTvOrig.setVisibility(View.GONE);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mTvOrig.setText(R.string.photo_viewpager_view_origin);
                        Toast.makeText(getContext(), R.string.photo_viewpager_download_failed, Toast.LENGTH_SHORT).show();
                        throwable.printStackTrace();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mTvOrig.setVisibility(View.GONE);
                        mImageLoaded = false;
                        loadFileCache(diskCache, false);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFullSizeSubscription != null) {
            mFullSizeSubscription.unsubscribe();
        }
    }

    @Nullable
    @Override
    protected ViewPropertyAnimator fadeOutContentViewAnimate() {
        if (mIvGif.getVisibility() == View.VISIBLE) {
            return mIvGif.animate();
        }
        return super.fadeOutContentViewAnimate();
    }

    @Override
    protected boolean animateFinish() {
        return mIvGif.getVisibility() != View.VISIBLE && super.animateFinish();
    }

    @Override
    public void setInfo(Info picInfo) {
        super.setInfo(picInfo);
        mPicInfo = ((PicInfo) picInfo);
    }

    @Override
    protected void loadFileCache(File fileCache, boolean needAnimate) {
        if (!Utils.isGifFile(fileCache.getAbsolutePath())) {
            super.loadFileCache(fileCache, needAnimate);
        } else {
            mState = State.Loading;
            if (needAnimate) {
                AnimateUtils.fadeInView(mIvGif);
            }
            mPb.setVisibility(View.GONE);
            mIvReal.setVisibility(View.GONE);
            mIvGif.setVisibility(View.VISIBLE);
            mIvGif.setImageURI(Uri.fromFile(fileCache));
            mIvExit.setVisibility(View.GONE);
            mIvTemp.setVisibility(View.GONE);
            mIvPreview.setVisibility(View.GONE);
            mIvGif.setOnClickListener(mFinishClickListener);
        }
    }
}
