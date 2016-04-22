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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.kaaproject.kaa.common.dto.CTLDataDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointRegistrationDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.TopicListEntryDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointConfiguration;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointNotification;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointRegistration;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraNotification;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraTopicListEntry;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraCredentials;
import org.kaaproject.kaa.server.common.dao.impl.CredentialsDao;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public abstract class AbstractCassandraTest {

    private static final String TEST_ENDPOINT_GROUP_ID = "124";

    @Autowired
    protected EndpointNotificationDao<CassandraEndpointNotification> unicastNotificationDao;
    @Autowired
    protected EndpointConfigurationDao<CassandraEndpointConfiguration> endpointConfigurationDao;
    @Autowired
    protected EndpointProfileDao<CassandraEndpointProfile> endpointProfileDao;
    @Autowired
    protected EndpointRegistrationDao<CassandraEndpointRegistration> endpointRegistrationDao;;
    @Autowired
    protected EndpointUserCassandraDao endpointUserDao;
    @Autowired
    protected NotificationDao<CassandraNotification> notificationDao;
    @Autowired
    protected TopicListEntryDao<CassandraTopicListEntry> topicListEntryDao;
    @Autowired
    protected CredentialsDao<CassandraCredentials> credentialsDao;

    protected List<CassandraEndpointNotification> generateEndpointNotification(ByteBuffer endpointKeyHash, int count) {
        List<CassandraEndpointNotification> savedNotifications = new ArrayList<>();
        String appId = generateStringId();
        if (endpointKeyHash == null) {
            endpointKeyHash = ByteBuffer.wrap(generateEndpointProfile(appId, null, null, null).getEndpointKeyHash());
        }
        String schemaId = generateStringId();
        for (int i = 0; i < count; i++) {
            CassandraEndpointNotification endpointNotification = new CassandraEndpointNotification();
            endpointNotification.setEndpointKeyHash(endpointKeyHash);
            endpointNotification.setApplicationId(appId);
            endpointNotification.setSchemaId(schemaId);
            endpointNotification.setType(NotificationTypeDto.USER);
            endpointNotification.setSeqNum(100 + i);
            endpointNotification.setLastModifyTime(new Date(System.currentTimeMillis()));
            savedNotifications.add(unicastNotificationDao.save(endpointNotification));
        }
        return savedNotifications;
    }

    protected List<NotificationDto> generateNotifications(String topicId, String appId, String schemaId, int count, NotificationTypeDto type) {
        List<NotificationDto> notifications = new ArrayList<>(count);
        NotificationDto notification;
        for (int i = 0; i < count; i++) {
            notification = new NotificationDto();
            notification.setApplicationId(appId != null ? appId : UUID.randomUUID().toString());
            notification.setSchemaId(schemaId != null ? schemaId : UUID.randomUUID().toString());
            notification.setTopicId(topicId != null ? topicId : UUID.randomUUID().toString());
            notification.setType(type != null ? type : NotificationTypeDto.USER);
            notification.setSecNum(i);
            notification.setBody(UUID.randomUUID().toString().getBytes());
            notification.setLastTimeModify(new Date(System.currentTimeMillis()));
            notification.setNfVersion(1);
            notification.setExpiredAt(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000));
            notifications.add(notificationDao.save(notification).toDto());
        }
        return notifications;
    }

    protected List<CassandraEndpointConfiguration> generateConfiguration(int count) {
        List<CassandraEndpointConfiguration> configurations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CassandraEndpointConfiguration configuration = new CassandraEndpointConfiguration();
            configuration.setConfiguration(ByteBuffer.wrap(generateBytes()));
            configuration.setConfigurationHash(ByteBuffer.wrap(generateBytes()));
            configurations.add(endpointConfigurationDao.save(configuration));
        }
        return configurations;
    }

    protected EndpointProfileDto generateEndpointProfile(CTLDataDto dataDto) {
        return generateEndpointProfile(null, null, null, null, dataDto);
    }

    protected EndpointProfileDto generateEndpointProfile(String appId, String sdkToken, String accessToken, List<String> topicIds) {
        return generateEndpointProfile(appId, sdkToken, accessToken, topicIds, null);
    }

    protected EndpointProfileDto generateEndpointProfile(String appId, String sdkToken, String accessToken, List<String> topicIds, CTLDataDto ctlDataDto) {
        byte[] keyHash = generateBytes();

        if (appId == null) {
            appId = generateStringId();
        }

        if (sdkToken == null) {
            sdkToken = generateStringId();
        }

        if (accessToken == null) {
            accessToken = generateStringId();
        }

        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setSdkToken(sdkToken);
        profileDto.setSubscriptions(topicIds);
        profileDto.setEndpointKeyHash(keyHash);
        profileDto.setAccessToken(accessToken);
        if (ctlDataDto != null) {
            profileDto.setServerProfileBody(ctlDataDto.getBody());
            profileDto.setServerProfileVersion(ctlDataDto.getServerProfileVersion());
        }
        return endpointProfileDao.save(new CassandraEndpointProfile(profileDto)).toDto();
    }

    protected EndpointProfileDto generateEndpointProfileForTestUpdate(String id, byte[] keyHash, List<EndpointGroupStateDto> cfGroupState) {
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setId(id);
        profileDto.setEndpointKeyHash(keyHash);
        profileDto.setApplicationId(generateStringId());
        profileDto.setAccessToken(generateStringId());
        profileDto.setGroupState(cfGroupState);
        profileDto.setSdkToken(UUID.randomUUID().toString());
        return profileDto;
    }

    protected EndpointProfileDto generateEndpointProfileWithEndpointGroupId(String appId) {
        byte[] keyHash = generateBytes();
        if (appId == null) {
            appId = generateStringId();
        }
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setEndpointKeyHash(keyHash);
        profileDto.setAccessToken(generateStringId());
        profileDto.setClientProfileBody("test profile");
        profileDto.setServerProfileBody("test server-side profile");
        List<EndpointGroupStateDto> groupState = new ArrayList<>();
        groupState.add(new EndpointGroupStateDto(TEST_ENDPOINT_GROUP_ID, null, null));
        profileDto.setGroupState(groupState);
        profileDto.setSdkToken(UUID.randomUUID().toString());
        return endpointProfileDao.save(new CassandraEndpointProfile(profileDto)).toDto();
    }

    protected EndpointUserDto generateEndpointUser() {
        return generateEndpointUser(null);
    }

    protected EndpointUserDto generateEndpointUser(List<String> endpointIds) {
        EndpointUserDto endpointUserDto = new EndpointUserDto();
        endpointUserDto.setExternalId(UUID.randomUUID().toString());
        endpointUserDto.setUsername("Test username");
        endpointUserDto.setTenantId(UUID.randomUUID().toString());
        endpointUserDto.setEndpointIds(endpointIds);
        return endpointUserDao.save(new CassandraEndpointUser(endpointUserDto)).toDto();
    }

    protected byte[] generateBytes() {
        return UUID.randomUUID().toString().getBytes();
    }

    protected String generateStringId() {
        return UUID.randomUUID().toString();
    }

    protected EndpointRegistrationDto generateEndpointRegistration(
            String applicationId,
            String endpointId,
            String credentialsId,
            Integer serverProfileVersion,
            String serverProfileBody) {

        EndpointRegistrationDto endpointRegistration = new EndpointRegistrationDto();
        endpointRegistration.setApplicationId(applicationId);
        endpointRegistration.setEndpointId(endpointId);
        endpointRegistration.setCredentialsId(credentialsId);
        endpointRegistration.setServerProfileVersion(serverProfileVersion);
        endpointRegistration.setServerProfileBody(serverProfileBody);
        return this.endpointRegistrationDao.save(endpointRegistration).toDto();
    }

    protected CredentialsDto generateCredentials(String applicationId, String credentialsId,
                                                 byte[] credentialsBody, CredentialsStatus status) {
        CredentialsDto credentialsDto = new CredentialsDto();
        credentialsDto.setId(credentialsId);
        credentialsDto.setCredentialsBody(credentialsBody);
        credentialsDto.setStatus(status);
        Credentials saved = this.credentialsDao.save(applicationId, credentialsDto);

        CredentialsDto generatedCredentials = saved.toDto();
        return generatedCredentials;
    }
}
