package com.nd.android.sdp.common.photoviewpager.widget;

import android.animation.IntEvaluator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Administrator on 2015/9/22.
 */
public class YEvaluator extends IntEvaluator {

    private View mView;

    public YEvaluator(View view) {
        mView = view;
    }

    @NonNull
    @Override
    public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
        final ViewGroup.MarginLayoutParams layoutParams = ((ViewGroup.MarginLayoutParams) mView.getLayoutParams());
        layoutParams.topMargin = super.evaluate(fraction, startValue, endValue);
        mView.requestLayout();
        return layoutParams.leftMargin;
    }
}
