/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.servlet;

import java.util.List;
import java.util.Map.Entry;

import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SdkKey;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class ServletHelper {

    public final static String KAA_SDK_SERVLET_PATH = "servlet/kaaSdkServlet";
    public final static String KAA_RECORD_LIBRARY_SERVLET_PATH = "servlet/kaaRecordLibraryServlet";

    public static void downloadSdk(String key) {
        String getUrl = composeURL(KAA_SDK_SERVLET_PATH,
        SdkKey.SDK_KEY_PARAMETER+"="+key);
        String url = GWT.getModuleBaseURL() + getUrl;
        Window.open( url, "_self", "enabled");
    }

    public static void downloadRecordLibrary(String key) {
        String getUrl = composeURL(KAA_RECORD_LIBRARY_SERVLET_PATH, RecordKey.RECORD_KEY_PARAMETER + "=" + key);
        String url = GWT.getModuleBaseURL() + getUrl;
        Window.open( url, "_self", "enabled");
    }

    private static String composeURL(String servletPath, String... params) {
        String ret = servletPath;
        ret = ret.replaceAll("[\\?&]+$", "");
        String sep = ret.contains("?") ? "&" : "?";
        for (String par : params) {
          ret += sep + par;
          sep = "&";
        }
        for (Entry<String, List<String>> e : Window.Location.getParameterMap().entrySet()) {
          ret += sep + e.getKey() + "=" + e.getValue().get(0);
        }
        ret += sep + "random=" + Math.random();
        return ret;
    }

}
