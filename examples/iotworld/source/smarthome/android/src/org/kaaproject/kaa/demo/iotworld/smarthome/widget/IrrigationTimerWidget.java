package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.TimeUtils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;

public class IrrigationTimerWidget extends AbstractIrrigationWidget {
    
    private final static int START_ANGLE = -90;
    
    private final static int DEFAULT_MAX_TIME_SEC = 60;
    
    private int mMaxTimeSec;

    private RectF mCircleRect = new RectF();
    
    public IrrigationTimerWidget(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IrrigationTimerWidget(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IrrigationTimerWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IrrigationTimerWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void init(final Context context, final AttributeSet attrs) {
        super.init(context, attrs);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IrrigationTimerWidget);
        mMaxTimeSec = typedArray.getInteger(R.styleable.IrrigationTimerWidget_maxTimeSec, DEFAULT_MAX_TIME_SEC);
        typedArray.recycle();
    }
    
    public void setMaxTimeSec(int maxTimeSec) {
        mMaxTimeSec = maxTimeSec;
        invalidate();
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
        mPaint.setColor(mStrokeColor);
        canvas.drawCircle(mCircleRect.centerX(), mCircleRect.centerY(), Math.min(getWidth(), getHeight())/2, mPaint);
        mPaint.setColor(mFillColor);
        int angleSweep = (int) (((float)(mMaxTimeSec-mValue))/((float)mMaxTimeSec)*360f);
        canvas.drawArc(mCircleRect, START_ANGLE, angleSweep, true, mPaint);
    }
    
    @Override
    protected String getValueText() {
        return TimeUtils.secondsToTimer((int)mValue, false);
    }

}
