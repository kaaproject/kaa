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

package org.kaaproject.kaa.server.admin.shared.servlet;

public interface ServletParams {

  String EP_CONF_SERVLET_PATH = "servlet/kaaEpConfServlet";
  String KAA_SDK_SERVLET_PATH = "servlet/kaaSdkServlet";
  String KAA_RECORD_LIBRARY_SERVLET_PATH = "servlet/kaaRecordLibraryServlet";
  String KAA_CTL_EXPORT_SERVLET_PATH = "servlet/kaaCtlExportServlet";
  String KAA_PROFILE_DOWNLOAD_SERVLET_PATH = "servlet/kaaProfileDownloadServlet";
  String KAA_USER_CONFIGURATION_SERVLET_PATH = "servlet/kaaUserConfServlet";

  String SDK_KEY_PARAMETER = "sdkKey";
  String RECORD_KEY_PARAMETER = "recordKey";
  String CTL_EXPORT_KEY_PARAMETER = "ctlExportKey";
  String ENDPOINT_KEY_PARAMETER = "endpointKey";
  String PROFILE_TYPE_PARAMETER = "profileType";
  String APPLICATION_ID_PARAMETER = "appId";
  String USER_EXTERNAL_ID_PARAMETER = "externalUId";


  String CONFIGURATION_SCHEMA_ID = "schemaId";
  String ENDPOINT_GROUP_ID = "endGroupId";

  enum ProfileType {
    CLIENT,
    SERVER
  }

}
