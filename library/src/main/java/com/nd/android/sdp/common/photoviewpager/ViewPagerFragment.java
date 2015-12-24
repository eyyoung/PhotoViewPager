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

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nd.android.sdp.common.photoviewpager.callback.OnPictureLongClickListener;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nd.android.sdp.common.photoviewpager.utils.Utils;
import com.nd.android.sdp.common.photoviewpager.view.ImageSource;
import com.nd.android.sdp.common.photoviewpager.view.SubsamplingScaleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.HeightEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.RevealCircleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.RevealImageView;
import com.nd.android.sdp.common.photoviewpager.widget.WidthEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.XEvaluator;
import com.nd.android.sdp.common.photoviewpager.widget.YEvaluator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okio.BufferedSink;
import okio.Okio;
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

    private static final String BUNDLE_URL = "url";
    private static final int EXIT_DURATION = 500;
    private static final int MAX_EXIT_SCALEDURATION = 300;
    private View mBg;
    private ViewGroup mView;
    private CircularProgressView mPb;
    private RevealCircleImageView mIvPreview;
    private RevealImageView mIvTemp;
    private boolean mNeedTransition;
    private Subscription mSubscription;
    private SubsamplingScaleImageView mIvReal;
    private Callback mActivityCallback;
    private float mOrigScale;
    private long mScaleDuration;
    private int mSceenWidth;
    private int mSceenHeight;
    private Subscription mFullSizeSubscription;
    private int mStatusBarHeight;
    private View mFlPreview;
    private String mUrl;
    private String mPreviewUrl;
    private int mFrameSize;
    private GifImageView mIvGif;
    private IPhotoViewPagerConfiguration mConfiguration;
    private TextView mTvError;
    private ImageView mIvExit;
    private OnPictureLongClickListener mOnPictureLongClickListener;
    private Subscription mBitmapProgressSubscription;

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

    public void setUrl(String pUrl) {
        mUrl = pUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        mPreviewUrl = previewUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = (ViewGroup) inflater.inflate(R.layout.photo_viewpager_fragment_page, container, false);
        if (mActivityCallback == null) {
            Log.d("PhotoViewPagerFragment", "not support save instance");
            return mView;
        }
        mPb = (CircularProgressView) mView.findViewById(R.id.pb);
        mIvPreview = ((RevealCircleImageView) mView.findViewById(R.id.ivPreview));
        mIvTemp = (RevealImageView) mView.findViewById(R.id.ivTemp);
        mIvReal = (SubsamplingScaleImageView) mView.findViewById(R.id.imageView);
        mIvGif = ((GifImageView) mView.findViewById(R.id.ivGif));
        mIvExit = (ImageView) mView.findViewById(R.id.ivExitPreview);
        mTvError = (TextView) mView.findViewById(R.id.tvErrorHint);
        mFlPreview = mView.findViewById(R.id.flPreview);
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mSceenWidth = displayMetrics.widthPixels;
        mSceenHeight = displayMetrics.heightPixels;
        mStatusBarHeight = Utils.getStatusBarHeightFix(getActivity().getWindow());
        mIvReal.setOnLongClickListener(ViewPagerFragment.this);
        mIvGif.setOnLongClickListener(ViewPagerFragment.this);
        mIvReal.setOnImageEventListener(this);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mUrl == null) {
            return;
        }
        // 边框大小
        mFrameSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_size);
        if (mNeedTransition) {
            mNeedTransition = false;
            final File fileCache = mConfiguration.getPicDiskCache(mUrl);
            if (fileCache != null && fileCache.exists()) {
                // 直接放大
                animateToBigImage(fileCache);
            } else {
                final ImageView imageView = mActivityCallback.getPreviewView(mPreviewUrl);
                if (imageView != null) {
                    final Bitmap previewBitmap = mConfiguration.getPreviewBitmap(mPreviewUrl);
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
        } else {
            noAnimateInit();
        }
    }

    private void animateToBigImage(final File fileCache) {
        mBg.setAlpha(0);
        mBg.animate()
                .alpha(1.0f)
                .setDuration(400)
                .setInterpolator(new AccelerateInterpolator())
                .start();
        Bitmap previewBitmap = mConfiguration.getPreviewBitmap(mPreviewUrl);
        final ImageView previewView = mActivityCallback.getPreviewView(mPreviewUrl);
        if (previewBitmap == null) {
            final Drawable drawable = previewView.getDrawable();
            if (drawable == null || !(drawable instanceof BitmapDrawable)) {
                loadFileCache(fileCache, true);
                return;
            }
            previewBitmap = ((BitmapDrawable) drawable).getBitmap();
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
                loadFileCache(fileCache, false);
            }
        });
        mIvReal.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }
                mIvExit.setVisibility(View.GONE);
            }
        }, 550);
        animatorSet.setDuration(400).start();
        mIvExit.setVisibility(View.VISIBLE);
        final ImageView imageView = previewView;
        if (imageView != null) {
            mIvExit.setScaleType(imageView.getScaleType());
        } else {
            mIvExit.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        mIvExit.setImageBitmap(previewBitmap);
        mIvPreview.setVisibility(View.GONE);
        mIvGif.setVisibility(View.GONE);
        mIvReal.setVisibility(View.GONE);
        mPb.setVisibility(View.GONE);
    }

    private void loadFileCache(File fileCache, boolean needAnimate) {
        if (!Utils.isGifFile(fileCache.getAbsolutePath())) {
            if (needAnimate) {
                mIvReal.setAlpha(0);
                mIvReal.animate()
                        .alpha(1.0f)
                        .setDuration(400)
                        .setInterpolator(new AccelerateInterpolator())
                        .start();
            }
            final ImageSource uri = ImageSource.uri(fileCache.getAbsolutePath());
            mIvReal.setImage(uri);
            mIvPreview.setVisibility(View.GONE);
            mIvReal.setVisibility(View.VISIBLE);
            mIvGif.setVisibility(View.GONE);
            mIvReal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else {
            if (needAnimate) {
                mIvGif.setAlpha(0);
                mIvGif.animate()
                        .alpha(1.0f)
                        .setDuration(400)
                        .setInterpolator(new AccelerateInterpolator())
                        .start();
            }
            mIvReal.setVisibility(View.GONE);
            mIvGif.setVisibility(View.VISIBLE);
            mIvGif.setImageURI(Uri.fromFile(fileCache));
            mView.removeView(mIvPreview);
            mIvTemp.setVisibility(View.GONE);
            mIvGif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        // 最后设置成不可见
//        mIvExit.setVisibility(View.GONE);
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
        final Bitmap previewBitmap = mConfiguration.getPreviewBitmap(mPreviewUrl);
        mIvPreview.setImageBitmap(previewBitmap);
        startGetImage();
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
        animatorSet.setDuration(400).start();
        final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvPreview, RevealCircleImageView.RADIUS,
                0, (mFrameSize - marginSize * 2) / 2);
        final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 0, 1);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator, animator1);
        set.setInterpolator(new AccelerateInterpolator());
        set.setDuration(400).start();
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
        mSubscription = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                mConfiguration.startGetImage(mUrl, new ImageGetterCallback() {
                    @Override
                    public void setImageToView(Bitmap bitmap) {
                        subscriber.onNext(bitmap);
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
                        mTvError.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                            }
                        });
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
                        final File diskCache = mConfiguration.getPicDiskCache(mUrl);
                        mIvReal.setOnLongClickListener(ViewPagerFragment.this);
                        if (diskCache != null && diskCache.exists()) {
                            if (!Utils.isGifFile(diskCache.getAbsolutePath())) {
                                mIvReal.setVisibility(View.GONE);
                                mIvGif.setVisibility(View.GONE);
                                mIvReal.setImage(ImageSource.uri(Uri.fromFile(diskCache)));
                                final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvTemp, RevealImageView.RADIUS,
                                        mFrameSize / 2, 0);
                                final ObjectAnimator animator2 = ObjectAnimator.ofFloat(mIvTemp, RevealImageView.ALPHA,
                                        0, 1);
                                AnimatorSet set = new AnimatorSet();
                                set.playTogether(animator, animator2);
                                set.setDuration(300).start();
                                set.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        if (!isAdded()) {
                                            return;
                                        }
                                        mView.removeView(mIvPreview);
                                        mIvReal.setVisibility(View.VISIBLE);
                                        mOrigScale = mIvReal.getScale();
                                        mIvReal.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                finish();
                                            }
                                        });
                                    }
                                });
                            } else {
                                mIvReal.setVisibility(View.GONE);
                                mIvGif.setVisibility(View.VISIBLE);
                                mIvGif.setImageURI(Uri.fromFile(diskCache));
                                mView.removeView(mIvPreview);
                                mIvTemp.setVisibility(View.GONE);
                                mIvGif.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                });
                            }
                        }
                        mSubscription.unsubscribe();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if (mBitmapProgressSubscription != null) {
                            mBitmapProgressSubscription.unsubscribe();
                        }
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
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
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
        setCallback(null);
        setOnPictureLongClickListener(null);
        setBg(null);
    }

    public void downloadFullSize() {
        final File diskCache = mConfiguration.getPicDiskCache(mUrl);
        if (diskCache.exists()) {
            loadPicFromFile(diskCache);
            return;
        }
        mIvReal.animate()
                .alpha(0)
                .setDuration(500)
                .start();
        mPb.setVisibility(View.VISIBLE);
        // 下载完成
        mFullSizeSubscription = download(mUrl, diskCache)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<Integer, File>>() {
                    @Override
                    public void call(Pair<Integer, File> filePair) {
                        final int progress = filePair.first;
                        if (progress > 0) {
                            mPb.setProgress(progress);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mPb.setVisibility(View.GONE);
                        throwable.printStackTrace();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        loadPicFromFile(diskCache);
                    }
                });
    }

    private void loadPicFromFile(File diskCache) {
        mIvReal.animate()
                .alpha(1.0f)
                .setDuration(500)
                .start();
        // 下载完成
        mPb.setVisibility(View.GONE);
        final ImageSource uri = ImageSource.uri(diskCache.getAbsolutePath());
        mIvReal.setImage(uri);
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
                                okHttpClient.newCall(request).enqueue(new com.squareup.okhttp.Callback() {
                                    @Override
                                    public void onFailure(Request request, IOException e) {
                                        subscriber.onError(e);
                                    }

                                    @Override
                                    public void onResponse(Response response) throws IOException {
                                        try {
                                            BufferedSink sink = Okio.buffer(Okio.sink(file));
                                            final ResponseBody body = response.body();
                                            final long size = sink.writeAll(body.source());
                                            final String strLen = response.header("Content-Length");
                                            long length;
                                            try {
                                                length = Long.parseLong(strLen);
                                            } catch (NumberFormatException e) {
                                                e.printStackTrace();
                                                length = -1;
                                            }
                                            sink.close();
                                            final int progress = (int) (size * 100 / length);
                                            final Pair<Integer, File> filePair = new Pair<>(progress, file);
                                            subscriber.onNext(filePair);
                                            if (progress == 100) {
                                                subscriber.onCompleted();
                                            }
                                        } catch (IOException io) {
                                            throw OnErrorThrowable.from(OnErrorThrowable.addValueAsLastCause(io, url));
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
    }

    @Override
    public void onReady() {

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
        return mOnPictureLongClickListener.onLongClick(v, mUrl, bitmap);
    }

    public void setCallback(Callback callback) {
        mActivityCallback = callback;
    }

    public void setBg(View bg) {
        mBg = bg;
    }

    public void finish() {
        int[] location = new int[2];
        mView.getLocationOnScreen(location);
        if (location[0] < 0) {
            return;
        }
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
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            exit();
                        }
                    })
                    .start();
            final ObjectAnimator animator1 = ObjectAnimator.ofFloat(mBg, View.ALPHA, 1, 0);
            animator1.setDuration(500);
            animator1.start();
        }
    }

    private void exit() {
        final FragmentActivity activity = getActivity();
        if (activity == null) {
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
                .commit();
    }

    private boolean animateFinish() {
        if (mActivityCallback == null) {
            return false;
        }
        final ImageView previewView = mActivityCallback.getPreviewView(mPreviewUrl);
        if (previewView == null) {
            return false;
        }
        if (mIvGif.getVisibility() == View.VISIBLE) {
            return false;
        }
        mScaleDuration = ((long) ((mIvReal.getScale() - mOrigScale) / 0.2 * 100));
        if (mScaleDuration > MAX_EXIT_SCALEDURATION) {
            mScaleDuration = MAX_EXIT_SCALEDURATION;
        }
        mIvReal.animateScale(mOrigScale)
                .withDuration(mScaleDuration)
                .withInterruptible(false)
                .start();
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
}
