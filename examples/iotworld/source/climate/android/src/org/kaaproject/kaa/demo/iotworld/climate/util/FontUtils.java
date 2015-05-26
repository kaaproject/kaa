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
package org.kaaproject.kaa.demo.iotworld.climate.util;

import java.util.EnumMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {
    
    public enum FontType {
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
    
}
