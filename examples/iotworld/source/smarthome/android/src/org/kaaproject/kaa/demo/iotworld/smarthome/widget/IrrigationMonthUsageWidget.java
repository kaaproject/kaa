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

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;

public class IrrigationMonthUsageWidget extends AbstractIrrigationWidget {
    
    private RectF mCircleRect = new RectF();
    
    public IrrigationMonthUsageWidget(Context context) {
        super(context);
        init(context, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IrrigationMonthUsageWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IrrigationMonthUsageWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IrrigationMonthUsageWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void onMeasureImpl(int width, int height) {
        int diameter = width - getPaddingLeft();
        float top = height / 2 - (diameter / 2);
        float left = width / 2 - (diameter / 2);
        mCircleRect.set(left, top, left + diameter, top + diameter);
    }
    
    @Override
    protected void drawBackground(Canvas canvas) {
        mPaint.setColor(mFillColor);
        mPaint.setStyle(Style.FILL);
        canvas.drawCircle(mCircleRect.centerX(), mCircleRect.centerY(), Math.min(getWidth(), getHeight())/2, mPaint);
        
        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Style.STROKE);
        canvas.drawCircle(mCircleRect.centerX(), mCircleRect.centerY(), (Math.min(getWidth(), getHeight())-mStrokeWidth)/2, mPaint);
    }

    @Override
    protected String getValueText() {
        return mValue + " l";
    }
    

}
