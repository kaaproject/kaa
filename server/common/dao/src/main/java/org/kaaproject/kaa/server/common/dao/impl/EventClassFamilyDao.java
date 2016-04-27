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
 * The interface for Event Class Family Dao.
 * @param <T>  the type parameter
 */
public interface EventClassFamilyDao<T> extends SqlDao<T> {

    /**
     * Find EventClassFamily by tenant id.
     *
     * @param id the user id
     * @return the list of users
     */
    List<T> findByTenantId(String id);

    /**
     * Find EventClassFamily by tenant id and name.
     *
     * @param tenantId id of tenant
     * @param name name of EventClassFamily
     * @return the list of users
     */
    T findByTenantIdAndName(String tenantId, String name);

    /**
     * Remove all EventClassFamily objects by tenant id.
     *
     * @param tenantId the tenant id
     */
    void removeByTenantId(String tenantId);

    /**
     * Validate event class family name for uniqueness within the tenant.
     *
     * @param tenantId the tenant id
     * @param ecfId the event class family id
     * @param name the event class family name
     * @return true if event class family name is unique otherwise false
     */
    boolean validateName(String tenantId, String ecfId, String name);

    /**
     * Validate event class family class name for uniqueness within the tenant.
     *
     * @param tenantId the tenant id
     * @param ecfId the event class family id
     * @param className the event class family class name
     * @return true if event class family class name is unique otherwise false
     */
    boolean validateClassName(String tenantId, String ecfId, String className);
}
