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

import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointNotification;

/**
 * The Interface EndpointNotificationDao.
 *
 * @param <T> the generic type
 */
public interface EndpointNotificationDao<T extends EndpointNotification> extends Dao<T, String> {

    /**
     * Save notification object
     *
     * @param dto the notification object
     * @return saved notification object
     */
    T save(EndpointNotificationDto dto);

    /**
     * Find notifications by key hash.
     *
     * @param keyHash the endpoint key hash
     * @return the list of endpoint notifications
     */
    List<T> findNotificationsByKeyHash(byte[] keyHash);

    /**
     * Removes the notifications by key hash.
     *
     * @param keyHash the endpoint key hash
     */
    void removeNotificationsByKeyHash(byte[] keyHash);

    /**
     * Removes the endpoint notifications by application id.
     *
     * @param appId the application id
     */
    void removeNotificationsByAppId(String appId);
}
