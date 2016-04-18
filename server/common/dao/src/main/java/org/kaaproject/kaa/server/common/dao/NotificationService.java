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

package org.kaaproject.kaa.server.common.dao;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.common.dto.VersionDto;

/**
 * The Interface NotificationService.
 */
public interface NotificationService {

    /**
     * Save notification schema.
     *
     * @param notificationSchemaDto the notification schema dto
     * @return the notification schema dto
     */
    NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchemaDto);

    /**
     * Save notification.
     *
     * @param notificationDto the notification dto
     * @return the update notification dto
     */
    UpdateNotificationDto<NotificationDto> saveNotification(NotificationDto notificationDto);

    /**
     * Find notification by id.
     *
     * @param id the id
     * @return the notification dto
     */
    NotificationDto findNotificationById(String id);

    /**
     * Find unicast notification by id.
     *
     * @param id the id
     * @return the endpoint notification dto
     */
    EndpointNotificationDto findUnicastNotificationById(String id);

    /**
     * Save unicast notification.
     *
     * @param dto the dto
     * @return the update notification dto
     */
    UpdateNotificationDto<EndpointNotificationDto> saveUnicastNotification(EndpointNotificationDto dto);

    /**
     * Find notifications by topic id.
     *
     * @param topicId the topic id
     * @return the list
     */
    List<NotificationDto> findNotificationsByTopicId(String topicId);

    /**
     * Find notification schema by id.
     *
     * @param id the id
     * @return the notification schema dto
     */
    NotificationSchemaDto findNotificationSchemaById(String id);

    /**
     * Find notification schemas by app id.
     *
     * @param appId the app id
     * @return the list
     */
    List<NotificationSchemaDto> findNotificationSchemasByAppId(String appId);

    /**
     * Find user notification schemas by app id.
     *
     * @param applicationId the application id
     * @return the list
     */
    List<VersionDto> findUserNotificationSchemasByAppId(String applicationId);

    /**
     * Find notification schema versions by app id.
     *
     * @param applicationId the application id
     * @return the list
     */
    List<VersionDto> findNotificationSchemaVersionsByAppId(String applicationId);

    /**
     * Removes the notification schemas by app id.
     *
     * @param appId the app id
     */
    void removeNotificationSchemasByAppId(String appId);


    /**
     * Find notifications by topic id, start sequence number and
     * either system schema version or user schema version.
     *
     * @param topicId the topic id
     * @param seqNum the sequence number
     * @param sysNfVersion the system schema version
     * @param userNfVersion the user schema version
     * @return the list
     */
    List<NotificationDto> findNotificationsByTopicIdAndVersionAndStartSecNum(String topicId, int seqNum, int sysNfVersion, int userNfVersion);

    /**
     * Find notification schemas by app id and type.
     *
     * @param appId the app id
     * @param type the type
     * @return the list
     */
    List<NotificationSchemaDto> findNotificationSchemasByAppIdAndType(String appId, NotificationTypeDto type);

    /**
     * Find notification schema by app id and type and version.
     *
     * @param appId the app id
     * @param type the type
     * @param majorVersion the major version
     * @return the notification schema dto
     */
    NotificationSchemaDto findNotificationSchemaByAppIdAndTypeAndVersion(String appId, NotificationTypeDto type, int majorVersion);

    /**
     * Find unicast notifications by key hash.
     *
     * @param keyHash the key hash
     * @return the list
     */
    List<EndpointNotificationDto> findUnicastNotificationsByKeyHash(byte[] keyHash);

    /**
     * Removes the unicast notifications by key hash.
     *
     * @param keyHash the key hash
     */
    void removeUnicastNotificationsByKeyHash(byte[] keyHash);

    /**
     * Removes the unicast notifications by id.
     *
     * @param id the String notification id
     */
    void removeUnicastNotificationById(String id);

}
