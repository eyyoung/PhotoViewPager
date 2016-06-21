package com.nd.android.sdp.common.photoviewpager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListenerV2;
import com.nd.android.sdp.common.photoviewpager.exception.HttpIOException;
import com.nd.android.sdp.common.photoviewpager.pojo.Info;
import com.nd.android.sdp.common.photoviewpager.reveal.animation.SupportAnimator;
import com.nd.android.sdp.common.photoviewpager.reveal.animation.ViewAnimationUtils;
import com.nd.android.sdp.common.photoviewpager.reveal.widget.RevealFrameLayout;
import com.nd.android.sdp.common.photoviewpager.utils.AnimateUtils;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;
import com.nd.android.sdp.common.photoviewpager.view.ImageSource;
import com.nd.android.sdp.common.photoviewpager.view.SubsamplingScaleImageView;
import com.nd.android.sdp.common.photoviewpager.view.decoder.SafeImageDecoder;
import com.nd.android.sdp.common.photoviewpager.widget.HeightEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.RevealCircleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.WidthEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.XEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.YEvaluator;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public abstract class BasePagerFragment extends Fragment implements SubsamplingScaleImageView.OnImageEventListener, View.OnLongClickListener {

    private static final int EXIT_DURATION = 300;
    private static final int MAX_EXIT_SCALEDURATION = 300;
    private static final int FADE_ANIMATE_DURATION = 300;
    private static final int REVEAL_IN_ANIMATE_DURATION = 300;
    private static final int TRANSLATE_IN_ANIMATE_DURATION = 300;
    private int mMarginSize;
    private RevealFrameLayout mRvFrame;
    private boolean mNoNeedBgAnim;
    private boolean mDisableOrigin;

    protected enum State {
        Animate,
        Loading,
        Loaded,
        Finishing,
        Finished
    }

    private View mBg;
    protected ViewGroup mView;
    protected CircularProgressView mPb;
    protected RevealCircleImageView mIvPreview;
    protected RevealCircleImageView mIvTemp;
    private boolean mNeedTransition;
    private Subscription mStartGetImageSubscription;
    protected SubsamplingScaleImageView mIvReal;
    private Callback mActivityCallback;
    private float mOrigScale;
    private long mScaleDuration;
    private int mSceenWidth;
    private int mSceenHeight;
    private int mStatusBarHeight;
    protected View mFlPreview;
    protected int mFrameSize;
    protected IPhotoViewPagerConfiguration mConfiguration;
    protected TextView mTvError;
    protected ImageView mIvExit;
    private OnPictureLongClickListener mOnPictureLongClickListener;
    private OnPictureLongClickListenerV2 mOnPictureLongClickListenerV2;
    private boolean mIsAnimateFinishing = false;
    private OnFinishListener mOnFinishListener;
    protected Info mInfo;
    private boolean mIsLoaded;
    private boolean mIsAreadyBigImage = false;
    protected boolean mImageLoaded = false;
    private View.OnClickListener mOnPictureClickListener;

    protected State mState;

    public BasePagerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mConfiguration == null) {
            mConfiguration = PhotoViewPagerManager.INSTANCE.getConfiguration();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mInfo == null) {
            return;
        }
        if (mActivityCallback == null) {
            Log.d("PhotoViewPagerFragment", "not support save instance");
            return;
        }
        initView((ViewGroup) view);
    }

    protected void initView(ViewGroup view) {
        mView = view;
        findView(mView);
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mSceenWidth = displayMetrics.widthPixels;
        mSceenHeight = displayMetrics.heightPixels;
        mIvReal.setOnLongClickListener(BasePagerFragment.this);
        mIvReal.setOnImageEventListener(this);
        mIvTemp.setOnClickListener(mFinishClickListener);

        // 边框大小
        mFrameSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_size);
        final Bitmap previewBitmap = getPreviewBitmap();
        if (previewBitmap != null) {
            try {
                Palette palette = Palette.from(previewBitmap).generate();
                final Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                if (lightVibrantSwatch != null) {
                    mPb.setColor(lightVibrantSwatch.getRgb());
                }
                mIvPreview.setImageBitmap(previewBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final File fileCache = getShowFileCache();
        if (mNeedTransition) {
            mNeedTransition = false;
            final boolean fileExists = fileCache != null && fileCache.exists();
            if (fileExists && fileCache.length() < 500 * 1024) {
                // 直接放大
                animateToBigImage(fileCache);
            } else {
                animateToProgress();
            }
            mIsLoaded = true;
        } else {
            noAnimateInit();
        }
        // 如果进入Loading状态了则不需要设置，如直接放大的情况实际上已经进入加载了
        if (mState != State.Loading) {
            mState = State.Animate;
        }
    }

    protected void findView(View view) {
        mPb = (CircularProgressView) mView.findViewById(R.id.pb);
        mIvPreview = ((RevealCircleImageView) mView.findViewById(R.id.ivPreview));
        mIvTemp = (RevealCircleImageView) mView.findViewById(R.id.ivTemp);
        mIvReal = (SubsamplingScaleImageView) mView.findViewById(R.id.imageView);
        mIvReal.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mIvExit = (ImageView) mView.findViewById(R.id.ivExitPreview);
        mTvError = (TextView) mView.findViewById(R.id.tvErrorHint);
        mFlPreview = mView.findViewById(R.id.flPreview);
        mRvFrame = (RevealFrameLayout) mView.findViewById(R.id.rvFrame);
    }

    /**
     * 起始显示图片缓存
     *
     * @return 图片缓存
     */
    protected abstract File getShowFileCache();

    private void animateToBigImage(final File fileCache) {
        mIsAreadyBigImage = true;
        if (!mNoNeedBgAnim) {
            AnimateUtils.fadeInView(mBg);
        }
        final ImageView previewView = mActivityCallback.getPreviewView(mInfo.getPreviewUrl());
        if (!Utils.isViewAvaliable(previewView)) {
            mIvPreview.setVisibility(View.GONE);
            loadFileCache(fileCache, true);
            return;
        }
        Bitmap previewBitmap = getPreviewBitmap();
        if (previewBitmap == null) {
            mIvPreview.setVisibility(View.GONE);
            loadFileCache(fileCache, true);
            return;
        }
        final Bundle arguments = getArguments();
        int startWidth = arguments.getInt(PhotoViewPagerFragment.PARAM_WIDTH, mSceenWidth);
        int startHeight = arguments.getInt(PhotoViewPagerFragment.PARAM_HEIGHT, mSceenHeight);
        // 起始x位置
        final int startLeft = arguments.getInt(PhotoViewPagerFragment.PARAM_LEFT, 0);
        // 起始y位置
        final int startTop = arguments.getInt(PhotoViewPagerFragment.PARAM_TOP, 0) + getStatusBarHeight();
        int targetHeight;
        int targetWidth;
        int targetTop;
        int targetLeft;
        if (isPortrait(previewBitmap.getWidth(), previewBitmap.getHeight())) {
            targetTop = 0;
            targetHeight = mSceenHeight + getStatusBarHeight();
            targetWidth = (int) (((float) previewBitmap.getWidth()) / ((float) previewBitmap.getHeight()) * targetHeight);
            targetLeft = (mSceenWidth - targetWidth) / 2;
        } else {
            targetHeight = (int) (((float) previewBitmap.getHeight()) / ((float) previewBitmap.getWidth()) * mSceenWidth);
            targetTop = (mSceenHeight + getStatusBarHeight() - targetHeight) / 2;
            targetLeft = 0;
            targetWidth = mSceenWidth;
        }
        final ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(mIvExit), startWidth, targetWidth);
        final ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(mIvExit), startHeight, targetHeight);
        final ValueAnimator xAnimator = ValueAnimator.ofObject(new XEvaluator(mIvExit), startLeft, targetLeft);
        final ValueAnimator yAnimator = ValueAnimator.ofObject(new YEvaluator(mIvExit), startTop, targetTop);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                loadFileCache(fileCache, false);
            }
        });
        animatorSet.setDuration(TRANSLATE_IN_ANIMATE_DURATION).start();
        mIvExit.setVisibility(View.VISIBLE);
        mIvExit.setScaleType(previewView.getScaleType());
        mIvExit.setOnClickListener(mFinishClickListener);
        mIvExit.setImageBitmap(previewBitmap);
        mIvPreview.setVisibility(View.GONE);
        mIvReal.setVisibility(View.GONE);
        mPb.setVisibility(View.GONE);
    }

    @CallSuper
    protected void loadFileCache(File fileCache, boolean needAnimate) {
        mState = State.Loading;
        if (needAnimate) {
            AnimateUtils.fadeInView(mIvReal);
        }
        final ImageSource source = ImageSource.uri(fileCache.getAbsolutePath());
        if (mDisableOrigin) {
            source.tilingDisabled();
            mIvReal.setBitmapDecoderClass(SafeImageDecoder.class);
        }
        mIvReal.setImage(source);
        mIvTemp.setVisibility(View.VISIBLE);
        // 保持Preview状态，由调用方自己决定是否显示
//        mIvPreview.setVisibility(View.GONE);
        mIvReal.setVisibility(View.VISIBLE);
        mIvReal.setOnClickListener(mFinishClickListener);
    }

    /**
     * 无动画
     */
    private void noAnimateInit() {
        final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mFlPreview.getLayoutParams();
        layoutParams.leftMargin = (mSceenWidth - mFrameSize) / 2;
        layoutParams.topMargin = (mSceenHeight - mFrameSize) / 2 + getStatusBarHeight();
        mFlPreview.requestLayout();
        mPb.setVisibility(View.VISIBLE);
        mIvReal.setVisibility(View.GONE);
        mIvTemp.setVisibility(View.GONE);
        mIvPreview.setVisibility(View.VISIBLE);
        mTvError.setVisibility(View.GONE);
        mIvPreview.setDrawableRadius(mFrameSize / 2);
        Bitmap previewBitmap = getPreviewBitmap();
        mIvPreview.setImageBitmap(previewBitmap);
    }

    @Nullable
    private Bitmap getPreviewBitmap() {
        Bitmap previewBitmap = mConfiguration.getPreviewBitmap(mInfo.getPreviewUrl());
        if (previewBitmap == null) {
            final ImageView previewView = mActivityCallback.getPreviewView(mInfo.getPreviewUrl());
            if (Utils.isViewAvaliable(previewView)) {
                final Drawable drawable = previewView.getDrawable();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    previewBitmap = ((BitmapDrawable) drawable).getBitmap();
                }
            }
        }
        return previewBitmap;
    }

    /**
     * 动画到加载画面
     */
    private void animateToProgress() {
        final Bundle arguments = getArguments();
        if (arguments == null) {
            return;
        }
        final int startViewWidth = arguments.getInt(PhotoViewPagerFragment.PARAM_WIDTH, mFrameSize);
        final int startViewHeight = arguments.getInt(PhotoViewPagerFragment.PARAM_HEIGHT, mFrameSize);
        // 边距大小
        mMarginSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_margin);
        int startWidth = startViewWidth + mMarginSize * 2;
        int startHeight = startViewHeight + mMarginSize * 2;
        final int targetLeft = (mSceenWidth - mFrameSize) / 2;
        final int startViewLeft = arguments.getInt(PhotoViewPagerFragment.PARAM_LEFT, targetLeft);
        final int targetTop = (mSceenHeight - mFrameSize) / 2 + getStatusBarHeight();
        final int startViewTop = arguments.getInt(PhotoViewPagerFragment.PARAM_TOP, targetTop);
        // 起始x位置
        final int startLeft = startViewLeft - mMarginSize;
        // 起始y位置
        final int startTop = startViewTop - mMarginSize + getStatusBarHeight();
        final ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(mFlPreview), startWidth, mFrameSize);
        final ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(mFlPreview), startHeight, mFrameSize);
        final ValueAnimator xAnimator = ValueAnimator.ofObject(new XEvaluator(mFlPreview), startLeft, targetLeft);
        final ValueAnimator yAnimator = ValueAnimator.ofObject(new YEvaluator(mFlPreview), startTop, targetTop);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
        animatorSet.setDuration(TRANSLATE_IN_ANIMATE_DURATION).start();
        final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvPreview, RevealCircleImageView.RADIUS,
                0, (mFrameSize - mMarginSize * 2) / 2);
        AnimatorSet set = new AnimatorSet();
        if (mNoNeedBgAnim) {
            set.playTogether(animator);
        } else {
            final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 0, 1);
            set.playTogether(animator, animator1);
        }
        set.setInterpolator(new AccelerateInterpolator());
        set.setDuration(TRANSLATE_IN_ANIMATE_DURATION).start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                mPb.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mPb.setVisibility(View.VISIBLE);
                startGetImage();
            }
        });
    }

    private void startGetImage() {
        mState = State.Loading;
        final File picDiskCache = getShowFileCache();
        mStartGetImageSubscription = Observable.just(picDiskCache)
                .flatMap(new Func1<File, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(File file) {
                        final boolean exists = picDiskCache.exists();
                        if (exists) {
                            return Observable.just(100);
                        } else {
                            return Utils.download(getContext(), mInfo.getUrl(), file);
                        }
                    }
                })
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer progress) {
                        if (progress > 0 && mPb.isIndeterminate()) {
                            mPb.setIndeterminate(false);
                        }
                        mPb.setProgress(progress);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        onImageLoadError(new HttpIOException("NetWorkError", throwable));
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mPb.setIndeterminate(true);
                        mIvTemp.setVisibility(View.GONE);
//                        mIvTemp.setImageBitmap(bitmap);
                        mIvReal.setOnLongClickListener(BasePagerFragment.this);
                        if (picDiskCache != null && picDiskCache.exists()) {
                            loadFileCache(picDiskCache, false);
                            afterGetImg();
                        }
                    }
                });
    }

    protected void afterGetImg() {

    }

    /**
     * 判断是否竖照片
     *
     * @param bmWidth  图片宽度
     * @param bmHeight 图片高度
     * @return 是否竖照片
     */
    private boolean isPortrait(float bmWidth, float bmHeight) {
        return bmHeight / bmWidth
                > ((float) mSceenHeight + getStatusBarHeight()) / (float) mSceenWidth;
    }

    public void startDefaultTransition() {
        mNeedTransition = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mStartGetImageSubscription != null
                && !mStartGetImageSubscription.isUnsubscribed()) {
            mStartGetImageSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // onsaveinstance
        if (mIvTemp != null) {
            mIvTemp.setImageBitmap(null);
            mIvExit.setImageBitmap(null);
            mIvPreview.setImageBitmap(null);
        }
    }

    @Override
    public void onReady() {
        onImageLoaded();
    }

    @Override
    public void onImageLoaded() {
        if (mImageLoaded) {
            return;
        }
        if (mIsAnimateFinishing) {
            return;
        }
        mImageLoaded = true;
        mState = State.Loaded;

        final int appliedOrientation = mIvReal.getAppliedOrientation();
        int sHeight;
        int sWidth;
        if (appliedOrientation == 90
                || appliedOrientation == 270) {
            sHeight = mIvReal.getSWidth();
            sWidth = mIvReal.getSHeight();
        } else {
            sHeight = mIvReal.getSHeight();
            sWidth = mIvReal.getSWidth();
        }
        final boolean portrait = isPortrait(sWidth, sHeight);
        float doubleZoomScale;
        final float widthScale = ((float) sWidth) / ((float) mSceenWidth);
        final float heightScale = ((float) sHeight) / ((float) mSceenHeight);
        float currentScale;
        if (portrait) {
            // 竖照片，放大宽
            doubleZoomScale = widthScale;
            currentScale = heightScale;
        } else {
            doubleZoomScale = heightScale;
            currentScale = widthScale;
        }
        if (doubleZoomScale < 1) {
            doubleZoomScale = 1f / doubleZoomScale;
        }
        if (currentScale < 1) {
            currentScale = 1f / currentScale;
        }
        float maxScale = doubleZoomScale;
        if (Math.abs(doubleZoomScale - currentScale) < 1.5) {
            maxScale = doubleZoomScale + 1.5f;
        }
        mIvReal.setMaxScale(maxScale);
        mIvReal.setDoubleTapZoomScale(doubleZoomScale);
        final float minScale = mIvReal.getMinScale();
        if (mIvReal.getScale() < minScale) {
            mIvReal.animateScale(minScale)
                    .withDuration(1)
                    .start();
        }
        mOrigScale = minScale;
        if (mIsAreadyBigImage) {
            mIvTemp.setVisibility(View.GONE);
            mIvReal.setVisibility(View.VISIBLE);
            mIvExit.setVisibility(View.GONE);
            return;
        }
        mIvReal.setVisibility(View.GONE);
        mIvTemp.setVisibility(View.VISIBLE);
        mIvTemp.setImageBitmap(getPreviewBitmap());
        mIvExit.setVisibility(View.GONE);
        mIvPreview.setVisibility(View.GONE);
        mPb.setVisibility(View.GONE);

        revealAndScaleTempView(sHeight, sWidth, portrait);
    }

    private void revealAndScaleTempView(float sHeight, float sWidth, boolean portrait) {
        final int previewSize = mFrameSize - mMarginSize * 2;
        final ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mIvTemp.getLayoutParams();
        layoutParams.width = previewSize;
        layoutParams.height = previewSize;
        final int startTop = (mSceenHeight - layoutParams.width) / 2 + getStatusBarHeight();
        layoutParams.topMargin = startTop;
        final int startLeft = (mSceenWidth - layoutParams.height) / 2;
        layoutParams.leftMargin = startLeft;
        mIvTemp.requestLayout();

        final int targetHeight;
        final int targetWidth;
        if (!portrait) {
            targetHeight = (int) (sHeight / sWidth * mIvReal.getWidth());
            targetWidth = mIvReal.getWidth();
        } else {
            targetWidth = (int) (sWidth / sHeight * mIvReal.getHeight());
            targetHeight = mIvReal.getHeight();
        }

        mIvTemp.alwaysCircle(true);
        int targetTop = (mIvReal.getHeight() - targetHeight) / 2;
        int targetLeft = (mSceenWidth - targetWidth) / 2;
        final ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(mIvTemp), previewSize, targetWidth);
        final ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(mIvTemp), previewSize, targetHeight);
        final ValueAnimator xAnimator = ValueAnimator.ofObject(new XEvaluator(mIvTemp), startLeft, targetLeft);
        final ValueAnimator yAnimator = ValueAnimator.ofObject(new YEvaluator(mIvTemp), startTop, targetTop);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
        set.setDuration(150).start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isAdded()) {
                    return;
                }
                if (mIsAnimateFinishing) {
                    return;
                }
                mIvTemp.alwaysCircle(false);
            }
        });

        final int startRadius = targetWidth > targetHeight ? targetHeight : targetWidth;
        final double endRadius = Math.hypot(targetWidth / 2, targetHeight / 2);
        final SupportAnimator circularReveal = ViewAnimationUtils.createCircularReveal(mIvTemp,
                targetWidth / 2 + targetLeft, targetHeight / 2 + targetTop, startRadius / 2,
                (float) endRadius);
        circularReveal.setStartDelay(100);
        circularReveal.setDuration(250);
        circularReveal.start();

        mIvTemp.postDelayed(new Runnable() {
            @Override
            public void run() {
                circularReveal.cancel();
                mIvTemp.setVisibility(View.GONE);
                mIvReal.setVisibility(View.VISIBLE);
            }
        }, 350);
    }

    @Override
    public void onPreviewLoadError(Exception e) {

    }

    @Override
    public void onImageLoadError(Exception e) {
        e.printStackTrace();
        mIvPreview.setVisibility(View.GONE);
        mIvReal.setVisibility(View.GONE);
        mPb.setVisibility(View.GONE);
        mTvError.setVisibility(View.VISIBLE);
        mTvError.setOnLongClickListener(BasePagerFragment.this);
        mTvError.setOnClickListener(mFinishClickListener);
        if (e instanceof HttpIOException) {
            mTvError.setText(R.string.photo_viewpager_download_failed);
        } else {
            mTvError.setText(R.string.photo_viewpager_image_load_failed);
        }
    }

    @Override
    public void onTileLoadError(Exception e) {
        onImageLoadError(e);
    }

    public void selected() {
        if (!mIsLoaded) {
            startGetImage();
            mIsLoaded = true;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnPictureLongClickListener != null) {
            Bitmap bitmap = null;
            final Drawable drawable = mIvTemp.getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            }
            mOnPictureLongClickListener.onLongClick(v, mInfo.getUrl(), bitmap);
        }
        return mOnPictureLongClickListenerV2 != null
                && mOnPictureLongClickListenerV2.onLongClick(v, mInfo.getUrl(), mConfiguration.getPicDiskCache(mInfo.getUrl()));
    }

    public void setCallback(Callback callback) {
        mActivityCallback = callback;
    }

    public void setBg(View bg) {
        mBg = bg;
    }

    public void finish() {
        if (mIsAnimateFinishing) {
            return;
        }
        if (mState == State.Animate) {
            return;
        }
        int[] location = new int[2];
        mView.getLocationOnScreen(location);
        if (location[0] < 0) {
            return;
        }
        mState = State.Finishing;
        mIsAnimateFinishing = true;
        final boolean animateFinish = animateFinish();
        if (animateFinish) {
            if (mNoNeedBgAnim) {
                finishActivity();
            } else {
                final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 1, 0);
                animator1.setDuration(mScaleDuration + EXIT_DURATION);
                animator1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        finishActivity();
                    }
                });
                animator1.start();
            }
        } else {
            ViewPropertyAnimator animate;
            if (mIvReal.getVisibility() == View.VISIBLE) {
                animate = mIvReal.animate();
            } else {
                animate = fadeOutContentViewAnimate();
            }
            mIvExit.setVisibility(View.GONE);
            if (animate != null) {
                animate
                        .alpha(0f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(FADE_ANIMATE_DURATION)
                        .start();
            }
            if (mNoNeedBgAnim) {
                finishActivity();
            } else {
                final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 1, 0);
                animator1.setDuration(FADE_ANIMATE_DURATION);
                animator1.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        finishActivity();
                    }
                });
                animator1.start();
            }
        }
        if (mPb.getVisibility() == View.VISIBLE) {
            final ObjectAnimator animator = ObjectAnimator.ofFloat(mFlPreview, View.ALPHA, 1, 0);
            animator.setDuration(mScaleDuration + EXIT_DURATION);
            animator.start();
        }
        if (mOnFinishListener != null) {
            mOnFinishListener.onFinish();
        }
    }

    private void finishActivity() {
        final FragmentActivity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
        mState = State.Finished;
    }

    @Nullable
    protected ViewPropertyAnimator fadeOutContentViewAnimate() {
        return null;
    }

    protected boolean animateFinish() {
        if (mActivityCallback == null) {
            return false;
        }
        final ImageView previewView = mActivityCallback.getPreviewView(mInfo.getPreviewUrl());
        if (!Utils.isViewAvaliable(previewView)) {
            return false;
        }
        mScaleDuration = Math.abs((long) ((mIvReal.getScale() - mOrigScale) / 0.2 * 100));
        if (mScaleDuration > MAX_EXIT_SCALEDURATION) {
            mScaleDuration = MAX_EXIT_SCALEDURATION;
        }
        final SubsamplingScaleImageView.AnimationBuilder animationBuilder = mIvReal.animateScale(mOrigScale);
        if (animationBuilder != null) {
            animationBuilder
                    .withDuration(mScaleDuration)
                    .withInterruptible(false)
                    .start();
        } else {
            return false;
        }
        mIvExit.setVisibility(View.GONE);
        final int sHeight = mIvReal.getSHeight();
        final int sWidth = mIvReal.getSWidth();
        int startHeight;
        int startWidth;
        if (isPortrait(sWidth, sHeight)) {
            startHeight = mIvReal.getHeight();
            startWidth = (int) ((float) sWidth / (float) sHeight * mIvReal.getHeight());
        } else {
            startHeight = (int) ((float) sHeight / (float) sWidth * mIvReal.getWidth());
            startWidth = mIvReal.getWidth();
        }
        final Integer animatedValue = startHeight;
        final ViewGroup.LayoutParams layoutParams = mIvExit.getLayoutParams();
        layoutParams.height = animatedValue;
        mIvExit.requestLayout();
        mIvExit.setImageDrawable(previewView.getDrawable());
        mIvExit.setScaleType(previewView.getScaleType());
        final int startY = (mIvReal.getHeight() - startHeight) / 2;
        mIvExit.setY(startY);
        final int startX = (mIvReal.getWidth() - startWidth) / 2;
        mIvExit.setX(startX);
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startWidth, previewView.getWidth());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Integer animatedValue = (Integer) animation.getAnimatedValue();
                final ViewGroup.LayoutParams layoutParams = mIvExit.getLayoutParams();
                layoutParams.width = animatedValue;
                mIvExit.requestLayout();
            }
        });
        valueAnimator.setStartDelay(mScaleDuration);
        valueAnimator.setDuration(EXIT_DURATION);
        valueAnimator.start();
        final ValueAnimator valueAnimator2 = ValueAnimator.ofInt(startHeight, previewView.getHeight());
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Integer animatedValue = (Integer) animation.getAnimatedValue();
                final ViewGroup.LayoutParams layoutParams = mIvExit.getLayoutParams();
                layoutParams.height = animatedValue;
                mIvExit.requestLayout();
            }
        });
        valueAnimator2.setStartDelay(mScaleDuration);
        valueAnimator2.setDuration(EXIT_DURATION);
        valueAnimator2.start();
        int location[] = new int[2];
        previewView.getLocationOnScreen(location);
        final ValueAnimator valueAnimator3 = ValueAnimator.ofFloat(startX, location[0]);
        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mIvExit.setX((Float) animation.getAnimatedValue());
            }
        });
        valueAnimator3.setStartDelay(mScaleDuration);
        valueAnimator3.setDuration(EXIT_DURATION);
        valueAnimator3.start();
        final ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(startY, location[1] + getStatusBarHeight());
        valueAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Float animatedValue = (Float) animation.getAnimatedValue();
                mIvExit.setY(animatedValue);
            }
        });
        valueAnimator4.setStartDelay(mScaleDuration);
        valueAnimator4.setDuration(EXIT_DURATION);
        valueAnimator4.start();
        if (mScaleDuration == 0) {
            mIvReal.setVisibility(View.GONE);
            mIvExit.setVisibility(View.VISIBLE);
        } else {
            mIvReal.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }
                    mIvReal.setVisibility(View.GONE);
                    mIvExit.setVisibility(View.VISIBLE);
                }
            }, mScaleDuration);
        }
        return true;
    }

    public void setOnPictureLongClickListener(OnPictureLongClickListener onPictureLongClickListener) {
        mOnPictureLongClickListener = onPictureLongClickListener;
    }

    public void setOnPictureLongClickListenerV2(OnPictureLongClickListenerV2 onPictureLongClickListenerV2) {
        mOnPictureLongClickListenerV2 = onPictureLongClickListenerV2;
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
    }

    protected final View.OnClickListener mFinishClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnPictureClickListener != null) {
                mOnPictureClickListener.onClick(v);
                return;
            }
            finish();
        }
    };

    public void setInfo(Info picInfo) {
        mInfo = picInfo;
    }

    public void setOnPictureClickListener(View.OnClickListener onPictureClickListener) {
        mOnPictureClickListener = onPictureClickListener;
    }

    public void setConfiguration(IPhotoViewPagerConfiguration configuration) {
        mConfiguration = configuration;
        if (mConfiguration == null) {
            mConfiguration = PhotoViewPagerManager.INSTANCE.getConfiguration();
        }
    }

    public void onParentScroll() {

    }

    public int getStatusBarHeight() {
        if (mStatusBarHeight == 0) {
            mStatusBarHeight = PhotoViewPagerManager.INSTANCE.getStatusHeight(getActivity());
        }
        return mStatusBarHeight;
    }

    public void disableOrigin(boolean disableOrigin) {
        mDisableOrigin = disableOrigin;
    }

    public void setNoBgAnim(boolean noNeedBgAnim) {
        mNoNeedBgAnim = noNeedBgAnim;
    }
}
