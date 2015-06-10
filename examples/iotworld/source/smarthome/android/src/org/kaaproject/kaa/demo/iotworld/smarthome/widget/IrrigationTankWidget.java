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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;

public class IrrigationTankWidget extends AbstractIrrigationWidget {
    
    private final static int DEFAULT_WAVES_COUNT = 5;
    
    private int mWavesCount;
    
    private RectF mTankRect = new RectF();
    private Path mBackgroundPath = new Path();
    private Path mStrokePath = new Path();    
    private PointF mStartPoint = new PointF();
    private PointF mEndPoint = new PointF();
    private PointF mMidPoint = new PointF();    
    
    public IrrigationTankWidget(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IrrigationTankWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IrrigationTankWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IrrigationTankWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void init(final Context context, final AttributeSet attrs) {
        super.init(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IrrigationTankWidget);
        mWavesCount = typedArray.getInteger(R.styleable.IrrigationTankWidget_wavesCount, DEFAULT_WAVES_COUNT);
        typedArray.recycle();
    }
    
    @Override
    protected void onMeasureImpl(int width, int height) {
        int rectHeight = width - getPaddingLeft();
        int rectWidth = (int) (0.9f*rectHeight);
        
        int top = height / 2 - (rectHeight / 2);
        int left = width / 2 - (rectWidth / 2);
        mTankRect.set(left, top, left + rectWidth, top + rectHeight);
        
        float backgroundTopOffset = 0.1f*height;
        float strokeOffset = mStrokeWidth*0.5f;
        
        mBackgroundPath.reset();
        mStrokePath.reset();
        
        mBackgroundPath.moveTo(mTankRect.right, mTankRect.top + backgroundTopOffset);
        mBackgroundPath.lineTo(mTankRect.right, mTankRect.bottom);
        mBackgroundPath.lineTo(mTankRect.left, mTankRect.bottom);
        mBackgroundPath.lineTo(mTankRect.left, mTankRect.top + backgroundTopOffset);
        
        
        float yDeviation = backgroundTopOffset/mWavesCount;
        float yMedian = mTankRect.top + backgroundTopOffset;
        float stepX = mTankRect.width() / mWavesCount;
        float startX = mTankRect.left;

        for (int i=0;i<mWavesCount;i++) {
            mStartPoint.set(startX + stepX*i, yMedian + yDeviation*(i%2==0 ? -1f : 1f));
            mEndPoint.set(startX + stepX*(i+1), yMedian + yDeviation*(i%2==0 ? 1f : -1f));
            mMidPoint.set((mStartPoint.x + mEndPoint.x) / 2, (mStartPoint.y + mEndPoint.y) / 2);
            mBackgroundPath.quadTo((mStartPoint.x + mMidPoint.x) / 2, mStartPoint.y, mMidPoint.x, mMidPoint.y);
            mBackgroundPath.quadTo((mMidPoint.x + mEndPoint.x) / 2, mEndPoint.y, mEndPoint.x, mEndPoint.y);
        }

        mBackgroundPath.close();
        
        mStrokePath.moveTo(mTankRect.left+strokeOffset, mTankRect.top+strokeOffset);
        mStrokePath.lineTo(mTankRect.left+strokeOffset, mTankRect.bottom-strokeOffset);
        mStrokePath.lineTo(mTankRect.right-strokeOffset, mTankRect.bottom-strokeOffset);
        mStrokePath.lineTo(mTankRect.right-strokeOffset, mTankRect.top+strokeOffset);
        
    }

    @Override
    protected void drawBackground(Canvas canvas) {
        mPaint.setColor(mFillColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawPath(mBackgroundPath, mPaint);
        
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Style.STROKE);
        canvas.drawPath(mStrokePath, mPaint);
        
    }

    @Override
    protected String getValueText() {
        return mValue + " l";
    }

}
