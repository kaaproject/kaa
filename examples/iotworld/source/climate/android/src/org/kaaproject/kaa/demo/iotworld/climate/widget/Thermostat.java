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
package org.kaaproject.kaa.demo.iotworld.climate.widget;

import org.kaaproject.kaa.demo.iotworld.climate.R;
import org.kaaproject.kaa.demo.iotworld.climate.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.climate.util.FontUtils.FontType;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class Thermostat extends View {

    private static final String TAG = Thermostat.class.getSimpleName();
    private static final int INVALID_PROGRESS_VALUE = -1;
    
    private static final int CHECK_PRESSED_MESSAGE = 1001;
    private static final int UPDATE_ON_CONTROL_MESSAGE = 1002;
    
    // The initial rotational offset -90 means that we start at 12 o'clock.
    private final int mAngleOffset = -90;
    
    private int mMinSize = 300;
    
    private int mTempMin = 0;
    
    private int mTempMax = 100;
    
    private CharSequence mIdleText;
    private CharSequence mCoolingText;
    private CharSequence mHeatingText;
    
    private float mTempDist;
    

    private int mArcColorCold = -1;
    private int mArcColor = -1;
    private int mArcColorHeat = -1;

    private int mProgressColorCold = -1;
    private int mProgressColor = -1;
    private int mProgressColorHeat = -1;
    
    private int mBackgroundColorCold = -1;
    private int mBackgroundColor = -1;
    private int mBackgroundColorHeat = -1;
    
    private boolean mEnableBlur = true;
    
    

    /**
     * The maximum value the Thermostat can be set to.
     */
    private int mMax = 100;
    
    /**
     * The current value the Thermostat is set to.
     */
    private int mProgress = 0;
    
    private int mTargetProgress = 0;
    
    private boolean mIsOperating = false;
        
    /**
     * The width of the progress line for the Thermostat.
     */
    private int mProgressWidth = 100;
    
    /**
     * The Width of the background arc for the Thermostat. 
     */
    private int mArcWidth = 100;
    
    /**
     * The angle to start drawing the arc from.
     */
    private int mStartAngle = 0;
    
    /**
     * The angle through which to draw the arc (max is 360).
     */
    private int mSweepAngle = 360;
    
    /**
     * The rotation of the Thermostat - 0 is twelve o'clock.
     */
    private int mRotation = 0;
    
    /**
     * Gives the Thermostat rounded edges.
     */
    private boolean mRoundedEdges = false;
    
    /**
     * Enables the touch inside the Thermostat.
     */
    private boolean mTouchInside = true;
    
    /**
     * Specifies whether the progress increases clockwise or anti-clockwise.
     */
    private boolean mClockwise = true;

    // Internal variables.
    private int mArcRadius = 0;
    private float mProgressSweep = 0;
    private float mTargetProgressSweep = 0;
    private RectF mArcRect = new RectF();
    private RectF mTargetThumbArcRect = new RectF();
    private RectF mControlUpRect = new RectF();
    private RectF mControlUpTouchRect = new RectF();
    private RectF mControlDownRect = new RectF();
    private RectF mControlDownTouchRect = new RectF();
    private boolean mControlUpPressed = false;
    private boolean mControlDownPressed = false;
    private Handler mHandler;
    private Paint mBackgroundPaint;
    private Paint mArcPaint;
    private float mOffInterval;
    private float mOnInterval;
    private Paint mArcPaintGlow;
    private Paint mProgressPaint;
    private Paint mProgressPaintGlow;
    //private PathEffect mProgressArcEffect;
    private Paint mProgressThumbPaint;
    private Paint mProgressThumbPaintGlow;
    private Paint mTargetThumbPaint;
    private Paint mTargetThumbPaintGlow;
    private Paint mTextPaint;
    private Paint mTextPaintGlow;
    private Paint mTargetTextPaint;
    private Paint mTargetTextPaintGlow;
    private Paint mTempTextPaint;
    private Paint mTempTextPaintGlow;
    private Paint mControlPaint;
    private Paint mControlPaintGlow;
    private int mTranslateX;
    private int mTranslateY;
    private int mTextXPos;
    private int mTextYPos;
    private double mTouchAngle;
    private float mTouchIgnoreRadius;
    private OnThermostatChangeListener mOnThermostatChangeListener;

    public interface OnThermostatChangeListener {

        /**
         * Notifies that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         * 
         * @param Thermostat
         *            The Thermostat whose progress has changed.
         * @param progress
         *            The current progress level. This will be in the range
         *            0..max where max was set by
         *            {@link ProgressArc#setMax(int)}. (The default value for
         *            max is 100.)
         * @param fromUser
         *            True if the progress change was initiated by the user.
         */
        void onProgressChanged(Thermostat Thermostat, int progress, boolean fromUser);
        
        void onTargetProgressChanged(Thermostat Thermostat, int targetProgress, boolean fromUser);

        /**
         * Notifies that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         * 
         * @param Thermostat
         *            The Thermostat in which a touch gesture began.
         */
        void onStartTrackingTouch(Thermostat Thermostat);

        /**
         * Notifies that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the Thermostat.
         * 
         * @param Thermostat
         *            The Thermostat in which a touch gesture began.
         */
        void onStopTrackingTouch(Thermostat Thermostat);
    }
    
    private class ThermostatHandlerCallback implements Callback {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case CHECK_PRESSED_MESSAGE:
					if (!mControlUpPressed && !mControlDownPressed) {
						setPressed(false);
					}
					return true;
				case UPDATE_ON_CONTROL_MESSAGE:
					if (mControlUpPressed || mControlDownPressed) {
						updateOnControl();
						mHandler.sendEmptyMessageDelayed(UPDATE_ON_CONTROL_MESSAGE, 100);
					}
					return true;
				default:
					return false;
			}
		}
    	
    }

    public Thermostat(Context context) {
        super(context);
        init(context, null, 0);
    }

    public Thermostat(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.thermostatStyle);
    }

    public Thermostat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        Log.d(TAG, "Initialising Thermostat");
        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;

        // Defaults, may need to link this into theme settings.
        
        mArcColorCold = res.getColor(R.color.thermostat_arc_cold);
        mArcColor = res.getColor(R.color.thermostat_arc);
        mArcColorHeat  = res.getColor(R.color.thermostat_arc_heat);
        mProgressColorCold = res.getColor(R.color.thermostat_progress_cold);
        mProgressColor = res.getColor(R.color.thermostat_progress);
        mProgressColorHeat = res.getColor(R.color.thermostat_progress_heat);
        mBackgroundColorCold = getResources().getColor(R.color.transparent);
        mBackgroundColor = getResources().getColor(R.color.transparent);
        mBackgroundColorHeat = getResources().getColor(R.color.transparent);
        
        mIdleText = getResources().getText(R.string.idle);
        mCoolingText = getResources().getText(R.string.cooling);
        mHeatingText = getResources().getText(R.string.heating);

        int thumbsColor = res.getColor(R.color.thermostat_thumbs_color);
        int thumbsGlowColor = res.getColor(R.color.thermostat_thumbs_glow_color);
        int controlsColor = res.getColor(R.color.thermostat_controls_color);
        
        // Convert the progress width to pixels for the current density.
        mProgressWidth = (int) (mProgressWidth * density);
        
        mHandler = new Handler(new ThermostatHandlerCallback());
        
        if (attrs != null) {
            
            // The attribute initialization.
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.Thermostat, defStyle, 0);

            mMinSize = a.getDimensionPixelSize(R.styleable.Thermostat_minSize, mMinSize);
            setMinimumHeight(mMinSize);
            setMinimumWidth(mMinSize);

            mMax = a.getInteger(R.styleable.Thermostat_max, mMax);
            mProgress = a.getInteger(R.styleable.Thermostat_progress, mProgress);
            mProgressWidth = (int) a.getDimension(
                    R.styleable.Thermostat_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.Thermostat_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.Thermostat_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.Thermostat_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.Thermostat_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.Thermostat_roundEdges,
                    mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.Thermostat_touchInside,
                    mTouchInside);
            mClockwise = a.getBoolean(R.styleable.Thermostat_clockwise,
                    mClockwise);
            
            mArcColorCold = a.getColor(R.styleable.Thermostat_arcColorCold, mArcColorCold);
            mArcColor = a.getColor(R.styleable.Thermostat_arcColor, mArcColor);
            mArcColorHeat = a.getColor(R.styleable.Thermostat_arcColorHeat, mArcColorHeat);
            
            mProgressColorCold = a.getColor(R.styleable.Thermostat_progressColorCold, mProgressColorCold);
            mProgressColor = a.getColor(R.styleable.Thermostat_progressColor, mProgressColor);
            mProgressColorHeat = a.getColor(R.styleable.Thermostat_progressColorHeat, mProgressColorHeat);

            mBackgroundColorCold = a.getColor(R.styleable.Thermostat_backgroundColorCold,
                    mBackgroundColorCold);
            mBackgroundColor = a.getColor(R.styleable.Thermostat_backgroundColor,
                    mBackgroundColor);
            mBackgroundColorHeat = a.getColor(R.styleable.Thermostat_backgroundColorHeat,
                    mBackgroundColorHeat);
            

            thumbsColor = a.getColor(R.styleable.Thermostat_thumbsColor,
                    thumbsColor);

            thumbsGlowColor = a.getColor(R.styleable.Thermostat_thumbsGlowColor,
                    thumbsGlowColor);
            
            controlsColor = a.getColor(R.styleable.Thermostat_controlsColor,
                    controlsColor);
            
            mEnableBlur = a.getBoolean(R.styleable.Thermostat_enableBlur,
                    mEnableBlur);
            
            mTempMin = a.getInteger(R.styleable.Thermostat_tempMin, mTempMin);
            mTempMax = a.getInteger(R.styleable.Thermostat_tempMax, mTempMax);
            
            CharSequence text = a.getText(R.styleable.Thermostat_idleText);
            if (text != null) {
                mIdleText = text;
            }
            
            text = a.getText(R.styleable.Thermostat_coolingText);
            if (text != null) {
                mCoolingText = text;
            }

            text = a.getText(R.styleable.Thermostat_heatingText);
            if (text != null) {
                mHeatingText = text;
            }

            a.recycle();
        }

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mOnInterval = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.5f, getResources().getDisplayMetrics());
        mOffInterval = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2.5f, getResources().getDisplayMetrics());
        
        float outer_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5.56f, getResources().getDisplayMetrics());
        float inner_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.33f, getResources().getDisplayMetrics());

        float text_inner_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.66f, getResources().getDisplayMetrics());
        float text_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3.33f, getResources().getDisplayMetrics());

        float text_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, getResources().getDisplayMetrics());
        float text_size_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22f, getResources().getDisplayMetrics());

        float temp_text_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20f, getResources().getDisplayMetrics());
        mTempDist = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 4f, getResources().getDisplayMetrics());
        
        float target_text_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 50f, getResources().getDisplayMetrics());
        float target_text_size_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 54f, getResources().getDisplayMetrics());     
        float target_text_inner_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5f, getResources().getDisplayMetrics());
        float target_text_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

        float control_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
        float control_size_blur = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
        
        PathEffect arcEffect = new DashPathEffect(new float[] {mOnInterval,mOffInterval}, 0);
        
        Typeface tf = FontUtils.getTypeface(context, FontType.RAJDHANI_BOLD, false);
        
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setPathEffect(arcEffect);
        mArcPaint.setStrokeWidth(mArcWidth);
        //mArcPaint.setAlpha(45);

        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setPathEffect(arcEffect);
        mProgressPaint.setStrokeWidth(mProgressWidth);

        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        mProgressThumbPaint = new Paint();
        mProgressThumbPaint.setColor(thumbsColor);
        mProgressThumbPaint.setAntiAlias(true);
        mProgressThumbPaint.setStyle(Paint.Style.STROKE);
        mProgressThumbPaint.setStrokeWidth(mProgressWidth);

        mTargetThumbPaint = new Paint();
        mTargetThumbPaint.setColor(thumbsColor);
        mTargetThumbPaint.setAntiAlias(true);
        mTargetThumbPaint.setStyle(Paint.Style.STROKE);
        mTargetThumbPaint.setStrokeWidth(mProgressWidth*1.4f);

        mTextPaint = new Paint();
        mTextPaint.setColor(thumbsColor);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(text_size);
        mTextPaint.setTypeface(tf);
        mTextPaint.setTextAlign(Align.CENTER);

        mTargetTextPaint = new Paint();
        mTargetTextPaint.setColor(thumbsColor);
        mTargetTextPaint.setAntiAlias(true);
        mTargetTextPaint.setTextSize(target_text_size);
        mTargetTextPaint.setTypeface(tf);
        mTargetTextPaint.setTextAlign(Align.CENTER);

        mTempTextPaint = new Paint();
        mTempTextPaint.setColor(thumbsColor);
        mTempTextPaint.setAntiAlias(true);
        mTempTextPaint.setTextSize(temp_text_size);
        mTempTextPaint.setTypeface(tf);
        mTempTextPaint.setTextAlign(Align.CENTER);
        
        mControlPaint = new Paint();
        mControlPaint.setAntiAlias(true);
        mControlPaint.setColor(controlsColor);
        mControlPaint.setStyle(Paint.Style.STROKE);
        mControlPaint.setStrokeWidth(control_size);
        mControlPaint.setStrokeCap(Paint.Cap.ROUND);


        if (mEnableBlur) {
            
            mArcPaint.setMaskFilter(new BlurMaskFilter(inner_blur, Blur.NORMAL));
            mProgressPaint.setMaskFilter(new BlurMaskFilter(inner_blur, Blur.NORMAL));
            mProgressThumbPaint.setMaskFilter(new BlurMaskFilter(inner_blur, Blur.NORMAL));
            mTargetThumbPaint.setMaskFilter(new BlurMaskFilter(inner_blur, Blur.NORMAL));
            mTextPaint.setMaskFilter(new BlurMaskFilter(text_inner_blur, Blur.NORMAL));
            mTargetTextPaint.setMaskFilter(new BlurMaskFilter(target_text_inner_blur, Blur.NORMAL));
            mTempTextPaint.setMaskFilter(new BlurMaskFilter(text_inner_blur, Blur.NORMAL));
            mControlPaint.setMaskFilter(new BlurMaskFilter(inner_blur, Blur.NORMAL));
            
            mArcPaintGlow = new Paint();
            mArcPaintGlow.set(mArcPaint);
            mArcPaintGlow.setStrokeWidth(mArcWidth*1.2f);
            mArcPaintGlow.setMaskFilter(new BlurMaskFilter(outer_blur, Blur.NORMAL));
            
            mProgressPaintGlow = new Paint();
            mProgressPaintGlow.set(mProgressPaint);
            mProgressPaintGlow.setStrokeWidth(mProgressWidth*1.2f);
            mProgressPaintGlow.setMaskFilter(new BlurMaskFilter(outer_blur, Blur.NORMAL));
    
            mProgressThumbPaintGlow = new Paint();
            mProgressThumbPaintGlow.set(mProgressThumbPaint);
            mProgressThumbPaintGlow.setColor(thumbsGlowColor);
            mProgressThumbPaintGlow.setStrokeWidth(mProgressWidth*1.2f);
            mProgressThumbPaintGlow.setMaskFilter(new BlurMaskFilter(outer_blur, Blur.NORMAL));
            
            mTargetThumbPaintGlow = new Paint();
            mTargetThumbPaintGlow.set(mTargetThumbPaint);
            mTargetThumbPaintGlow.setColor(thumbsGlowColor);
            mTargetThumbPaintGlow.setStrokeWidth(mProgressWidth*1.4f*1.2f);
            mTargetThumbPaintGlow.setMaskFilter(new BlurMaskFilter(outer_blur, Blur.NORMAL));
            
            mTextPaintGlow = new Paint();
            mTextPaintGlow.set(mTextPaint);
            mTextPaintGlow.setTextSize(text_size_blur);
            mTextPaintGlow.setColor(thumbsGlowColor);
            mTextPaintGlow.setMaskFilter(new BlurMaskFilter(text_blur, Blur.NORMAL));
            
            mTargetTextPaintGlow = new Paint();
            mTargetTextPaintGlow.set(mTargetTextPaint);
            mTargetTextPaintGlow.setTextSize(target_text_size_blur);
            mTargetTextPaintGlow.setColor(thumbsGlowColor);
            mTargetTextPaintGlow.setMaskFilter(new BlurMaskFilter(target_text_blur, Blur.NORMAL));
            
            mTempTextPaintGlow = new Paint();
            mTempTextPaintGlow.set(mTempTextPaint);
            mTempTextPaintGlow.setColor(thumbsGlowColor);
            mTempTextPaintGlow.setMaskFilter(new BlurMaskFilter(text_blur, Blur.NORMAL));
            
            mControlPaintGlow = new Paint();
            mControlPaintGlow.set(mControlPaint);
            mControlPaintGlow.setStrokeWidth(control_size*1.2f);
            mControlPaintGlow.setMaskFilter(new BlurMaskFilter(control_size_blur, Blur.NORMAL));
        }

        updateColors();
    }
    
    private void updateColors () {
        
        int arcColor;
        int progressColor;
        int backgroundColor;
        
        if (mProgress==mTargetProgress || !mIsOperating) {
            arcColor = mArcColor;
            progressColor = mProgressColor;
            backgroundColor = mBackgroundColor;
        }
        else if (mProgress<mTargetProgress) {
            arcColor = mArcColorHeat;
            progressColor = mProgressColorHeat;
            backgroundColor = mBackgroundColorHeat;
        }
        else {
            arcColor = mArcColorCold;
            progressColor = mProgressColorCold;
            backgroundColor = mBackgroundColorCold;
        }
        
        mArcPaint.setColor(arcColor);
        mProgressPaint.setColor(progressColor);
        mBackgroundPaint.setColor(backgroundColor);
        
        if (mEnableBlur) {
            mArcPaintGlow.setColor(arcColor);
            mProgressPaintGlow.setColor(progressColor);
        }
        
    }
    
    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas) {      
        
        canvas.drawCircle(mArcRect.centerX(), mArcRect.centerY(), Math.min(getWidth(), getHeight())/2, mBackgroundPaint);
        
        if(!mClockwise) {
            canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY() );
        }
        
        // Draw the arcs.
        final int arcStart = mStartAngle + mAngleOffset + mRotation;
        final int arcSweep = mSweepAngle;
        
        if (mEnableBlur) {
            canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaintGlow);
        }
        canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
        
        if (mEnableBlur) {
            canvas.drawArc(mArcRect, arcStart+mTargetProgressSweep, mProgressSweep-mTargetProgressSweep, false,
                    mProgressPaintGlow);
        }
        canvas.drawArc(mArcRect, arcStart+mTargetProgressSweep, mProgressSweep-mTargetProgressSweep, false,
                mProgressPaint);
            
        //Draw the progress thumb.
        if (mEnableBlur) {
            canvas.drawArc(mArcRect, arcStart+mProgressSweep-1.5f, 3, false, mProgressThumbPaintGlow);
        }
        canvas.drawArc(mArcRect, arcStart+mProgressSweep-1.5f, 3, false, mProgressThumbPaint);

        if (mEnableBlur) {
            canvas.drawArc(mTargetThumbArcRect, arcStart+mTargetProgressSweep-1.5f, 3, false, mTargetThumbPaintGlow);
        }
        canvas.drawArc(mTargetThumbArcRect, arcStart+mTargetProgressSweep-1.5f, 3, false, mTargetThumbPaint);
        
        int xPos = mTranslateX-mTextXPos;
        if (mEnableBlur) {
            int yPos = (int)(mTranslateY-mTextYPos - ((mTextPaintGlow.descent() + mTextPaintGlow.ascent()) / 2)); 
            canvas.drawText(getTargetTemp()+"", xPos, yPos, mTextPaintGlow);
        }
        
        int yPos = (int)(mTranslateY-mTextYPos - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
        canvas.drawText(getTargetTemp()+"", xPos, yPos, mTextPaint);
        
        float innerTextHeight = mTargetTextPaintGlow.descent() + Math.abs(mTargetTextPaintGlow.ascent()) + 
                mTempDist + mTempTextPaintGlow.descent() + Math.abs(mTempTextPaintGlow.ascent());
        
        float innerTextTranslateY = mTranslateY + innerTextHeight/2 - (mTargetTextPaintGlow.descent() + Math.abs(mTargetTextPaintGlow.ascent()))/2;

        if (mEnableBlur) {
            yPos = (int)(innerTextTranslateY - ((mTargetTextPaintGlow.descent() + mTargetTextPaintGlow.ascent()) / 2));
            canvas.drawText(getDisplayTemp()+"", mTranslateX, yPos, mTargetTextPaintGlow);
        }
        
        yPos = (int)(innerTextTranslateY - ((mTargetTextPaint.descent() + mTargetTextPaint.ascent()) / 2));
        canvas.drawText(getDisplayTemp()+"", mTranslateX, yPos, mTargetTextPaint);
        
        float mTempY;
        if (mEnableBlur) {
            mTempY = innerTextTranslateY + mTargetTextPaintGlow.descent() + mTargetTextPaintGlow.ascent() - mTempDist;
        }
        else {
            mTempY = innerTextTranslateY + mTargetTextPaint.descent() + mTargetTextPaint.ascent() - mTempDist;
        }
        
        String text = mIdleText.toString();
        if (mIsOperating && mProgress != mTargetProgress) {
            text = mProgress < mTargetProgress ? mHeatingText.toString() : mCoolingText.toString();
        }
        text = text.toUpperCase();
        
        if (mEnableBlur) { 
            yPos = (int)(mTempY - ((mTempTextPaintGlow.descent() + mTempTextPaintGlow.ascent()) / 2));
            canvas.drawText(text, mTranslateX, yPos, mTempTextPaintGlow);
        }

        yPos = (int)(mTempY - ((mTempTextPaint.descent() + mTempTextPaint.ascent()) / 2));
        canvas.drawText(text, mTranslateX, yPos, mTempTextPaint);
        
        if (mEnableBlur) { 
            canvas.drawLine(mControlUpRect.left, 
                    mControlUpRect.bottom, 
                    mControlUpRect.left+mControlUpRect.width()/2f, 
                    mControlUpRect.top, mControlPaintGlow);
            canvas.drawLine(mControlUpRect.left+mControlUpRect.width()/2f, 
                    mControlUpRect.top, 
                    mControlUpRect.right, 
                    mControlUpRect.bottom, mControlPaintGlow);

            canvas.drawLine(mControlDownRect.left, 
                    mControlDownRect.top, 
                    mControlDownRect.left+mControlDownRect.width()/2f, 
                    mControlDownRect.bottom, mControlPaintGlow);
            canvas.drawLine(mControlDownRect.left+mControlDownRect.width()/2f, 
                    mControlDownRect.bottom, 
                    mControlDownRect.right, 
                    mControlDownRect.top, mControlPaintGlow);
        }

        canvas.drawLine(mControlUpRect.left, 
                mControlUpRect.bottom, 
                mControlUpRect.left+mControlUpRect.width()/2f, 
                mControlUpRect.top, mControlPaint);
        canvas.drawLine(mControlUpRect.left+mControlUpRect.width()/2f, 
                mControlUpRect.top, 
                mControlUpRect.right, 
                mControlUpRect.bottom, mControlPaint);

        canvas.drawLine(mControlDownRect.left, 
                mControlDownRect.top, 
                mControlDownRect.left+mControlDownRect.width()/2f, 
                mControlDownRect.bottom, mControlPaint);
        canvas.drawLine(mControlDownRect.left+mControlDownRect.width()/2f, 
                mControlDownRect.bottom, 
                mControlDownRect.right, 
                mControlDownRect.top, mControlPaint);
        
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int height = getDefaultSize(getSuggestedMinimumHeight(),
                heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(),
                widthMeasureSpec);
        final int min = Math.min(width, height);
        width = min;
        height = min;
        float top = 0;
        float left = 0;
        
        int arcDiameter = 0;

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);
        
        arcDiameter = min - getPaddingLeft() - mArcWidth;
        mArcRadius = arcDiameter / 2;
        top = height / 2 - (arcDiameter / 2);
        left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);
        mTargetThumbArcRect.set(left + mProgressWidth*0.2f, top + mProgressWidth*0.2f, 
                left + arcDiameter - mProgressWidth*0.2f, top + arcDiameter - mProgressWidth*0.2f);
        
        float controlWidth = arcDiameter/14f;
        float controlHeight = arcDiameter/28f;
        float controlDistance = arcDiameter/3.5f;
        
        mControlUpRect.set(mArcRect.centerX()-controlWidth/2f, 
                mArcRect.centerY()-controlDistance-controlHeight/2, 
                mArcRect.centerX()+controlWidth/2f, 
                mArcRect.centerY()-controlDistance+controlHeight/2);
        mControlUpTouchRect.set(mControlUpRect.left-mControlUpRect.width()*2,
                mControlUpRect.top-mControlUpRect.height()*2,
                mControlUpRect.right+mControlUpRect.width()*2,
                mControlUpRect.bottom+mControlUpRect.height()*2);
        
        mControlDownRect.set(mArcRect.centerX()-controlWidth/2f, 
                mArcRect.centerY()+controlDistance-controlHeight/2, 
                mArcRect.centerX()+controlWidth/2f, 
                mArcRect.centerY()+controlDistance+controlHeight/2);
        mControlDownTouchRect.set(mControlDownRect.left-mControlDownRect.width()*2,
                mControlDownRect.top-mControlDownRect.height()*2,
                mControlDownRect.right+mControlDownRect.width()*2,
                mControlDownRect.bottom+mControlDownRect.height()*2);
        
    
        //int arcStart = (int)mProgressSweep + mStartAngle  + mRotation + 90;
        int arcStart = (int)mTargetProgressSweep + mStartAngle  + mRotation + 90;
        mTextXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcStart-10)));
        mTextYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcStart-10)));
        
        setTouchInSide(mTouchInside);
        setMeasuredDimension(width, height);
        //super.onMeasure(width, height);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return true;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	mHandler.removeMessages(CHECK_PRESSED_MESSAGE);
        	setPressed(true);
            if (!onControlDown(event)) {
                onStartTrackingTouch();
                updateOnTouch(event, false);
            } else {
                mHandler.sendEmptyMessageDelayed(UPDATE_ON_CONTROL_MESSAGE, 500);
            }
            break;
        case MotionEvent.ACTION_MOVE:
            if (!mControlUpPressed && !mControlDownPressed) {
                updateOnTouch(event, false);
            }
            break;
        case MotionEvent.ACTION_UP:
            if (!mControlUpPressed && !mControlDownPressed) {
                updateOnTouch(event, true);
                onStopTrackingTouch();
            }
            else if (onControlUp(event)) {
            	updateOnControl();            	
            }
            mHandler.sendEmptyMessageDelayed(CHECK_PRESSED_MESSAGE, 1000);
            mHandler.removeMessages(UPDATE_ON_CONTROL_MESSAGE);
            mControlUpPressed = false;
            mControlDownPressed = false;
            break;
        case MotionEvent.ACTION_CANCEL:
            onStopTrackingTouch();
            setPressed(false);

            break;
        }

        return true;
    }
    
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
    
    private boolean onControlDown(MotionEvent event) {
        if (mControlUpTouchRect.contains(event.getX(), event.getY())) {
            mControlUpPressed = true;
        }
        else if (mControlDownTouchRect.contains(event.getX(), event.getY())) {
            mControlDownPressed = true;
        }
        return mControlUpPressed || mControlDownPressed;
    }
    
    private boolean onControlUp(MotionEvent event) {
        if (mControlUpTouchRect.contains(event.getX(), event.getY()) && mControlUpPressed) {
            return true;
        }
        else if (mControlDownTouchRect.contains(event.getX(), event.getY()) && mControlDownPressed) {
            return true;
        }
        return false;
    }
 
    private void onStartTrackingTouch() {
        if (mOnThermostatChangeListener != null) {
            mOnThermostatChangeListener.onStartTrackingTouch(this);
        }
    }

    private void onStopTrackingTouch() {
        if (mOnThermostatChangeListener != null) {
            mOnThermostatChangeListener.onStopTrackingTouch(this);
        }
    }
    
    private void updateOnControl() {
        if (mControlUpPressed) {
            updateTargetProgress(mTargetProgress+1, true, true);
        }
        else if (mControlDownPressed) {
            updateTargetProgress(mTargetProgress-1, true, true);
        }
    }

    private void updateOnTouch(MotionEvent event, boolean notifyListeners) {
        boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
        if (ignoreTouch) {
            return;
        }
        mTouchAngle = getTouchDegrees(event.getX(), event.getY());
        int targetProgress = getProgressForAngle(mTouchAngle);
        onTargetProgressRefresh(targetProgress, true, notifyListeners);
    }

    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (touchRadius < mTouchIgnoreRadius) {
            ignore = true;
        }
        return ignore;
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        
        // Invert the x-coord if rotating anti-clockwise.
        x= (mClockwise) ? x:-x;
        // Convert to the arc angle.
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? 0
                : touchProgress;
        touchProgress = (touchProgress > mMax) ? mMax
                : touchProgress;
        return touchProgress;
    }

    private float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }
    
    private void onTargetProgressRefresh(int targetProgress, boolean fromUser, boolean notifyListeners) {
        updateTargetProgress(targetProgress, fromUser, notifyListeners);
    }

    private void updateThumbPosition() {
        //int thumbAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
    	int thumbAngle = (int) (mStartAngle + mTargetProgressSweep + mRotation + 90);
        mTextXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbAngle-10)));
        mTextYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbAngle-10)));
    }
    
    private void updateProgress(int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        progress = (progress > mMax) ? mMax : progress;
        progress = (mProgress < 0) ? 0 : progress;

        mProgress = progress;
          if (mOnThermostatChangeListener != null) {
                mOnThermostatChangeListener
                        .onProgressChanged(this, progress, fromUser);
        }
        
        mProgressSweep = (float) progress / (float)mMax * mSweepAngle;

        updateThumbPosition();
        
        updateColors();

        invalidate();
    }
    
    private void updateTargetProgress(int targetProgress, boolean fromUser, boolean notifyListeners) {

        if (targetProgress == INVALID_PROGRESS_VALUE) {
            return;
        }
        
        targetProgress = (targetProgress > mMax) ? mMax : targetProgress;
        targetProgress = (mTargetProgress < 0) ? 0 : targetProgress;

        mTargetProgress = targetProgress;

        if (mOnThermostatChangeListener != null && notifyListeners) {
              mOnThermostatChangeListener
                      .onTargetProgressChanged(this, targetProgress, fromUser);
        }
        
        mTargetProgressSweep = (float) targetProgress / (float)mMax * mSweepAngle;
        
        float angleSweep = 0;
        float fullPhase = 0;
        if (mTargetProgressSweep<mProgressSweep) {
            angleSweep = mTargetProgressSweep;
        }
        else {
            angleSweep = mSweepAngle - mTargetProgressSweep;
            float fullLength = (float) (Math.toRadians(mSweepAngle))*(float)(mArcRadius);
            fullPhase = fullLength % (mOnInterval+mOffInterval);
        }
        float length = (float) (Math.toRadians(angleSweep))*(float)(mArcRadius);
        float phaseValue = length % (mOnInterval+mOffInterval) + fullPhase;
        
        PathEffect arcEffect = new DashPathEffect(new float[] {mOnInterval,mOffInterval}, phaseValue);
        if (mEnableBlur) {
            mProgressPaintGlow.setPathEffect(arcEffect);
        }
        mProgressPaint.setPathEffect(arcEffect);
        
        updateThumbPosition();
        
        updateColors();

        invalidate();
    }

    /**
     * Sets a listener to receive notifications of changes to the Thermostat
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the Thermostat.
     * 
     * @param l
     *            The seek bar notification listener.
     * 
     * @see Thermostat.OnSeekBarChangeListener
     */
    public void setOnThermostatChangeListener(OnThermostatChangeListener l) {
        mOnThermostatChangeListener = l;
    }
    
    public void setTempMin(int tempMin) {
        mTempMin = tempMin;
    }

    public void setTempMax(int tempMax) {
        mTempMax = tempMax;
    }
    
    public void setTemp(int temp) {
        updateProgress(temp-mTempMin, false);
    }
    
    public int getTemp() {
        return mTempMin+mProgress;
    }

    public void setProgress(int progress) {
        updateProgress(progress, false);
    }
    
    public void setTargetTemp(int temp, boolean notifyListeners) {
        updateTargetProgress(temp-mTempMin, false, notifyListeners);
    }
    
    public int getTargetTemp() {
        return mTempMin+mTargetProgress;
    }

    private int getDisplayTemp() {
    	if (isPressed()) {
            return getTargetTemp();
    	} else {
    		return getTemp();
    	}
    }

    public void setOperating(boolean isOperating) {
        if (mIsOperating != isOperating) {
            mIsOperating = isOperating;
            updateColors();
            invalidate();
        }
    }
    
    public void setTargetProgress(int targetProgress, boolean notifyListeners) {
        updateTargetProgress(targetProgress, false, notifyListeners);
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressPaint.setStrokeWidth(mProgressWidth);
    }
    
    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }
    public int getArcRotation() {
        return mRotation;
    }

    public void setArcRotation(int mRotation) {
        this.mRotation = mRotation;
        updateThumbPosition();
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        updateThumbPosition();
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
        updateThumbPosition();
    }
    
    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressPaint.setStrokeCap(Paint.Cap.SQUARE);
        }
    }
    
    public void setTouchInSide(boolean isEnabled) {
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius as it makes interaction too tricky.
            mTouchIgnoreRadius = mArcRadius;
        }
    }
    
    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }
}
