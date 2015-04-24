package org.kaaproject.kaa.demo.iotworld.smarthome.util;

import android.graphics.Color;

public class ColorUtils {

    public static int darkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
    
}
