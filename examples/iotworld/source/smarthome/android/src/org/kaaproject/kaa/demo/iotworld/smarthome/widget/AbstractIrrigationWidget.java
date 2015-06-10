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
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils.FontType;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public abstract class AbstractIrrigationWidget extends View {

    private final static int DEFAULT_FILL_COLOR = 0xff579fd7;
    private final static int DEFAULT_STROKE_COLOR = 0xff263238;
    private final static int DEFAULT_TEXT_COLOR = 0xffffffff;
    private final static float DEFAULT_STROKE_WIDTH_DP = 3f;
    private final static float DEFAULT_VALUE = 0f;
    
    //params
    protected int mFillColor;
    protected int mStrokeColor;
    protected float mStrokeWidth;
    //value param
    protected float mValue;
    
    //common fields
    protected Paint mPaint = new Paint();
    private Paint mTextPaint = new Paint();    
    private int mTranslateX;
    private int mTranslateY;
    
    public AbstractIrrigationWidget(Context context) {
        super(context);
        init(context, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AbstractIrrigationWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public AbstractIrrigationWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public AbstractIrrigationWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    protected void init(final Context context, final AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IrrigationWidget);
        
        ColorStateList colorStateList = typedArray.getColorStateList(R.styleable.IrrigationWidget_fillColor);
        if (colorStateList != null) {
            mFillColor = colorStateList.getDefaultColor();
        } else {
            mFillColor = DEFAULT_FILL_COLOR;
        }
        
        colorStateList = typedArray.getColorStateList(R.styleable.IrrigationWidget_strokeColor);
        if (colorStateList != null) {
            mStrokeColor = colorStateList.getDefaultColor();
        } else {
            mStrokeColor = DEFAULT_STROKE_COLOR;
        }
        
        int textColor = DEFAULT_TEXT_COLOR;
        colorStateList = typedArray.getColorStateList(R.styleable.IrrigationWidget_textColor);
        if (colorStateList != null) {
            textColor = colorStateList.getDefaultColor();
        } 
        
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_STROKE_WIDTH_DP, getResources().getDisplayMetrics());
        mStrokeWidth = typedArray.getDimension(R.styleable.IrrigationWidget_strokeWidth, mStrokeWidth);
        
        mValue = typedArray.getFloat(R.styleable.IrrigationWidget_value, DEFAULT_VALUE);
        
        typedArray.recycle();
        
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mStrokeWidth);
        
        mTextPaint.setColor(textColor);
        mTextPaint.setAntiAlias(true);
        
        Typeface tf = null;
        if (isInEditMode()) {
            tf = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
        } else {
            tf = FontUtils.getTypeface(context, FontType.ROBOTO_BOLD, false);
        }
        
        mTextPaint.setTypeface(tf);
        mTextPaint.setTextAlign(Align.CENTER);
    }
    
    public void setValue(float value) {
        mValue = value;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {     
        drawBackground(canvas);
        
        String text = getValueText();

        int xPos = mTranslateX;
        int yPos = (int)(mTranslateY - ((mTextPaint.descent() + mTextPaint.ascent()) / 2));
        
        canvas.drawText(text, xPos, yPos, mTextPaint);
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
        
        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);
        
        if (width > 0) {
            float textWidthMax = width * 0.7f;
            float textWidthMin = width * 0.6f;
            String text = getValueText();
            float textWidth = mTextPaint.measureText(text);
            while(textWidth > textWidthMax) {
                float textSize = mTextPaint.getTextSize() - 1f;
                mTextPaint.setTextSize(textSize);
                textWidth = mTextPaint.measureText(text);
            }
            while(textWidth < textWidthMin) {
                float textSize = mTextPaint.getTextSize() + 1f;
                mTextPaint.setTextSize(textSize);
                textWidth = mTextPaint.measureText(text);
            }
        }
        
        onMeasureImpl(width, height);
        
        setMeasuredDimension(width, height);
    }
    
    protected abstract void onMeasureImpl(int width, int height);
    
    protected abstract void drawBackground(Canvas canvas);
    
    protected abstract String getValueText();

}
