package com.nd.android.sdp.common.photoviewpager.widget;

import android.animation.IntEvaluator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2015/9/22.
 */
public class XEvaluator extends IntEvaluator {

    private View mView;

    public XEvaluator(View view) {
        mView = view;
    }

    @NonNull
    @Override
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        final ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) mView.getLayoutParams());
        layoutParams.leftMargin = super.evaluate(fraction, startValue, endValue);
        mView.requestLayout();
        return layoutParams.leftMargin;
    }
}
