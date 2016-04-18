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

package org.kaaproject.kaa.server.admin.shared.util;

import java.util.Map;

import com.google.gwt.http.client.URL;

public class UrlParams {

    public static final String RESET_PASSWORD = "resetPassword";
    public static final int PASSWORD_RESET_HASH_LENGTH = 128;
    public static final String PARAMS_SEPARATOR = "&";

    private UrlParams() {
    }

    public static String generateParamsUrl(Map<String, String> paramsMap) {
        String paramsUrl = "";
        for (String key : paramsMap.keySet()) {
            String val = paramsMap.get(key);
            if (paramsUrl.length() > 0) {
                paramsUrl += PARAMS_SEPARATOR;
            }
            paramsUrl += key + "=" + URL.encodeQueryString(val);
        }
        return paramsUrl;
    }
    
    public static void updateParamsFromUrl(Map<String, String> paramsMap,
            String paramsUrl) {
        if (paramsUrl != null && paramsUrl.trim().length() > 0) {
            String[] params = paramsUrl.split(PARAMS_SEPARATOR);
            for (String param : params) {
                String[] keyVal = param.split("=");
                if (keyVal != null && keyVal.length == 2) {
                    paramsMap.put(keyVal[0], URL.decodeQueryString(keyVal[1]));
                }
            }
        }
    }
    
}
