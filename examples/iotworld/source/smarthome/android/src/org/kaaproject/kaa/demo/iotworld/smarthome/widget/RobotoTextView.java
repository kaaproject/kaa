package org.kaaproject.kaa.demo.iotworld.smarthome.widget;

import org.kaaproject.kaa.demo.iotworld.smarthome.R;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils;
import org.kaaproject.kaa.demo.iotworld.smarthome.util.FontUtils.FontType;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.TextView;

public class RobotoTextView extends TextView {

    private static final FontType DEFAULT_ROBOTO_FONT = FontType.ROBOTO_NORMAL;
    
    private FontType mRobotoFontType = DEFAULT_ROBOTO_FONT;
    
    public RobotoTextView(Context context) {
        super(context);
        init(context, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RobotoTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public RobotoTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public RobotoTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    private void init(final Context context, final AttributeSet attrs) {
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
        mRobotoFontType = FontType.values()[typedArray.getInt(R.styleable.RobotoTextView_robotoFontFamily, DEFAULT_ROBOTO_FONT.ordinal())];
        Typeface robotoTypeface = FontUtils.getTypeface(context, mRobotoFontType, isInEditMode());
        setTypeface(robotoTypeface);
        typedArray.recycle();
    }

}
