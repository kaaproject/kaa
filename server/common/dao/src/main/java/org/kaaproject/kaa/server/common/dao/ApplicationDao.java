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

package org.kaaproject.kaa.server.common.dao;


import java.util.List;

/**
 * The interface Application dao.
 * @param <T>  the type parameter
 */
public interface ApplicationDao<T> extends Dao<T> {

    /**
     * Find applications by  tenant id.
     *
     * @param tenantId the tenant id
     * @return the list of applications
     */
    List<T> findByTenantId(String tenantId);

    /**
     * Remove applications by tenant id.
     *
     * @param tenantId the tenant id
     */
    void removeByTenantId(String tenantId);

    /**
     * Find applications by application token.
     *
     * @param token the token
     * @return the application object
     */
    T findByApplicationToken(String token);

    /**
     * Remove application by application token.
     *
     * @param applicationId the application id
     */
    void removeByApplicationToken(String applicationId);

    /**
     * Update sequence number.
     *
     * @param id the application id
     * @return the application
     */
    T updateSeqNumber(String id);

    /**
     * Gets next sequence number.
     *
     * @param id the application id
     * @return the next sequence number
     */
    T getNextSeqNumber(String id);

    /**
     * This method force getting next sequence number.
     *
     * @param id the id
     * @return the application object
     */
    T forceNextSeqNumber(String id);
}
