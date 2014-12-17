package org.kaaproject.kaa.server.common.dao.cassandra;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNotification;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AbstractCassandraTest {

    private static final Random RANDOM = new Random();

    @Autowired
    protected EndpointNotificationDao<CassandraEndpointNotification> endpointNotificationDao;
    @Autowired
    protected EndpointConfigurationDao<CassandraEndpointConfiguration> endpointConfigurationDao;
    @Autowired
    private EndpointProfileDao<CassandraEndpointProfile> endpointProfileDao;

    protected List<CassandraEndpointNotification> generateEndpointNotification(ByteBuffer endpointKeyHash, int count) {
        List<CassandraEndpointNotification> savedNotifications = new ArrayList<>();
        if (endpointKeyHash == null) {
            endpointKeyHash = ByteBuffer.allocate(8).putLong(RANDOM.nextLong()).compact();
        }
        String topicId = generateStringId();
        String appId = generateStringId();
        String schemaId = generateStringId();
        for (int i = 0; i < count; i++) {
            CassandraEndpointNotification endpointNotification = new CassandraEndpointNotification();
            endpointNotification.setEndpointKeyHash(endpointKeyHash);
            NotificationDto notificationDto = generateNotifications(topicId, appId, schemaId, 1, NotificationTypeDto.USER).get(0);
            endpointNotification.setId(notificationDto.getId());
            endpointNotification.setNotification(new CassandraNotification(notificationDto));
            savedNotifications.add(endpointNotificationDao.save(endpointNotification));
        }
        return savedNotifications;
    }

    protected List<NotificationDto> generateNotifications(String topicId, String appId, String schemaId, int count, NotificationTypeDto type) {
        List<NotificationDto> notifications = new ArrayList<>(count);
        NotificationDto notification;
        for (int i = 0; i < count; i++) {
            notification = new NotificationDto();
            notification.setId(UUID.randomUUID().toString());
            notification.setApplicationId(appId);
            notification.setSchemaId(schemaId);
            notification.setTopicId(topicId);
            notification.setType(type != null ? type : NotificationTypeDto.USER);
            byte[] body = "dao/schema/testBaseData.json".getBytes(Charset.forName("UTF-8"));
            notification.setBody(body);
            notifications.add(notification);
        }
        return notifications;
    }

    protected List<CassandraEndpointConfiguration> generateConfiguration(int count) {
        List<CassandraEndpointConfiguration> configurations = new ArrayList();
        for (int i = 0; i < count; i++) {
            CassandraEndpointConfiguration configuration = new CassandraEndpointConfiguration();
            configuration.setConfiguration(ByteBuffer.wrap(generateBytes()));
            configuration.setConfigurationHash(ByteBuffer.wrap(generateBytes()));
            configurations.add(endpointConfigurationDao.save(configuration));
        }
        return configurations;
    }

    protected EndpointProfileDto generateEndpointProfile(String appId, String accessToken, byte[] keyHash, List<String> topicIds) {
        if (keyHash == null) {
            keyHash = "TEST_KEY_HASH".getBytes();
        }
        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setSubscriptions(topicIds);
        profileDto.setEndpointKeyHash(keyHash);
        return endpointProfileDao.save(new CassandraEndpointProfile(profileDto)).toDto();
    }

    protected byte[] generateBytes() {
        return UUID.randomUUID().toString().getBytes();
    }

    protected String generateStringId() {
        return UUID.randomUUID().toString();
    }
}
