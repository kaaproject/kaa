/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.place;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.URL;

public class PlaceParams {

    public static final String PARAMS_SEPARATOR = "&";

    private static Map<String,String> paramsMap = new HashMap<String,String>();

    public static String generateToken() {
        StringBuilder paramsUrl = new StringBuilder();
        for (String key : paramsMap.keySet()) {
            String val = paramsMap.get(key);
            if (paramsUrl.length() > 0) {
                paramsUrl.append(PARAMS_SEPARATOR);
            }
            paramsUrl.append(key).append("=").append(URL.encodeQueryString(val));
        }
        return paramsUrl.toString();
    }

    public static void paramsFromToken(String token) {
        paramsMap.clear();
        if (token != null && token.trim().length() > 0) {
            String[] params = token.split(PARAMS_SEPARATOR);
            for (String param : params) {
                String[] keyVal = param.split("=");
                if (keyVal != null && keyVal.length == 2) {
                    paramsMap.put(keyVal[0], URL.decodeQueryString(keyVal[1]));
                }
            }
        }
    }

    public static void clear() {
        paramsMap.clear();
    }

    public static String getParam(String key) {
        String val = paramsMap.get(key);
        return isEmptyVal(val) ? null : val;
    }

    public static boolean getBooleanParam(String key) {
        String val = paramsMap.get(key);
        return isEmptyVal(val) ? false : Boolean.valueOf(val);
    }

    public static int getIntParam(String key) {
        String val = paramsMap.get(key);
        try {
            return isEmptyVal(val) ? 0 : Integer.valueOf(val);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }
    
    public static boolean hasParam(String key) {
        return paramsMap.containsKey(key);
    }
    
    public static double getDoubleParam(String key) {
        String val = paramsMap.get(key);
        try {
            return isEmptyVal(val) ? 0 : Double.valueOf(val);
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static boolean isEmptyVal(String val) {
        return val == null || val.length()==0 || val.equals("null");
    }

    public static void putParam(String key, String val) {
        paramsMap.put(key, val);
    }

    public static void putBooleanParam(String key, boolean val) {
        paramsMap.put(key, Boolean.toString(val));
    }

    public static void putDoubleParam(String key, double val) {
        paramsMap.put(key, Double.toString(val));
    }

    public static void putIntParam(String key, int val) {
        paramsMap.put(key, Integer.toString(val));
    }

}
