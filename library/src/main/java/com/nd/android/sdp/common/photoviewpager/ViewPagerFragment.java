package com.nd.android.sdp.common.photoviewpager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nd.android.sdp.common.photoviewpager.callback.OnFinishListener;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nd.android.sdp.common.photoviewpager.pojo.PicInfo;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;
import com.nd.android.sdp.common.photoviewpager.view.ImageSource;
import com.nd.android.sdp.common.photoviewpager.view.SubsamplingScaleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.HeightEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.RevealCircleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.RevealImageView;
import com.nd.android.sdp.common.photoviewpager.widget.WidthEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.XEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.YEvaluator;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import pl.droidsonroids.gif.GifImageView;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ViewPagerFragment extends Fragment implements SubsamplingScaleImageView.OnImageEventListener, View.OnKeyListener, View.OnLongClickListener {

    private static final int EXIT_DURATION = 300;
    private static final int MAX_EXIT_SCALEDURATION = 300;
    private static final int FADE_ANIMATE_DURATION = 300;
    private static final int REVEAL_IN_ANIMATE_DURATION = 300;
    private static final int TRANSLATE_IN_ANIMATE_DURATION = 300;

    private View mBg;
    private ViewGroup mView;
    private CircularProgressView mPb;
    private RevealCircleImageView mIvPreview;
    private RevealImageView mIvTemp;
    private boolean mNeedTransition;
    private Subscription mStartGetImageSubscription;
    private SubsamplingScaleImageView mIvReal;
    private Callback mActivityCallback;
    private float mOrigScale;
    private long mScaleDuration;
    private int mSceenWidth;
    private int mSceenHeight;
    private Subscription mFullSizeSubscription;
    private int mStatusBarHeight;
    private View mFlPreview;
    private int mFrameSize;
    private GifImageView mIvGif;
    private IPhotoViewPagerConfiguration mConfiguration;
    private TextView mTvError;
    private ImageView mIvExit;
    private CircularProgressView mPbBigPic;
    private OnPictureLongClickListener mOnPictureLongClickListener;
    private Subscription mBitmapProgressSubscription;
    private boolean mIsAnimateFinishing = false;
    private OnFinishListener mOnFinishListener;
    private TextView mTvOrig;
    private PicInfo mPicInfo;
    private boolean mIsLoaded;

    public ViewPagerFragment() {
    }

    public static ViewPagerFragment newInstance(Bundle arguments) {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        if (arguments == null) {
            arguments = new Bundle();
        }
        viewPagerFragment.setArguments(arguments);
        return viewPagerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfiguration = PhotoViewPagerManager.INSTANCE.getConfiguration();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = (ViewGroup) inflater.inflate(R.layout.photo_viewpager_fragment_page, container, false);
        if (mActivityCallback == null) {
            Log.d("PhotoViewPagerFragment", "not support save instance");
            return mView;
        }
        mPb = (CircularProgressView) mView.findViewById(R.id.pb);
        mPbBigPic = (CircularProgressView) mView.findViewById(R.id.pbBigPic);
        mIvPreview = ((RevealCircleImageView) mView.findViewById(R.id.ivPreview));
        mIvTemp = (RevealImageView) mView.findViewById(R.id.ivTemp);
        mIvReal = (SubsamplingScaleImageView) mView.findViewById(R.id.imageView);
        mIvGif = ((GifImageView) mView.findViewById(R.id.ivGif));
        mIvExit = (ImageView) mView.findViewById(R.id.ivExitPreview);
        mTvError = (TextView) mView.findViewById(R.id.tvErrorHint);
        mTvOrig = ((TextView) mView.findViewById(R.id.tvOrig));
        mFlPreview = mView.findViewById(R.id.flPreview);
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mSceenWidth = displayMetrics.widthPixels;
        mSceenHeight = displayMetrics.heightPixels;
        mStatusBarHeight = Utils.getStatusBarHeightFix(getActivity().getWindow());
        mIvReal.setOnLongClickListener(ViewPagerFragment.this);
        mIvGif.setOnLongClickListener(ViewPagerFragment.this);
        mIvReal.setOnImageEventListener(this);
        mIvTemp.setOnClickListener(mFinishClickListener);
        mTvOrig.setOnClickListener(mViewOrig);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mPicInfo == null) {
            return;
        }
        // 边框大小
        mFrameSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_size);
        final boolean origAvailable = isOrigAvailable();
        final File fileCache = mConfiguration.getPicDiskCache(origAvailable ? mPicInfo.origUrl : mPicInfo.url);
        if (mNeedTransition) {
            mNeedTransition = false;
            if (fileCache != null && fileCache.exists()) {
                // 直接放大
                animateToBigImage(fileCache, origAvailable);
            } else {
                final ImageView imageView = mActivityCallback.getPreviewView(mPicInfo.previewUrl);
                if (imageView != null) {
                    final Bitmap previewBitmap = mConfiguration.getPreviewBitmap(mPicInfo.previewUrl);
                    if (previewBitmap != null) {
                        Palette palette = Palette.from(previewBitmap).generate();
                        final Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                        if (lightVibrantSwatch != null) {
                            mPb.setColor(lightVibrantSwatch.getRgb());
                        }
                        mIvPreview.setImageBitmap(previewBitmap);
                    }
                }
                animateToProgress();
            }
            mIsLoaded = true;
        }
    }

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

    private void animateToBigImage(final File fileCache, final boolean origAvailable) {
        mBg.setAlpha(0);
        mBg.animate()
                .alpha(1.0f)
                .setDuration(TRANSLATE_IN_ANIMATE_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .start();
        final ImageView previewView = mActivityCallback.getPreviewView(mPicInfo.previewUrl);
        if (previewView == null) {
            loadFileCache(fileCache, true, origAvailable);
            return;
        }
        Bitmap previewBitmap = getPreviewBitmap();
        if (previewBitmap == null) {
            loadFileCache(fileCache, true, origAvailable);
            return;
        }
        final Bundle arguments = getArguments();
        int startWidth = arguments.getInt(PhotoViewPagerFragment.PARAM_WIDTH, mSceenWidth);
        int startHeight = arguments.getInt(PhotoViewPagerFragment.PARAM_HEIGHT, mSceenHeight);
        // 起始x位置
        final int startLeft = arguments.getInt(PhotoViewPagerFragment.PARAM_LEFT, 0);
        // 起始y位置
        final int startTop = arguments.getInt(PhotoViewPagerFragment.PARAM_TOP, 0) + mStatusBarHeight;
        int targetHeight;
        int targetWidth;
        int targetTop;
        int targetLeft;
        if (isPortrait(previewBitmap.getWidth(), previewBitmap.getHeight())) {
            targetTop = 0;
            targetHeight = mSceenHeight + mStatusBarHeight;
            targetWidth = (int) (((float) previewBitmap.getWidth()) / ((float) previewBitmap.getHeight()) * targetHeight);
            targetLeft = (mSceenWidth - targetWidth) / 2;
        } else {
            targetHeight = (int) (((float) previewBitmap.getHeight()) / ((float) previewBitmap.getWidth()) * mSceenWidth);
            targetTop = (mSceenHeight + mStatusBarHeight - targetHeight) / 2;
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
                loadFileCache(fileCache, false, origAvailable);
            }
        });
        animatorSet.setDuration(TRANSLATE_IN_ANIMATE_DURATION).start();
        mIvExit.setVisibility(View.VISIBLE);
        mIvExit.setScaleType(previewView.getScaleType());
        mIvExit.setOnClickListener(mFinishClickListener);
        mIvExit.setImageBitmap(previewBitmap);
        mIvPreview.setVisibility(View.GONE);
        mIvGif.setVisibility(View.GONE);
        mIvReal.setVisibility(View.GONE);
        mPb.setVisibility(View.GONE);
    }

    private void loadFileCache(File fileCache, boolean needAnimate, boolean needBigOrig) {
        if (!Utils.isGifFile(fileCache.getAbsolutePath())) {
            if (needAnimate) {
                mIvReal.setAlpha(0);
                mIvReal.animate()
                        .alpha(1.0f)
                        .setDuration(TRANSLATE_IN_ANIMATE_DURATION)
                        .setInterpolator(new AccelerateInterpolator())
                        .start();
            }
            if (needBigOrig) {
                mPbBigPic.setVisibility(View.VISIBLE);
                mIvReal.setImage(ImageSource.uri(Uri.fromFile(fileCache)));
                mIvTemp.setVisibility(View.VISIBLE);
                mIvPreview.setVisibility(View.GONE);
                mIvReal.setVisibility(View.VISIBLE);
                mIvGif.setVisibility(View.GONE);
                mIvReal.setOnClickListener(mFinishClickListener);
            } else {
                mConfiguration.startGetImage("file://" + fileCache.getAbsolutePath(),
                        new ImageGetterCallback() {
                            @Override
                            public void setImageToView(Bitmap bitmap) {
                                mIvReal.setImage(ImageSource.cachedBitmap(bitmap));
                                mIvPreview.setVisibility(View.GONE);
                                mIvReal.setVisibility(View.VISIBLE);
                                mIvGif.setVisibility(View.GONE);
                                mIvReal.setOnClickListener(mFinishClickListener);
                            }

                            @Override
                            public void setProgress(long current, long total) {

                            }

                            @Override
                            public void error(String imageUri, View view, Throwable cause) {

                            }
                        });
            }
        } else {
            if (needAnimate) {
                mIvGif.setAlpha(0f);
                mIvGif.animate()
                        .alpha(1.0f)
                        .setDuration(TRANSLATE_IN_ANIMATE_DURATION)
                        .setInterpolator(new AccelerateInterpolator())
                        .start();
            }
            mIvReal.setVisibility(View.GONE);
            mIvGif.setVisibility(View.VISIBLE);
            mIvGif.setImageURI(Uri.fromFile(fileCache));
            mIvExit.setVisibility(View.GONE);
            mView.removeView(mIvPreview);
            mIvTemp.setVisibility(View.GONE);
            mIvGif.setOnClickListener(mFinishClickListener);
        }
    }

    /**
     * 无动画
     */
    private void noAnimateInit() {
        ((RelativeLayout.LayoutParams) mFlPreview.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        mFlPreview.requestLayout();
        mPb.setVisibility(View.VISIBLE);
        mIvReal.setVisibility(View.GONE);
        mIvTemp.setVisibility(View.GONE);
        mIvPreview.setVisibility(View.VISIBLE);
        mTvError.setVisibility(View.GONE);
        mIvPreview.setDrawableRadius(mFrameSize / 2);
        Bitmap previewBitmap = getPreviewBitmap();
        mIvPreview.setImageBitmap(previewBitmap);
        startGetImage();
    }

    @Nullable
    private Bitmap getPreviewBitmap() {
        Bitmap previewBitmap = mConfiguration.getPreviewBitmap(mPicInfo.previewUrl);
        if (previewBitmap == null) {
            final ImageView previewView = mActivityCallback.getPreviewView(mPicInfo.previewUrl);
            if (previewView != null) {
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
        // 边距大小
        int marginSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_margin);
        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        final int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int startWidth = arguments.getInt(PhotoViewPagerFragment.PARAM_WIDTH, mFrameSize) + marginSize * 2;
        int startHeight = arguments.getInt(PhotoViewPagerFragment.PARAM_HEIGHT, mFrameSize) + marginSize * 2;
        final int targetLeft = (screenWidth - mFrameSize) / 2;
        final int targetTop = (screenHeight - mFrameSize) / 2 + mStatusBarHeight;
        // 起始x位置
        final int startLeft = arguments.getInt(PhotoViewPagerFragment.PARAM_LEFT, targetLeft) - marginSize;
        // 起始y位置
        final int startTop = arguments.getInt(PhotoViewPagerFragment.PARAM_TOP, targetTop) - marginSize + mStatusBarHeight;
        final ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(mFlPreview), startWidth, mFrameSize);
        final ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(mFlPreview), startHeight, mFrameSize);
        final ValueAnimator xAnimator = ValueAnimator.ofObject(new XEvaluator(mFlPreview), startLeft, targetLeft);
        final ValueAnimator yAnimator = ValueAnimator.ofObject(new YEvaluator(mFlPreview), startTop, targetTop);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
        animatorSet.setDuration(TRANSLATE_IN_ANIMATE_DURATION).start();
        final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvPreview, RevealCircleImageView.RADIUS,
                0, (mFrameSize - marginSize * 2) / 2);
        final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 0, 1);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator, animator1);
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

    private PublishSubject<Integer> mBitmapProgressSubject;

    private void initProgressPublishSubject() {
        mBitmapProgressSubject = PublishSubject.create();
        mBitmapProgressSubscription = mBitmapProgressSubject
                .throttleLast(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        if (integer > 0 && mPb.isIndeterminate()) {
                            mPb.setIndeterminate(false);
                        }
                        mPb.setProgress(integer);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void startGetImage() {
        initProgressPublishSubject();
        mStartGetImageSubscription = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                mConfiguration.startGetImage(mPicInfo.url, new ImageGetterCallback() {
                    @Override
                    public void setImageToView(Bitmap bitmap) {
                        subscriber.onNext(bitmap);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void setProgress(final long current, final long total) {
                        final float currentProgress = ((float) current) / ((float) total) * 100;
                        mBitmapProgressSubject.onNext((int) currentProgress);
                    }

                    @Override
                    public void error(String imageUri, View view, Throwable cause) {
                        mPb.setVisibility(View.GONE);
                        mTvError.setVisibility(View.VISIBLE);
                        mTvError.setOnLongClickListener(ViewPagerFragment.this);
                        mTvError.setOnClickListener(mFinishClickListener);
                    }
                });
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(final Bitmap bitmap) {
                        mPb.setVisibility(View.GONE);
                        mIvTemp.setVisibility(View.VISIBLE);
                        mIvPreview.setVisibility(View.GONE);
                        mIvTemp.setImageBitmap(bitmap);
                        final File diskCache = mConfiguration.getPicDiskCache(mPicInfo.url);
                        mIvReal.setOnLongClickListener(ViewPagerFragment.this);
                        if (diskCache != null && diskCache.exists()) {
                            if (!Utils.isGifFile(diskCache.getAbsolutePath())) {
                                mIvReal.setVisibility(View.GONE);
                                mIvGif.setVisibility(View.GONE);
                                final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvTemp, RevealImageView.RADIUS,
                                        mFrameSize / 2, 0);
                                final ObjectAnimator animator2 = ObjectAnimator.ofFloat(mIvTemp, RevealImageView.ALPHA,
                                        0, 1);
                                AnimatorSet set = new AnimatorSet();
                                set.playTogether(animator, animator2);
                                set.setDuration(REVEAL_IN_ANIMATE_DURATION).start();
                                set.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        if (!isAdded()) {
                                            return;
                                        }
                                        final boolean origAvailable = isOrigAvailable();
                                        ImageSource source = origAvailable ?
                                                ImageSource.uri(Uri.fromFile(mConfiguration.getPicDiskCache(mPicInfo.origUrl))) :
                                                ImageSource.cachedBitmap(bitmap);
                                        mPbBigPic.setVisibility(View.VISIBLE);
                                        mIvReal.setImage(source);
                                        mView.removeView(mIvPreview);
                                        mIvReal.setVisibility(View.VISIBLE);
                                        mIvReal.setOnClickListener(mFinishClickListener);
                                    }
                                });
                            } else {
                                mIvReal.setVisibility(View.GONE);
                                mIvGif.setVisibility(View.VISIBLE);
                                mIvGif.setImageURI(Uri.fromFile(diskCache));
                                mView.removeView(mIvPreview);
                                mIvTemp.setVisibility(View.GONE);
                                mIvExit.setVisibility(View.GONE);
                                mIvGif.setOnClickListener(mFinishClickListener);
                            }
                        }
                        if (mBitmapProgressSubscription != null) {
                            mBitmapProgressSubscription.unsubscribe();
                        }
                        mStartGetImageSubscription.unsubscribe();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                    }
                });

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
                > ((float) mSceenHeight + mStatusBarHeight) / (float) mSceenWidth;
    }

    public void startDefaultTransition() {
        mNeedTransition = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mStartGetImageSubscription != null && !mStartGetImageSubscription.isUnsubscribed()) {
            mStartGetImageSubscription.unsubscribe();
        }
        if (mBitmapProgressSubscription != null) {
            mBitmapProgressSubscription.unsubscribe();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFullSizeSubscription != null) {
            mFullSizeSubscription.unsubscribe();
        }
        // onsaveinstance
        if (mIvTemp != null) {
            mIvTemp.setImageBitmap(null);
            mIvExit.setImageBitmap(null);
            mIvPreview.setImageBitmap(null);
        }
    }

    public void downloadFullSize() {
        final File diskCache = mConfiguration.getPicDiskCache(mPicInfo.origUrl);
        if (diskCache.exists()) {
            loadFileCache(diskCache, false, true);
            return;
        }
        // 下载完成
        mFullSizeSubscription = download(mPicInfo.origUrl, diskCache)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<Integer, File>>() {
                    @Override
                    public void call(Pair<Integer, File> filePair) {
                        final long progress = filePair.first;
                        if (progress > 0) {
                            if (progress != 100) {
                                mTvOrig.setText(String.format("%d%%", progress));
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
                        loadFileCache(diskCache, false, true);
                    }
                });
    }

    private void loadPicFromFile(File diskCache) {
        mIvReal.animate()
                .alpha(1.0f)
                .setDuration(FADE_ANIMATE_DURATION)
                .start();
        // 下载完成
        mPb.setVisibility(View.GONE);
        mConfiguration.startGetImage("file://" + diskCache.getAbsolutePath(),
                new ImageGetterCallback() {
                    @Override
                    public void setImageToView(Bitmap bitmap) {
                        mIvReal.setImage(ImageSource.cachedBitmap(bitmap));
                    }

                    @Override
                    public void setProgress(long current, long total) {

                    }

                    @Override
                    public void error(String imageUri, View view, Throwable cause) {

                    }
                });
    }


    public Observable<Pair<Integer, File>> download(final String url, final File file) {
        return Observable.just(url)
                .flatMap(new Func1<String, Observable<Pair<Integer, File>>>() {
                    @Override
                    public Observable<Pair<Integer, File>> call(String s) {
                        return Observable.create(new Observable.OnSubscribe<Pair<Integer, File>>() {
                            @Override
                            public void call(final Subscriber<? super Pair<Integer, File>> subscriber) {
                                Request request = new Request.Builder().url(url).build();
                                final OkHttpClient okHttpClient = new OkHttpClient();
                                Call call = okHttpClient.newCall(request);
                                try {
                                    Response response = call.execute();
                                    if (response.code() == 200) {
                                        InputStream inputStream = null;
                                        OutputStream outputStream = null;
                                        try {
                                            File tempFile = new File(file.getParent(), file.getName() + "_temp");
                                            if (tempFile.exists()) {
                                                final boolean delete = tempFile.delete();
                                                if (!delete) {
                                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new IOException(), url));
                                                }
                                            }
                                            if (file.exists()) {
                                                final boolean delete = file.delete();
                                                if (!delete) {
                                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new IOException(), url));
                                                }
                                            }
                                            inputStream = response.body().byteStream();
                                            outputStream = new FileOutputStream(tempFile);
                                            byte[] buff = new byte[1024 * 4];
                                            long downloaded = 0;
                                            long target = response.body().contentLength();
                                            final Pair<Integer, File> filePair = new Pair<>(0, tempFile);
                                            subscriber.onNext(filePair);
                                            while (true) {
                                                int readed = inputStream.read(buff);
                                                if (readed == -1) {
                                                    break;
                                                }
                                                downloaded += readed;
                                                outputStream.write(buff, 0, readed);
                                                final Pair<Integer, File> filePairProgress = new Pair<>(((int) (downloaded * 100 / target)), tempFile);
                                                subscriber.onNext(filePairProgress);
                                            }
                                            if (downloaded == target) {
                                                outputStream.flush();
                                                final boolean result = tempFile.renameTo(file);
                                                if (!result) {
                                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new IOException(), url));
                                                }
                                                subscriber.onCompleted();
                                            } else {
                                                throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new Throwable("Http Error"), url));
                                            }
                                        } catch (IOException io) {
                                            throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, url));
                                        } finally {
                                            if (inputStream != null) {
                                                inputStream.close();
                                            }
                                            if (outputStream != null) {
                                                outputStream.close();
                                            }
                                        }
                                    } else {
                                        throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(new Throwable("Http Error"), url));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(e, url));
                                }
                            }
                        });
                    }
                }).throttleLast(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onReady() {
        onImageLoaded();
    }

    @Override
    public void onImageLoaded() {
        final int sHeight = mIvReal.getSHeight();
        final int sWidth = mIvReal.getSWidth();
        float maxScale;
        if (isPortrait(sWidth, sHeight)) {
            // 竖照片，放大宽
            maxScale = ((float) sWidth) / ((float) mSceenWidth);
        } else {
            maxScale = ((float) sHeight) / ((float) mSceenHeight);
        }
        if (maxScale < 1) {
            maxScale = 1f / maxScale;
        }
        mIvReal.setMaxScale(maxScale);
        mIvReal.setDoubleTapZoomScale(maxScale);
        final float minScale = mIvReal.getMinScale();
        if (mIvReal.getScale() < minScale) {
            mIvReal.animateScale(minScale)
                    .withDuration(1)
                    .start();
        }
        mIvTemp.setVisibility(View.GONE);
        mIvExit.setVisibility(View.GONE);
        mIvPreview.setVisibility(View.GONE);
        mPbBigPic.setVisibility(View.GONE);
        mOrigScale = minScale;
    }

    @Override
    public void onPreviewLoadError(Exception e) {

    }

    @Override
    public void onImageLoadError(Exception e) {

    }

    @Override
    public void onTileLoadError(Exception e) {

    }

    public void selected() {
        mView.setFocusableInTouchMode(true);
        mView.requestFocus();
        mView.setOnKeyListener(this);
        if (!mIsLoaded) {
            noAnimateInit();
            mIsLoaded = true;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        if (mOnPictureLongClickListener == null) {
            return false;
        }
        Bitmap bitmap = null;
        final Drawable drawable = mIvTemp.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        return mOnPictureLongClickListener.onLongClick(v, mPicInfo.url, bitmap);
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
        // 还没加载完
//        if (mIvGif.getVisibility() == View.GONE
//                && !mIvReal.isReady()) {
//            return;
//        }
        int[] location = new int[2];
        mView.getLocationOnScreen(location);
        if (location[0] < 0) {
            return;
        }
        mIsAnimateFinishing = true;
        final boolean animateFinish = animateFinish();
        if (animateFinish) {
            mIvReal.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }
                    exit();
                }
            }, mScaleDuration + EXIT_DURATION);
            final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 1, 0);
            animator1.setDuration(mScaleDuration + EXIT_DURATION);
            animator1.start();
        } else {
            ViewPropertyAnimator animate;
            if (mIvGif.getVisibility() == View.GONE) {
                animate = mIvReal.animate();
            } else {
                animate = mIvGif.animate();
            }
            mIvExit.setVisibility(View.GONE);
            animate
                    .alpha(0f)
                    .setInterpolator(new AccelerateInterpolator())
                    .setDuration(FADE_ANIMATE_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            exit();
                        }
                    })
                    .start();
            final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 1, 0);
            animator1.setDuration(FADE_ANIMATE_DURATION);
            animator1.start();
        }
        if (mOnFinishListener != null) {
            mOnFinishListener.onFinish();
        }
    }

    private void exit() {
        final FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (!isAdded()) {
            return;
        }
        final FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        final Fragment fragment = supportFragmentManager.findFragmentByTag(PhotoViewPagerFragment.TAG_PHOTO);
        if (fragment == null) {
            return;
        }
        supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commitAllowingStateLoss();
    }

    private boolean animateFinish() {
        if (mActivityCallback == null) {
            return false;
        }
        final ImageView previewView = mActivityCallback.getPreviewView(mPicInfo.previewUrl);
        if (previewView == null) {
            return false;
        }
        if (mIvGif.getVisibility() == View.VISIBLE) {
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
        final ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(startY, location[1] + mStatusBarHeight);
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

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        mOnFinishListener = onFinishListener;
    }

    private View.OnClickListener mViewOrig = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            downloadFullSize();
        }
    };

    private final View.OnClickListener mFinishClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    public void setPicInfo(PicInfo picInfo) {
        mPicInfo = picInfo;
    }
}
