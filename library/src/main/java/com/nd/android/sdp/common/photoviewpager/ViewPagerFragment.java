package com.nd.android.sdp.common.photoviewpager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.kogitune.activity_transition.ActivityTransition;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetter;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nd.android.sdp.common.photoviewpager.view.ImageSource;
import com.nd.android.sdp.common.photoviewpager.view.SubsamplingScaleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.RevealCircleImageView;
import com.nd.android.sdp.common.photoviewpager.widget.RevealImageView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ViewPagerFragment extends Fragment {

    private static final String BUNDLE_URL = "url";
    private static final String PARAM_IMAGE_GETTER = "image_getter";
    private ImageGetter mImageGetter;
    private View mView;
    private CircularProgressView mPb;
    private RevealCircleImageView mIvPreview;
    private RevealImageView mIvTemp;
    private boolean mNeedTransition;
    private Subscription mSubscription;

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

    public static ViewPagerFragment newInstance(Class<? extends ImageGetter> getterClass) {
        ViewPagerFragment viewPagerFragment = new ViewPagerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_IMAGE_GETTER, getterClass);
        viewPagerFragment.setArguments(bundle);
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
                        set.setInterpolator(new AccelerateInterpolator());
                        set.setDuration(300).start();
                        set.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animator) {
                                if (!isAdded()) {
                                    return;
                                }
                                ((ViewGroup) getView()).removeView(mIvTemp);
                                ((ViewGroup) getView()).removeView(mIvPreview);
                                SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) mView.findViewById(R.id.imageView);
                                imageView.setMinimumDpi(120);
                                imageView.setDoubleTapZoomDpi(120);
                                imageView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CUSTOM);
                                imageView.setImage(ImageSource.cachedBitmap(bitmap));
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
//            ActivityTransition.with(getActivity()
//                    .getIntent())
//                    .to(mIvPreview)
//                    .duration(200).start(null);
            mNeedTransition = false;
            int finalSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_size);
            int marginSize = getResources().getDimensionPixelSize(R.dimen.photo_viewpager_preview_margin);
            final ObjectAnimator animator = ObjectAnimator.ofFloat(mIvPreview, RevealCircleImageView.RADIUS,
                    40f, (finalSize - marginSize * 2) / 2);
            final ObjectAnimator animator1 = ObjectAnimator.ofFloat(getView().findViewById(R.id.bg), View.ALPHA, 0, 1);
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animator, animator1);
            set.setInterpolator(new AccelerateInterpolator());
            set.setDuration(300).start();
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
    }
}
