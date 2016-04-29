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
 * The interface Application dao.
 * @param <T>  the type parameter
 */
public interface ApplicationDao<T> extends SqlDao<T> {

    /**
     * Find applications by  tenant id.
     *
     * @param tenantId the tenant id
     * @return the list of applications
     */
    List<T> findByTenantId(String tenantId);

    /**
     * Find applications by application token.
     *
     * @param token the token
     * @return the application object
     */
    T findByApplicationToken(String token);

    /**
     * Find applications by name and tenant id.
     *
     * @param name the name of application
     * @param tenantId the tenant id
     * @return the application object
     */
    T findByNameAndTenantId(String name, String tenantId);

    /**
     * Remove application by application token.
     *
     * @param applicationToken the application token
     */
    void removeByApplicationToken(String applicationToken);

    /**
     * Gets next sequence number.
     *
     * @param id the application id
     * @return the next sequence number
     */
    T getNextSeqNumber(String id);

}
