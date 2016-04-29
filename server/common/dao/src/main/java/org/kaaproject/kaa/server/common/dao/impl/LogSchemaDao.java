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
 * The interface Log Schema dao.
 * @param <T>  the type parameter
 */
public interface LogSchemaDao<T> extends SqlDao<T> {

    /**
     * Find all Log Schemas for Application with specific id
     *
     * @param applicationId the id of Application
     * @return List of Log Schemas
     */
    List<T> findByApplicationId(String applicationId);

    /**
     * Find Log Schema by application id and version.
     *
     * @param applicationId the application id
     * @param version the version of profile schema
     * @return the Log Schema
     */
    T findByApplicationIdAndVersion(String applicationId, int version);

    /**
     * Remove by application id.
     *
     * @param applicationId the application id
     */
    void removeByApplicationId(String applicationId);

    /**
     * Find latest log schema by application id.
     *
     * @param applicationId the application id
     * @return the notification schema
     */
    T findLatestLogSchemaByAppId(String applicationId);

}
