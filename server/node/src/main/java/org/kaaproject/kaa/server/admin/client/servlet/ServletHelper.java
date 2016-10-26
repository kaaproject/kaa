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

package org.kaaproject.kaa.server.admin.client.servlet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;

import org.kaaproject.kaa.server.admin.shared.servlet.ServletParams;

import java.util.List;
import java.util.Map.Entry;

public class ServletHelper implements ServletParams {

  /**
   * Download user configuration.
   *
   * @param externalUId   the external user identifier
   * @param schemaVersion the configuration schema version
   * @param appId         the application identifier
   */
  public static void downloadUserConfiguration(String externalUId, String schemaVersion,
                                               String appId) {
    String getUrl = composeUrl(KAA_USER_CONFIGURATION_SERVLET_PATH,
        APPLICATION_ID_PARAMETER + "=" + URL.encodeQueryString(appId),
        USER_EXTERNAL_ID_PARAMETER + "=" + URL.encodeQueryString(externalUId),
        CONFIGURATION_SCHEMA_ID + "=" + URL.encodeQueryString(schemaVersion));
    String url = GWT.getModuleBaseURL() + getUrl;
    Window.open(url, "_self", "enabled");
  }

  /**
   * Download the SDK.
   *
   * @param key the SDK key
   */
  public static void downloadSdk(String key) {
    String getUrl = composeUrl(KAA_SDK_SERVLET_PATH,
        SDK_KEY_PARAMETER + "=" + URL.encodeQueryString(key));
    String url = GWT.getModuleBaseURL() + getUrl;
    Window.open(url, "_self", "enabled");
  }

  /**
   * Download record library.
   *
   * @param key the record library key
   */
  public static void downloadRecordLibrary(String key) {
    String getUrl = composeUrl(KAA_RECORD_LIBRARY_SERVLET_PATH, RECORD_KEY_PARAMETER
        + "=" + URL.encodeQueryString(key));
    String url = GWT.getModuleBaseURL() + getUrl;
    Window.open(url, "_self", "enabled");
  }

  /**
   * Export CTL schema.
   *
   * @param key the CTL export key
   */
  public static void exportCtlSchema(String key) {
    String getUrl = composeUrl(KAA_CTL_EXPORT_SERVLET_PATH, CTL_EXPORT_KEY_PARAMETER
        + "=" + URL.encodeQueryString(key));
    String url = GWT.getModuleBaseURL() + getUrl;
    Window.open(url, "_self", "enabled");
  }

  /**
   * Download endpoint profile.
   *
   * @param endpointKey the endpoint key
   * @param type        the type of an endpoint
   */
  public static void downloadEndpointProfile(String endpointKey, ProfileType type) {
    String getUrl = composeUrl(KAA_PROFILE_DOWNLOAD_SERVLET_PATH,
        ENDPOINT_KEY_PARAMETER + "=" + URL.encodeQueryString(endpointKey),
        PROFILE_TYPE_PARAMETER + "=" + URL.encodeQueryString(type.name()));
    String url = GWT.getModuleBaseURL() + getUrl;
    Window.open(url, "_self", "enabled");
  }

  /**
   * Download endpoint configuration.
   *
   * @param endpointKeyHash the endpoint key hash
   */
  public static void downloadEndpointConfiguration(String endpointKeyHash) {
    String getUrl = composeUrl(EP_CONF_SERVLET_PATH,
        ENDPOINT_KEY_PARAMETER + "=" + URL.encodeQueryString(endpointKeyHash));
    String url = GWT.getModuleBaseURL() + getUrl;
    Window.open(url, "_self", "enabled");
  }


  /**
   * Some browsers may not use given filename.
   */
  public static void downloadJsonFile(String json, String filename) {
    Window.open("data:application/octet-stream;"
        + "headers=Content-Disposition: attachment; filename=\""
        + filename + "\"," + json, "_self", "enabled");
  }

  private static String composeUrl(String servletPath, String... params) {
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
    return UriUtils.encode(ret);
  }

}
