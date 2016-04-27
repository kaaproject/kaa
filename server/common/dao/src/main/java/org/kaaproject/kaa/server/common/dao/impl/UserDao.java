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

import java.util.List;

/**
 * The interface User dao.
 * @param <T>  the type parameter
 */
public interface UserDao<T> extends SqlDao<T> {

    /**
     * Find user by external uid.
     *
     * @param externalUid the external user id
     * @return the user object
     */
    T findByExternalUid(String externalUid);

    /**
     * Find user by tenant id and authority.
     *
     * @param id the user id
     * @param authority the user authority
     * @return the list of users
     */
    List<T> findByTenantIdAndAuthority(String id, String authority);

    /**
     * Find user by tenant id and authorities.
     *
     * @param id the user id
     * @param authorities the user authorities
     * @return the list of users
     */
    List<T> findByTenantIdAndAuthorities(String id, String... authorities);

    /**
     * Remove user by tenant id.
     *
     * @param tenantId the tenant id
     */
    void removeByTenantId(String tenantId);
}
