package com.nd.android.sdp.common.photoviewpager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetter;
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

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ViewPagerFragment extends Fragment {

    private static final String BUNDLE_URL = "url";
    private static final String PARAM_IMAGE_GETTER = "image_getter";
    private static final int SCALE_DURATION = 1000;
    private static final int EXIT_DURATION = 500;
    private ImageGetter mImageGetter;
    private View mView;
    private CircularProgressView mPb;
    private RevealCircleImageView mIvPreview;
    private RevealImageView mIvTemp;
    private boolean mNeedTransition;
    private Subscription mSubscription;
    private SubsamplingScaleImageView mIvReal;
    private int mStartWidth;// 起始宽度
    private int mStartHeight; // 起始高度
    private PhotoViewPagerFragment.Callback mActivityCallback;
    private float mOrigScale;

    public void setUrl(String pUrl) {
        mUrl = pUrl;
    }

    private String mUrl;

    public void setPreviewUrl(String previewUrl) {
        mPreviewUrl = previewUrl;
    }

    private String mPreviewUrl;

    public ViewPagerFragment() {
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        final Class<? extends ImageGetter> getterClass = (Class<? extends ImageGetter>) args.getSerializable(PARAM_IMAGE_GETTER);
        try {
            mImageGetter = getterClass.newInstance();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ViewPagerFragment newInstance(Class<? extends ImageGetter> getterClass, Bundle arguments) {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        if (arguments == null) {
            arguments = new Bundle();
        }
        arguments.putSerializable(PARAM_IMAGE_GETTER, getterClass);
        viewPagerFragment.setArguments(arguments);
        return viewPagerFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.photo_viewpager_fragment_page, container, false);

        if (savedInstanceState != null) {
            if (mUrl == null && savedInstanceState.containsKey(BUNDLE_URL)) {
                mUrl = savedInstanceState.getString(BUNDLE_URL);
            }
        }
        if (mUrl != null && !mNeedTransition) {
            startGetImage();
        }

        mPb = (CircularProgressView) mView.findViewById(R.id.pb);
        mIvPreview = ((RevealCircleImageView) mView.findViewById(R.id.ivPreview));
        mIvTemp = (RevealImageView) mView.findViewById(R.id.ivTemp);
        mIvReal = (SubsamplingScaleImageView) mView.findViewById(R.id.imageView);

        Bitmap bitmap = mImageGetter.getPreviewImage(mPreviewUrl);
        if (bitmap != null) {
            mIvPreview.setImageBitmap(bitmap);
        }
        return mView;
    }

    private void startGetImage() {
        mSubscription = Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                mImageGetter.startGetImage(mUrl, new ImageGetterCallback() {
                    @Override
                    public void setImageToView(Bitmap bitmap) {
                        subscriber.onNext(bitmap);
                    }
                });
            }
        })
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(final Bitmap bitmap) {
                        mPb.setVisibility(View.GONE);
                        mIvTemp.setImageBitmap(bitmap);
                        mIvPreview.setVisibility(View.GONE);
                        int finalSize = mIvTemp.getWidth();
                        final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvTemp, RevealImageView.RADIUS,
                                finalSize / 2, 0);
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
                                ((ViewGroup) getView()).removeView(mIvPreview);
