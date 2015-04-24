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
