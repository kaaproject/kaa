package org.kaaproject.kaa.demo.iotworld.smarthome.util;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import org.kaaproject.kaa.demo.iotworld.smarthome.widget.RobotoTextView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FontUtils {
    
    public enum FontType {
        ROBOTO_NORMAL("fonts/Roboto/Roboto-Regular.ttf"),
        ROBOTO_BOLD("fonts/Roboto/Roboto-Bold.ttf"),
        ROBOTO_ITALIC("fonts/Roboto/Roboto-Italic.ttf"),
        ROBOTO_BOLD_ITALIC("fonts/Roboto/Roboto-BoldItalic.ttf"),
        ROBOTO_LIGHT("fonts/Roboto/Roboto-Light.ttf"),
        ROBOTO_MEDIUM("fonts/Roboto/Roboto-Medium.ttf"),
        ROBOTO_THIN("fonts/Roboto/Roboto-Thin.ttf"),
        RAJDHANI_BOLD("fonts/Rajdhani/Rajdhani-Bold.ttf");

        private final String path;

        FontType(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
    
    private static Map<FontType, Typeface> typefaceCache = new EnumMap<FontType, Typeface>(FontType.class);
    
    public static Typeface getTypeface(Context context, FontType fontType, boolean previewMode) {
        String fontPath = fontType.getPath();
        if (!typefaceCache.containsKey(fontType)) {
            Typeface typeface = null;
            if (previewMode) {
                typeface = Typeface.SANS_SERIF;
            } else {
                typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
            }
            typefaceCache.put(fontType, typeface);
        }
        return typefaceCache.get(fontType);
    }
    
    private static Typeface getRobotoTypeface(Context context, int style) {
        FontType robotoFontType = null;
        switch (style) {
        case Typeface.BOLD:
            robotoFontType = FontType.ROBOTO_BOLD;
            break;
        case Typeface.BOLD_ITALIC:
            robotoFontType = FontType.ROBOTO_BOLD_ITALIC;
            break;
        case Typeface.ITALIC:
            robotoFontType = FontType.ROBOTO_ITALIC;
            break;
        case Typeface.NORMAL:
            robotoFontType = FontType.ROBOTO_NORMAL;
            break;
        }
        return getTypeface(context, robotoFontType, false);
    }
    
    private static Typeface getRobotoTypeface(Context context, Typeface originalTypeface) {
        FontType robotoFontType = null;
        if (originalTypeface == null) {
            robotoFontType = FontType.ROBOTO_NORMAL;
        } else {
            int style = originalTypeface.getStyle();
            switch (style) {
                case Typeface.BOLD:
                    robotoFontType = FontType.ROBOTO_BOLD;
                    break;
                case Typeface.BOLD_ITALIC:
                    robotoFontType = FontType.ROBOTO_BOLD_ITALIC;
                    break;
                case Typeface.ITALIC:
                    robotoFontType = FontType.ROBOTO_ITALIC;
                    break;
                case Typeface.NORMAL:
                    robotoFontType = FontType.ROBOTO_NORMAL;
                    break;
            }
        }
        return (robotoFontType == null) ? originalTypeface : getTypeface(context, robotoFontType, false);
    }
    
    public static void setRobotoFont (Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            ViewGroup godfatherView = (ViewGroup) activity.getWindow().getDecorView();
            FontUtils.setRobotoFont(godfatherView);
        }
    }

    public static void setRobotoFont (View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                    setRobotoFont(((ViewGroup)view).getChildAt(i));
                }
            } else if (view instanceof TextView && !(view instanceof RobotoTextView)) {
                Typeface currentTypeface = ((TextView) view).getTypeface();
                ((TextView) view).setTypeface(getRobotoTypeface(view.getContext(), currentTypeface));
            }
        }
    }
    
    public static void setFontStyle(TextView view, int style) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            view.setTypeface(getRobotoTypeface(view.getContext(), style));
        } else {
            Typeface currentTypeface = view.getTypeface();
            view.setTypeface(Typeface.create(currentTypeface, style));
        }
    }
}
