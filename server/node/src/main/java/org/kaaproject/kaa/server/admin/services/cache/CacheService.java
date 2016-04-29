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

package org.kaaproject.kaa.server.admin.services.cache;

import java.io.Serializable;
import java.util.List;

import org.kaaproject.kaa.common.dto.admin.RecordKey;
import org.kaaproject.kaa.common.dto.admin.SdkPlatform;
import org.kaaproject.kaa.common.dto.admin.SdkProfileDto;
import org.kaaproject.kaa.common.dto.file.FileData;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaExportKey;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;

public interface CacheService {

    /**
     * @author Bohdan Khablenko
     *
     * @since v0.8.0
     */
    public class SdkKey implements Serializable {

        private static final long serialVersionUID = 8594934041188059060L;

        private final SdkProfileDto sdkProfile;
        private final SdkPlatform targetPlatform;

        public SdkKey(SdkProfileDto sdkProfileDto, SdkPlatform targetPlatform) {
            this.sdkProfile = sdkProfileDto;
            this.targetPlatform = targetPlatform;
        }

        public SdkProfileDto getSdkProfile() {
            return sdkProfile;
        }

        public SdkPlatform getTargetPlatform() {
            return targetPlatform;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((sdkProfile == null) ? 0 : sdkProfile.hashCode());
            result = prime * result + ((targetPlatform == null) ? 0 : targetPlatform.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SdkKey)) {
                return false;
            }
            SdkKey other = (SdkKey) obj;
            if (sdkProfile == null) {
                if (other.sdkProfile != null) {
                    return false;
                }
            } else if (!sdkProfile.equals(other.sdkProfile)) {
                return false;
            }
            if (targetPlatform != other.targetPlatform) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("SdkKey [sdkProfileDto=");
            builder.append(sdkProfile);
            builder.append(", targetPlatform=");
            builder.append(targetPlatform);
            builder.append("]");
            return builder.toString();
        }
    }

    FileData getSdk(SdkKey key);

    FileData putSdk(SdkKey key, FileData sdkFile);

    void flushSdk(SdkKey key) throws KaaAdminServiceException;

    List<SdkKey> getCachedSdkKeys(String applicationId);

    byte[] uploadedFile(String key, byte[] data);

    void removeUploadedFile(String key);

    FileData getRecordLibrary(RecordKey key) throws KaaAdminServiceException;

    FileData getRecordSchema(RecordKey key) throws KaaAdminServiceException;

    FileData getRecordData(RecordKey key) throws KaaAdminServiceException;
    
    FileData getExportedCtlSchema(CtlSchemaExportKey key) throws KaaAdminServiceException;

}
