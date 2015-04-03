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

import org.kaaproject.kaa.common.dto.SdkKeyDto;

/**
 * The interface for SdkKey service.
 */
public interface SdkKeyService {

    /**
     * Find sdk key by its token
     * @param token token of an SDK
     * @return the sdk key dto object
     */
    SdkKeyDto findSdkKeyByToken(String token);

    /**
     * Save sdk key. If sdk key object has id, then sdk key
     * will be updated, otherwise it will be inserted as a
     * new object.
     *
     * @param sdkKeyDto the application dto
     * @return the saved application dto object
     */
    SdkKeyDto saveSdkKey(SdkKeyDto sdkKeyDto);
}
