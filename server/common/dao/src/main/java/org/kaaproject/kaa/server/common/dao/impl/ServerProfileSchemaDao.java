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

public interface ServerProfileSchemaDao<T> extends SqlDao<T> {

    /**
     * Find latest server profile schema for application with given identifier.
     *
     * @param appId the application identifier
     * @return the latest server profile schema.
     */
    T findLatestByAppId(String appId);

    /**
     * Find server profile schemas with given application identifier.
     *
     * @param appId the application identifier
     * @return the list of server profile schemas for corresponding application.
     */
    List<T> findByAppId(String appId);
    
    /**
     * Find server profile schema by application id and version.
     *
     * @param applicationId the application id
     * @param version the version of server profile schema
     * @return the server profile schema
     */
    T findByAppIdAndVersion(String applicationId, int version);

    /**
     * Remove server profile schemas for application with the given identifier.
     *
     * @param appId the application identifier
     */
    void removeByAppId(String appId);
}
