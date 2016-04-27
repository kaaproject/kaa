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

package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUser;


/**
 * The Interface EndpointUserDao.
 *
 * @param <T> the generic type
 */
public interface EndpointUserDao<T extends EndpointUser> extends Dao<T, String> {

    /**
     * Save or update endpoint user object
     *
     * @param dto the endpoint user object
     * @return save endpoint user object
     */
    T save(EndpointUserDto dto);

    /**
     * Find user by external id and tenant id.
     *
     * @param externalId the external user id
     * @param tenantId   the tenant id
     * @return the user object
     */
    T findByExternalIdAndTenantId(String externalId, String tenantId);

    /**
     * Remove user by external id and tenant id.
     *
     * @param externalId the external user id
     * @param tenantId   the tenant id
     */
    void removeByExternalIdAndTenantId(String externalId, String tenantId);

    /**
     * Generate access token.
     *
     * @param externalUid the external uid
     * @param tenantId    the tenant id
     * @return the generated access token
     */
    String generateAccessToken(String externalUid, String tenantId);


    /**
     * Check access token.
     *
     * @param externalUid the external uid
     * @param tenantId    the tenant id
     * @param accessToken the access token
     * @return true, if successful
     */
    boolean checkAccessToken(String externalUid, String tenantId, String accessToken);
    
}
