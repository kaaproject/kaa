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
    
    public final static String KAA_SDK_SERVLET_PATH = "servlet/kaaSdkServlet";
    public final static String KAA_RECORD_LIBRARY_SERVLET_PATH = "servlet/kaaRecordLibraryServlet";
    public final static String KAA_CTL_EXPORT_SERVLET_PATH = "servlet/kaaCtlExportServlet";
    public final static String KAA_PROFILE_DOWNLOAD_SERVLET_PATH = "servlet/kaaProfileDownloadServlet";
    
    public static final String SDK_KEY_PARAMETER = "sdkKey";
    public static final String RECORD_KEY_PARAMETER = "recordKey";
    public static final String CTL_EXPORT_KEY_PARAMETER = "ctlExportKey";
    public static final String ENDPOINT_KEY_PARAMETER = "endpointKey";
    public static final String PROFILE_TYPE_PARAMETER = "profileType";

    public static final String CONFIGURATION_SCHEMA_ID = "schemaId";
    public static final String ENDPOINT_GROUP_ID = "endGroupId";
    public static enum ProfileType {        
        CLIENT,
        SERVER        
    }

}
