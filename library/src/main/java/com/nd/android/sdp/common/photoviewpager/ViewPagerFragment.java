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
import android.support.v4.app.Fragment;
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

import com.github.rahatarmanahmed.cpv.CircularProgressView;
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
        mFlPreview = mView.findViewById(R.id.flPreview);
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mSceenWidth = displayMetrics.widthPixels;
        mSceenHeight = displayMetrics.heightPixels;
        mStatusBarHeight = Utils.getStatusBarHeightFix(getActivity().getWindow());
        final ImageView imageView = mActivityCallback.getPreviewView(mPreviewUrl);
        if (imageView != null) {
            final Bitmap previewBitmap = mActivityCallback.getPreviewBitmap(mPreviewUrl);
            if (previewBitmap != null) {
                Palette palette = Palette.from(previewBitmap).generate();
                final Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                if (lightVibrantSwatch != null) {
                    mPb.setColor(lightVibrantSwatch.getRgb());
                }
                mIvPreview.setImageBitmap(previewBitmap);
            }
        }

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
            final Bundle arguments = getArguments();
            if (arguments == null) {
                return;
            }
//            ((RelativeLayout.LayoutParams) targetView.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT, 0);
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
        } else {
            ((RelativeLayout.LayoutParams) mFlPreview.getLayoutParams()).addRule(RelativeLayout.CENTER_IN_PARENT, 1);
            mFlPreview.requestLayout();
            mPb.setVisibility(View.VISIBLE);
            mIvReal.setVisibility(View.GONE);
            mIvTemp.setVisibility(View.GONE);
            mIvPreview.setVisibility(View.VISIBLE);
            mIvPreview.setDrawableRadius(mFrameSize / 2);
            final Bitmap previewBitmap = mActivityCallback.getPreviewBitmap(mUrl);
            mIvPreview.setImageBitmap(previewBitmap);
            startGetImage();
        }
    }

    private PublishSubject<Integer> mBitmapProgressSubject;

    private void initProgressPublishSubject() {
        mBitmapProgressSubject = PublishSubject.create();
        mBitmapProgressSubject
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
                mActivityCallback.startGetImage(mUrl, new ImageGetterCallback() {
                    @Override
                    public void setImageToView(Bitmap bitmap) {
                        subscriber.onNext(bitmap);
                    }

                    @Override
                    public void setProgress(final int current, final int total) {
                        final float currentProgress = ((float) current) / ((float) total) * 100;
                        mBitmapProgressSubject.onNext((int) currentProgress);
                        if (currentProgress == 100) {
                            mBitmapProgressSubject.onCompleted();
                        }
                    }
                });
            }
        })
                .throttleLast(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(final Bitmap bitmap) {
                        mPb.setVisibility(View.GONE);
                        mIvTemp.setVisibility(View.VISIBLE);
                        mIvTemp.setImageBitmap(bitmap);
                        mIvPreview.setVisibility(View.GONE);
                        final File diskCache = mActivityCallback.getPicDiskCache(mUrl);
                        mIvReal.setOnLongClickListener(ViewPagerFragment.this);
                        if (diskCache != null && diskCache.exists()) {
                            if (!Utils.isGifFile(diskCache.getAbsolutePath())) {
                                mIvReal.setVisibility(View.VISIBLE);
                                mIvGif.setVisibility(View.GONE);
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
//                                ((ViewGroup) getView()).removeView(mIvTemp);
                                        mIvTemp.setVisibility(View.GONE);
                                        final int bmHeight = bitmap.getHeight();
                                        final int bmWidth = bitmap.getWidth();
                                        float maxScale;
                                        if (isPortrait(bmWidth, bmHeight)) {
                                            // 竖照片，放大宽
                                            maxScale = ((float) bmWidth) / ((float) mSceenWidth);
                                        } else {
                                            maxScale = ((float) bmHeight) / ((float) mSceenHeight);
                                        }
                                        if (maxScale < 1f) {
                                            maxScale = 1f / maxScale;
                                        }
                                        mIvReal.setMaxScale(maxScale);
                                        mIvReal.setDoubleTapZoomScale(maxScale);
                                        mIvReal.setImage(ImageSource.cachedBitmap(bitmap));
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
                > ((float) mSceenHeight) / (float) mSceenWidth;
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFullSizeSubscription != null) {
            mFullSizeSubscription.unsubscribe();
        }
    }

    public void downloadFullSize() {
        final File diskCache = mActivityCallback.getPicDiskCache(mUrl);
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
        mIvReal.setOnImageEventListener(ViewPagerFragment.this);
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
        mIvReal.resetScaleAndCenter();
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
        Bitmap bitmap = null;
        final Drawable drawable = mIvTemp.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        }
        return mActivityCallback.onLongClick(v, mUrl, bitmap);
    }

    public void setCallback(Callback callback) {
        mActivityCallback = callback;
    }

    public void setBg(View bg){
        mBg = bg;
    }

    public void finish() {
        if (getActivity().getCurrentFocus() != mView) {
            return;
        }
        final boolean animateFinish = animateFinish();
        if (animateFinish) {
            mIvReal.postDelayed(new Runnable() {
                @Override
                public void run() {
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
        final FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        final Fragment fragment = supportFragmentManager.findFragmentByTag(PhotoViewPagerFragment.TAG_PHOTO);
        supportFragmentManager
                .beginTransaction()
                .remove(fragment)
                .commit();
    }

    private boolean animateFinish() {
        if (mActivityCallback == null) {
            return false;
        }
        final ImageView previewView = mActivityCallback.getPreviewView(mUrl);
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
        final ImageView animateView = (ImageView) mView.findViewById(R.id.ivExitPreview);
        animateView.setVisibility(View.GONE);
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
        final ViewGroup.LayoutParams layoutParams = animateView.getLayoutParams();
        layoutParams.height = animatedValue;
        animateView.requestLayout();
        animateView.setImageDrawable(previewView.getDrawable());
        animateView.setScaleType(previewView.getScaleType());
        final int startY = (mIvReal.getHeight() - startHeight) / 2;
        animateView.setY(startY);
        final int startX = (mIvReal.getWidth() - startWidth) / 2;
        animateView.setX(startX);
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(startWidth, previewView.getWidth());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Integer animatedValue = (Integer) animation.getAnimatedValue();
                final ViewGroup.LayoutParams layoutParams = animateView.getLayoutParams();
                layoutParams.width = animatedValue;
                animateView.requestLayout();
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
                final ViewGroup.LayoutParams layoutParams = animateView.getLayoutParams();
                layoutParams.height = animatedValue;
                animateView.requestLayout();
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
                animateView.setX((Float) animation.getAnimatedValue());
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
                animateView.setY(animatedValue);
            }
        });
        valueAnimator4.setStartDelay(mScaleDuration);
        valueAnimator4.setDuration(EXIT_DURATION);
        valueAnimator4.start();
        if (mScaleDuration == 0) {
            mIvReal.setVisibility(View.GONE);
            animateView.setVisibility(View.VISIBLE);
        } else {
            mIvReal.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIvReal.setVisibility(View.GONE);
                    animateView.setVisibility(View.VISIBLE);
                }
            }, mScaleDuration);
        }
        return true;
    }

}
