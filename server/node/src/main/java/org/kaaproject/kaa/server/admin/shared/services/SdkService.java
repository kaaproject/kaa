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

package org.kaaproject.kaa.server.admin.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.admin.SdkProfileViewDto;
import org.kaaproject.kaa.common.dto.file.FileData;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/sdkService")
public interface SdkService extends RemoteService {

    SdkProfileDto createSdkProfile(SdkProfileDto sdkProfile) throws KaaAdminServiceException;

    void deleteSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    SdkProfileDto getSdkProfile(String sdkProfileId) throws KaaAdminServiceException;

    List<SdkProfileDto> getSdkProfilesByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<SdkProfileDto> getSdkProfilesByApplicationId(String applicationId) throws KaaAdminServiceException;

    FileData getSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    void flushSdkCache() throws KaaAdminServiceException;

    String generateSdk(SdkProfileDto sdkProfile, SdkPlatform targetPlatform) throws KaaAdminServiceException;

    SdkProfileViewDto getSdkProfileView(String sdkProfileId) throws KaaAdminServiceException;

    SchemaVersions getSchemaVersionsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    SchemaVersions getSchemaVersionsByApplicationId(String applicationId) throws KaaAdminServiceException;

}
