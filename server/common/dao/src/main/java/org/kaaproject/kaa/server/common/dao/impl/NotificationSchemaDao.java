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

import org.kaaproject.kaa.common.dto.NotificationTypeDto;

import java.util.List;

/**
 * The Interface NotificationSchemaDao.
 *
 * @param <T> the generic type
 */
public interface NotificationSchemaDao<T> extends SqlDao<T> {
    
    /**
     * Find notification schemas by application id.
     *
     * @param appId the application id
     * @return the list of notification schemas
     */
    List<T> findNotificationSchemasByAppId(String appId);

    /**
     * Removes the notification schemas by application id.
     *
     * @param appId the application id
     */
    void removeNotificationSchemasByAppId(String appId);

    /**
     * Find notification schemas by application id and type.
     *
     * @param appId the application id
     * @param type the type
     * @return the list of notification schemas
     */
    List<T> findNotificationSchemasByAppIdAndType(String appId, NotificationTypeDto type);

    /**
     * Find notification schemas by application id and type and version.
     *
     * @param appId the application id
     * @param type the type
     * @param majorVersion the major version
     * @return the notification schema
     */
    T findNotificationSchemasByAppIdAndTypeAndVersion(String appId, NotificationTypeDto type, int majorVersion);

    /**
     * Find latest notification schema by application id.
     *
     * @param applicationId the application id
     * @param type the type
     * @return the notification schema
     */
    T findLatestNotificationSchemaByAppId(String applicationId, NotificationTypeDto type);
}
