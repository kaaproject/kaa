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

package org.kaaproject.kaa.server.admin.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.TopicDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.NotificationSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/notificationService")
public interface NotificationService extends RemoteService {

    List<NotificationSchemaDto> getNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<NotificationSchemaDto> getNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<VersionDto> getUserNotificationSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<VersionDto> getUserNotificationSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    NotificationSchemaDto getNotificationSchema(String notificationSchemaId) throws KaaAdminServiceException;

    NotificationSchemaDto saveNotificationSchema(NotificationSchemaDto notificationSchema) throws KaaAdminServiceException;

    List<TopicDto> getTopicsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<TopicDto> getTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    List<TopicDto> getVacantTopicsByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    TopicDto getTopic(String topicId) throws KaaAdminServiceException;

    TopicDto editTopic(TopicDto topic) throws KaaAdminServiceException;

    void deleteTopic(String topicId) throws KaaAdminServiceException;

    void addTopicToEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    void removeTopicFromEndpointGroup(String endpointGroupId, String topicId) throws KaaAdminServiceException;

    NotificationDto sendNotification(NotificationDto notification, byte[] body) throws KaaAdminServiceException;

    EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, byte[] body) throws KaaAdminServiceException;

    List<TopicDto> getTopicsByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<SchemaInfoDto> getUserNotificationSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    NotificationSchemaViewDto getNotificationSchemaView(String notificationSchemaId) throws KaaAdminServiceException;

    NotificationSchemaViewDto saveNotificationSchemaView(NotificationSchemaViewDto notificationSchema) throws KaaAdminServiceException;

    NotificationSchemaViewDto createNotificationSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    void sendNotification(NotificationDto notification, RecordField notificationData) throws KaaAdminServiceException;

    EndpointNotificationDto sendUnicastNotification(NotificationDto notification, String clientKeyHash, RecordField notificationData) throws KaaAdminServiceException;

}
