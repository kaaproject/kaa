/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class RippleView extends RelativeLayout {
    
    public static final int SIMPLE_RIPPLE = 0;
    public static final int DOUBLE_RIPPLE = 1;
    public static final int RECTANGLE = 2;

    private int mWidth;
    private int mHeight;
    private int mFrameRate = 10;
    private int mDuration = 400;
    private int mPaintAlpha = 90;
    private Handler mCanvasHandler;
    private float mRadiusMax = 0;
    private boolean mAnimationRunning = false;
    private int mTimer = 0;
    private int mTimerEmpty = 0;
    private int mDurationEmpty = -1;
    private float mX = -1;
    private float mY = -1;
    private int mZoomDuration;
    private float mZoomScale;
    private ScaleAnimation mScaleAnimation;
    private Boolean mHasToZoom;
    private Boolean mIsCentered;
    private Integer mRippleType;
    private Paint mPaint;
    private Bitmap mOriginBitmap;
    private int mRippleColor;
    private int mRipplePadding;
    private GestureDetector mGestureDetector;
    
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
        }
    };

    public RippleView(Context context) {
        super(context);
        init(context, null);
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(final Context context, final AttributeSet attrs) {
        if (isInEditMode())
            return;

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
        mRippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, getResources().getColor(R.color.rippleColor));
        mRippleType = typedArray.getInt(R.styleable.RippleView_rv_type, SIMPLE_RIPPLE);
        mHasToZoom = typedArray.getBoolean(R.styleable.RippleView_rv_zoom, false);
        mIsCentered = typedArray.getBoolean(R.styleable.RippleView_rv_centered, false);
        mDuration = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, mDuration);
        mFrameRate = typedArray.getInteger(R.styleable.RippleView_rv_framerate, mFrameRate);
        mPaintAlpha = typedArray.getInteger(R.styleable.RippleView_rv_alpha, mPaintAlpha);
        mRipplePadding = typedArray.getDimensionPixelSize(R.styleable.RippleView_rv_ripplePadding, 0);
        mCanvasHandler = new Handler();
        mZoomScale = typedArray.getFloat(R.styleable.RippleView_rv_zoomScale, 1.03f);
        mZoomDuration = typedArray.getInt(R.styleable.RippleView_rv_zoomDuration, 200);
        typedArray.recycle();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mRippleColor);
        mPaint.setAlpha(mPaintAlpha);
        this.setWillNotDraw(false);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent event) {
                super.onLongPress(event);
                animateRipple(event);
                sendClickEvent(true);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });

        this.setDrawingCacheEnabled(true);
        this.setClickable(true);
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mAnimationRunning) {
            if (mDuration <= mTimer * mFrameRate) {
                mAnimationRunning = false;
                mTimer = 0;
                mDurationEmpty = -1;
                mTimerEmpty = 0;
                canvas.restore();
                invalidate();
                return;
            } else
                mCanvasHandler.postDelayed(mRunnable, mFrameRate);

            if (mTimer == 0)
                canvas.save();


            canvas.drawCircle(mX, mY, (mRadiusMax * (((float) mTimer * mFrameRate) / mDuration)), mPaint);

            mPaint.setColor(Color.parseColor("#ffff4444"));

            if (mRippleType == DOUBLE_RIPPLE && mOriginBitmap != null && (((float) mTimer * mFrameRate) / mDuration) > 0.4f) {
                if (mDurationEmpty == -1)
                    mDurationEmpty = mDuration - mTimer * mFrameRate;

                mTimerEmpty++;
                final Bitmap tmpBitmap = getCircleBitmap((int) ((mRadiusMax) * (((float) mTimerEmpty * mFrameRate) / (mDurationEmpty))));
                canvas.drawBitmap(tmpBitmap, 0, 0, mPaint);
                tmpBitmap.recycle();
            }

            mPaint.setColor(mRippleColor);

            if (mRippleType == DOUBLE_RIPPLE) {
                if ((((float) mTimer * mFrameRate) / mDuration) > 0.6f)
                    mPaint.setAlpha((int) (mPaintAlpha - ((mPaintAlpha) * (((float) mTimerEmpty * mFrameRate) / (mDurationEmpty)))));
                else
                    mPaint.setAlpha(mPaintAlpha);
            }
            else
                mPaint.setAlpha((int) (mPaintAlpha - ((mPaintAlpha) * (((float) mTimer * mFrameRate) / mDuration))));

            mTimer++;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;

        mScaleAnimation = new ScaleAnimation(1.0f, mZoomScale, 1.0f, mZoomScale, w / 2, h / 2);
        mScaleAnimation.setDuration(mZoomDuration);
        mScaleAnimation.setRepeatMode(Animation.REVERSE);
        mScaleAnimation.setRepeatCount(1);
    }

    public void animateRipple(MotionEvent event) {
        createAnimation(event.getX(), event.getY());
    }

    public void animateRipple(final float x, final float y) {
        createAnimation(x, y);
    }

    private void createAnimation(final float x, final float y) {
        if (!mAnimationRunning) {
            if (mHasToZoom)
                this.startAnimation(mScaleAnimation);

            mRadiusMax = Math.max(mWidth, mHeight);

            if (mRippleType != RECTANGLE) {
                mRadiusMax /= 2;
            }

            mRadiusMax -= mRipplePadding;

            if (mIsCentered || mRippleType == DOUBLE_RIPPLE) {
                this.mX = getMeasuredWidth() / 2;
                this.mY = getMeasuredHeight() / 2;
            } else {
                this.mX = x;
                this.mY = y;
            }

            mAnimationRunning = true;

            if (mRippleType == DOUBLE_RIPPLE && mOriginBitmap == null)
                mOriginBitmap = getDrawingCache(true);

            invalidate();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            animateRipple(event);
            sendClickEvent(false);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        this.onTouchEvent(event);
        return super.onInterceptTouchEvent(event);
    }
    
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if (getParent() instanceof AutoSpanRecyclerView) {
            AutoSpanRecyclerView reyclerView  = (AutoSpanRecyclerView)getParent();
            final int position = reyclerView.getChildPosition(this);
            final long id = reyclerView.getChildItemId(this);
            reyclerView.onCreateContextMenu(this, menu, position, id);
        }
    }

    private void sendClickEvent(final Boolean isLongClick) {
        if (getParent() instanceof AutoSpanRecyclerView) {
            AutoSpanRecyclerView reyclerView  = (AutoSpanRecyclerView)getParent();
            final int position = reyclerView.getChildPosition(this);
            final long id = reyclerView.getChildItemId(this);
            if (isLongClick) {
                reyclerView.onItemLongClick(this, position, id);
            } else {
                reyclerView.onItemClick(this, position, id);
            }
        } else if (getParent() instanceof ListView) {
            final int position = ((ListView) getParent()).getPositionForView(this);
            final long id = ((ListView) getParent()).getItemIdAtPosition(position);
            if (isLongClick) {
                if (((ListView) getParent()).getOnItemLongClickListener() != null)
                    ((ListView) getParent()).getOnItemLongClickListener().onItemLongClick(((ListView) getParent()), this, position, id);
            } else {
                if (((ListView) getParent()).getOnItemClickListener() != null)
                    ((ListView) getParent()).getOnItemClickListener().onItemClick(((ListView) getParent()), this, position, id);
            }
        }
    }

    private Bitmap getCircleBitmap(final int radius) {
        final Bitmap output = Bitmap.createBitmap(mOriginBitmap.getWidth(), mOriginBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect((int)(mX - radius), (int)(mY - radius), (int)(mX + radius), (int)(mY + radius));

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(mX, mY, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(mOriginBitmap, rect, rect, paint);

        return output;
    }
    
    public void setRippleColor(int rippleColor) {
        mRippleColor = rippleColor;
    }

    public int getRippleColor() {
        return mRippleColor;
    }
    
    public void setCentered(boolean isCentered) {
        mIsCentered = isCentered;
    }
    
    public void setDuration(int duration) {
        mDuration = duration;
    }
    
    public void setRippleType(int rippleType) {
        mRippleType = rippleType;
    }
    
    public void setFrameRate(int frameRate) {
        mFrameRate = frameRate;
    }

}