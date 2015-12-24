package com.nd.android.sdp.common.photoviewpager.widget;

import android.animation.IntEvaluator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * The type Width evaluator.
 */
public class WidthEvaluator extends IntEvaluator {

    private View mView;

    public WidthEvaluator(View view) {
        mView = view;
    }

    @NonNull
    @Override
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        final ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
        layoutParams.width = super.evaluate(fraction, startValue, endValue);
        mView.requestLayout();
        return layoutParams.width;
    }
}
