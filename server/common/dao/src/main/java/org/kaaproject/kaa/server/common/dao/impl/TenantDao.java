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

/**
 * The interface Tenant dao.
 * @param <T>  the type parameter
 */
public interface TenantDao<T> extends SqlDao<T> {

    /**
     * Find tenant by name.
     *
     * @param tenantName the tenant name
     * @return the tenant
     */
    T findByName(String tenantName);


    /**
     * Removes tenant by name.
     *
     * @param tenantName the tenant name
     */
    void removeByName(String tenantName);
}
