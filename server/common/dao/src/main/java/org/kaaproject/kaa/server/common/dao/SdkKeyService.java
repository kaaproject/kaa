/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao;

import org.kaaproject.kaa.common.dto.admin.SdkPropertiesDto;

/**
 * The interface for SdkKey service.
 */
public interface SdkKeyService {

    /**
     * Find sdk properties by theirs token
     * @param token token of an SDK
     * @return the sdk properties dto object
     */
    SdkPropertiesDto findSdkKeyByToken(String token);

    /**
     * Save sdk properties. If sdk properties object has id, then sdk key
     * will be updated, otherwise if there is no other sdk key object
     * with the same token, it will be inserted as a new sdk key object,
     * if there is, nothing will be saved, existent object will be returned
     *
     * @param sdkPropertiesDto the sdk properties dto
     * @return the saved application dto object
     */
    SdkPropertiesDto saveSdkKey(SdkPropertiesDto sdkPropertiesDto);
}
