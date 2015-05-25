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
package org.kaaproject.kaa.demo.iotworld.smarthome.layout;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareLayout extends LinearLayout {
    
    public static final int SIZING_BY_WIDTH = 0;
    public static final int SIZING_BY_HEIGHT = 1;
    
    private static final int DEFAULT_SIZING_TYPE = SIZING_BY_WIDTH;

    private final double mScale = 1.0;
    
    private int mSizingType = DEFAULT_SIZING_TYPE;

    public SquareLayout(Context context) {
        super(context);
        init(context, null);
    }

    public SquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquareLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SquareLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SquareLayout);
        mSizingType = typedArray.getInt(R.styleable.SquareLayout_sizingType, DEFAULT_SIZING_TYPE);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        
        if (mSizingType == SIZING_BY_WIDTH) {
            height = (int)((width / mScale) + 0.5);
        } else {
            width = (int)((mScale * height) + 0.5);
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        );
    }
}