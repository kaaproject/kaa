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

package org.kaaproject.kaa.server.admin.services;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.admin.services.cache.CacheService;
import org.kaaproject.kaa.server.admin.services.thrift.ControlThriftClientProvider;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.dto.SdkKey;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.common.thrift.gen.control.Sdk;
import org.kaaproject.kaa.server.common.thrift.gen.control.SdkPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheServiceImpl implements CacheService {

    @Autowired
    private ControlThriftClientProvider clientProvider;


    @Override
    @Cacheable("sdkCache")
    public Sdk getSdk(SdkKey key) throws KaaAdminServiceException {
        SdkPlatform targetPlatform = toSdkPlatform(key.getTargetPlatform());
        try {
            return clientProvider.getClient().generateSdk(targetPlatform,
                    key.getApplicationId(), key.getProfileSchemaVersion(),
                    key.getConfigurationSchemaVersion(), key.getNotificationSchemaVersion());
        } catch (TException e) {
            throw Utils.handleException(e);
        }
    }

    private SdkPlatform toSdkPlatform(org.kaaproject.kaa.server.admin.shared.dto.SdkPlatform sdkPlatform) {
        switch (sdkPlatform) {
        case JAVA:
            return SdkPlatform.JAVA;
        case CPP:
            return SdkPlatform.CPP;
            default:
                return null;
        }
    }

    @Override
    @Cacheable(value = "fileUploadCache", key = "#key")
    public byte[] uploadedFile(String key, byte[] data) {
        return data;
    }

    @Override
    @CacheEvict(value = "fileUploadCache", key = "#key")
    public void removeUploadedFile(String key) {
    }

}
