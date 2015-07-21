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

package org.kaaproject.kaa.server.admin.services.cache;

import java.util.List;

import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;

public interface CacheService {

    FileData getSdk(SdkPropertiesDto key);

    FileData putSdk(SdkPropertiesDto key, FileData sdkFile);

    void flushSdk(SdkPropertiesDto key) throws KaaAdminServiceException;
    
    List<SdkPropertiesDto> getCachedSdkKeys(String applicationId);

    byte[] uploadedFile(String key, byte[] data);

    void removeUploadedFile(String key);

    FileData getRecordLibrary(RecordKey key) throws KaaAdminServiceException;

    FileData getRecordSchema(RecordKey key) throws KaaAdminServiceException;

    FileData getRecordData(RecordKey key) throws KaaAdminServiceException;

}
