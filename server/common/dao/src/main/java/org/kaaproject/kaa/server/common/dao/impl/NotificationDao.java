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

import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.server.common.dao.model.Notification;

/**
 * The Interface NotificationDao.
 *
 * @param <T> the generic type
 */
public interface NotificationDao<T extends Notification> extends Dao<T, String> {

    T save(NotificationDto notification);

    /**
     * Find notifications by topic id.
     *
     * @param topicId the topic id
     * @return the list of notifications
     */
    List<T> findNotificationsByTopicId(String topicId);

    /**
     * Removes the notifications by topic id.
     *
     * @param topicId the topic id
     */
    void removeNotificationsByTopicId(String topicId);

    /**
     * Find notifications by topic id,
     * notification schema version and start sequence number.
     *
     * @param topicId the topic id
     * @param seqNum the sequence number
     * @param sysNfVersion the system notification version
     * @param userNfVersion the user notification version
     * @return the list of notifications
     */
    List<T> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion);
}
