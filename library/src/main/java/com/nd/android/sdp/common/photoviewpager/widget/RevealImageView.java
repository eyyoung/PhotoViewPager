package com.nd.android.sdp.common.photoviewpager.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Property;
import android.widget.ImageView;

public class RevealImageView extends ImageView {

    public static final Property<RevealImageView, Float> RADIUS = new RevealProperty();

    private static class RevealProperty extends Property<RevealImageView, Float> {

        public RevealProperty() {
            super(Float.class, "revealRadius");
        }

        @Override
        public Float get(RevealImageView revealCircleImageView) {
            return revealCircleImageView.mDrawableRadius;
        }

        @Override
        public void set(RevealImageView object, Float value) {
            object.mDrawableRadius = value;
            object.invalidate();
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }
    }

    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    private final RectF mDrawableRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();
    private final Paint mBitmapPaint = new Paint();

    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private int mBitmapWidth;
    private int mBitmapHeight;

    private float mDrawableRadius;

    private ColorFilter mColorFilter;

    private boolean mReady;
    private boolean mSetupPending;

    public RevealImageView(Context context) {
        super(context);

        init();
    }

    public RevealImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mReady = true;

        if (mSetupPending) {
            setup();
            mSetupPending = false;
        }
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        if (adjustViewBounds) {
            throw new IllegalArgumentException("adjustViewBounds not supported.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            return;
        }
        if (getHeight() / mDrawableRect.height() > getWidth() / mDrawableRect.width()) {
            canvas.translate(0, (getHeight() - mDrawableRect.height()) / 2);
        } else {
            canvas.translate((getWidth() - mDrawableRect.width()) / 2, 0);
        }
        canvas.drawRoundRect(mDrawableRect, mDrawableRadius, mDrawableRadius, mBitmapPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setup();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        mBitmap = bm;
        setup();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (cf == mColorFilter) {
            return;
        }

        mColorFilter = cf;
        mBitmapPaint.setColorFilter(mColorFilter);
        invalidate();
    }

    private void setup() {
        if (!mReady) {
            mSetupPending = true;
            return;
        }

        if (getWidth() == 0 && getHeight() == 0) {
            return;
        }

        if (mBitmap == null) {
            invalidate();
            return;
        }

        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);

        mBitmapHeight = mBitmap.getHeight();
        mBitmapWidth = mBitmap.getWidth();

        float scale = 1f;
        if (getWidth() / mBitmapWidth < getHeight() / mBitmapHeight) {
            scale = (float) getWidth() / (float) mBitmapWidth;
        } else {
            scale = (float) getHeight() / (float) mBitmapHeight;
        }
        mDrawableRect.set(0, 0, mBitmapWidth * scale, mBitmapHeight * scale);
        mDrawableRadius = Math.min(mDrawableRect.height() / 2.0f, mDrawableRect.width() / 2.0f);

        updateShaderMatrix();
        invalidate();
    }

    private void updateShaderMatrix() {
        float scale;

        mShaderMatrix.set(null);

        if (mBitmapWidth * getHeight() > getWidth() * mBitmapHeight) {
            scale = getWidth() / (float) mBitmapWidth;
        } else {
            scale = getHeight() / (float) mBitmapHeight;
        }

        mShaderMatrix.setScale(scale, scale);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

}
