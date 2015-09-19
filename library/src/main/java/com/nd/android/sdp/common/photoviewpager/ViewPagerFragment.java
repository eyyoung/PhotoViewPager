package com.nd.android.sdp.common.photoviewpager;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nd.android.sdp.common.photoviewpager.getter.ImageGetter;
import com.nd.android.sdp.common.photoviewpager.getter.ImageGetterCallback;
import com.nd.android.sdp.common.photoviewpager.view.ImageSource;
import com.nd.android.sdp.common.photoviewpager.view.SubsamplingScaleImageView;

public class ViewPagerFragment extends Fragment {

    private static final String BUNDLE_URL = "url";
    private static final String PARAM_IMAGE_GETTER = "image_getter";
    private ImageGetter mImageGetter;
    private View mView;

    public void setUrl(String pUrl) {
        mUrl = pUrl;
    }

    private String mUrl;

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
        if (mUrl != null) {
            mImageGetter.startGetImage(mUrl, new ImageGetterCallback() {
                @Override
                public void setImageToView(Bitmap bitmap) {
                    SubsamplingScaleImageView imageView = (SubsamplingScaleImageView) mView.findViewById(R.id.imageView);
                    imageView.setDoubleTapZoomDpi(120);
                    imageView.setMinScale(0.5f);
                    imageView.setImage(ImageSource.cachedBitmap(bitmap));
                }
            });
        }

        return mView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        View rootView = getView();
        if (rootView != null) {
            outState.putString(BUNDLE_URL, mUrl);
        }
    }

}