//                                ((ViewGroup) getView()).removeView(mIvTemp);
                                mIvTemp.setVisibility(View.GONE);
                                mIvReal.setMinimumDpi(120);
                                mIvReal.setDoubleTapZoomDpi(120);
                                mIvReal.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
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
                        mSubscription.unsubscribe();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });

    }

    private void finish() {
        animateFinish();
        mIvReal.postDelayed(new Runnable() {
            @Override
            public void run() {
                final FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = supportFragmentManager.findFragmentByTag(PhotoViewPagerFragment.TAG_PHOTO);
                supportFragmentManager
                        .beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }, SCALE_DURATION + EXIT_DURATION);
        final ObjectAnimator animator1 = ObjectAnimator.ofFloat(getView().findViewById(R.id.bg), View.ALPHA, 1, 0);
        animator1.setDuration(SCALE_DURATION + EXIT_DURATION);
        animator1.start();
    }

    private void animateFinish() {
        if (mActivityCallback == null) {
            return;
        }
        final ImageView previewView = mActivityCallback.getPreviewView(mUrl);
        if (previewView == null) {
            return;
        }
        mIvReal.animateScale(mOrigScale)
                .withDuration(SCALE_DURATION)
                .withInterruptible(false)
                .start();
        final ImageView animateView = (ImageView) getView().findViewById(R.id.ivExitPreview);
        final int startHeight = (int) ((float) mIvReal.getSHeight() / (float) mIvReal.getSWidth() * mIvReal.getWidth());
        final Integer animatedValue = startHeight;
        final ViewGroup.LayoutParams layoutParams = animateView.getLayoutParams();
        layoutParams.height = animatedValue;
        animateView.requestLayout();
        animateView.setImageDrawable(previewView.getDrawable());
        animateView.setScaleType(previewView.getScaleType());
        final int startY = (mIvReal.getHeight() - startHeight) / 2;
        animateView.setY(startY);
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(mIvReal.getWidth(), previewView.getWidth());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Integer animatedValue = (Integer) animation.getAnimatedValue();
                final ViewGroup.LayoutParams layoutParams = animateView.getLayoutParams();
                layoutParams.width = animatedValue;
                animateView.requestLayout();
            }
        });
        valueAnimator.setStartDelay(SCALE_DURATION);
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
                Log.d("ViewPagerFragment", "height:" + animatedValue);
            }
        });
        valueAnimator2.setStartDelay(SCALE_DURATION);
        valueAnimator2.setDuration(EXIT_DURATION);
        valueAnimator2.start();
        final ValueAnimator valueAnimator3 = ValueAnimator.ofFloat(mIvReal.getX(), previewView.getX());
        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animateView.setX((Float) animation.getAnimatedValue());
            }
        });
        valueAnimator3.setStartDelay(SCALE_DURATION);
        valueAnimator3.setDuration(EXIT_DURATION);
        valueAnimator3.start();
        final ValueAnimator valueAnimator4 = ValueAnimator.ofFloat(startY, previewView.getY());
        valueAnimator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final Float animatedValue = (Float) animation.getAnimatedValue();
                animateView.setY(animatedValue);
                Log.d("ViewPagerFragment", "setY:" + animatedValue);
            }
        });
        valueAnimator4.setStartDelay(SCALE_DURATION);
        valueAnimator4.setDuration(EXIT_DURATION);
        valueAnimator4.start();
        mIvReal.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        View rootView = getView();
        if (rootView != null) {
            outState.putString(BUNDLE_URL, mUrl);
        }
    }


    public void startDefaultTransition() {
        mNeedTransition = true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mNeedTransition) {
            mNeedTransition = false;
            final Bundle arguments = getArguments();
            if (arguments == null) {
                return;
            }
            View targetView = getView().findViewById(R.id.flPreview);
            // 边框大小
            int frameSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_size);
            // 边距大小
            int marginSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_margin);
            final int screenWidth = getResources().getDisplayMetrics().widthPixels;
            final int screenHeight = getResources().getDisplayMetrics().heightPixels;
            final int statusBarHeight = Utils.getStatusBarHeight(getActivity());
            mStartWidth = arguments.getInt(PhotoViewPagerFragment.PARAM_WIDTH, frameSize) + marginSize * 2;
            mStartHeight = arguments.getInt(PhotoViewPagerFragment.PARAM_HEIGHT, frameSize) + marginSize * 2;
            final int targetLeft = (screenWidth - frameSize) / 2;
            final int targetTop = (screenHeight - frameSize) / 2 - statusBarHeight;
            // 起始x位置
            final int startLeft = arguments.getInt(PhotoViewPagerFragment.PARAM_LEFT, targetLeft) - marginSize;
            // 起始y位置
            final int startTop = arguments.getInt(PhotoViewPagerFragment.PARAM_TOP, targetTop) - marginSize - statusBarHeight;
            final ValueAnimator widthAnimator = ValueAnimator.ofObject(new WidthEvaluator(targetView), mStartWidth, frameSize);
            final ValueAnimator heightAnimator = ValueAnimator.ofObject(new HeightEvaluator(targetView), mStartHeight, frameSize);
            final ValueAnimator xAnimator = ValueAnimator.ofObject(new XEvaluator(targetView), startLeft, targetLeft);
            final ValueAnimator yAnimator = ValueAnimator.ofObject(new YEvaluator(targetView), startTop, targetTop);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(new AccelerateInterpolator());
            animatorSet.playTogether(widthAnimator, heightAnimator, xAnimator, yAnimator);
            animatorSet.setDuration(400).start();
            final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvPreview, RevealCircleImageView.RADIUS,
                    0, (frameSize - marginSize * 2) / 2);
            final ObjectAnimator animator1 = ObjectAnimator.ofFloat(getView().findViewById(R.id.bg), View.ALPHA, 0, 1);
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
                    ((AppCompatActivity) getActivity()).getSupportActionBar().show();
                    startGetImage();
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PhotoViewPagerFragment.Callback) {
            mActivityCallback = ((PhotoViewPagerFragment.Callback) context);
        }
    }
}
