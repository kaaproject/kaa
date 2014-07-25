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

package org.kaaproject.kaa.sandbox.web.client.servlet;

import java.util.List;
import java.util.Map.Entry;

import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataKey;
import org.kaaproject.kaa.sandbox.web.shared.dto.ProjectDataType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class ServletHelper {

    public final static String SANDBOX_PROJECT_FILE_SERVLET_PATH = "servlet/sandboxProjectFileServlet";

    public static void downloadProjectFile(String projectId, ProjectDataType type) {
        String getUrl = composeURL(SANDBOX_PROJECT_FILE_SERVLET_PATH,
        ProjectDataKey.PROJECT_ID_PARAMETER+"="+projectId,
        ProjectDataKey.PROJECT_DATA_TYPE_PARAMETER+"="+type.name());
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
