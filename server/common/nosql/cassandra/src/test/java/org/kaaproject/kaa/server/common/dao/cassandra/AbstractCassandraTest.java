package org.kaaproject.kaa.server.common.dao.cassandra;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet;
import org.junit.ClassRule;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointUser;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNotification;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointProfileDao;
import org.kaaproject.kaa.server.common.dao.impl.EndpointUserDao;
import org.kaaproject.kaa.server.common.dao.impl.NotificationDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AbstractCassandraTest {

    private static final Random RANDOM = new Random();

    @ClassRule
    public static CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new ClassPathCQLDataSet("cassandra.cql", "kaa"));

    @Autowired
    protected EndpointNotificationDao<CassandraEndpointNotification> endpointNotificationDao;
    @Autowired
    protected EndpointConfigurationDao<CassandraEndpointConfiguration> endpointConfigurationDao;
    @Autowired
    protected EndpointProfileDao<CassandraEndpointProfile> endpointProfileDao;
    @Autowired
    protected EndpointUserDao<CassandraEndpointUser> userEndpointUserDao;
    @Autowired
    protected NotificationDao<CassandraNotification> notificationDao;

    protected List<CassandraEndpointNotification> generateEndpointNotification(ByteBuffer endpointKeyHash, int count) {
        List<CassandraEndpointNotification> savedNotifications = new ArrayList<>();
        String appId = generateStringId();
        if (endpointKeyHash == null) {
            endpointKeyHash = ByteBuffer.wrap(generateEndpointProfile(appId, null, null).getEndpointKeyHash());
        }
        String topicId = generateStringId();
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
            notification.setApplicationId(appId != null ? appId : UUID.randomUUID().toString());
            notification.setSchemaId(schemaId != null ? schemaId : UUID.randomUUID().toString());
            notification.setTopicId(topicId != null ? topicId : UUID.randomUUID().toString());
            notification.setType(type != null ? type : NotificationTypeDto.USER);
            notification.setSecNum(i);
            notification.setBody(UUID.randomUUID().toString().getBytes());
            notification.setLastTimeModify(new Date(System.currentTimeMillis()));
            notification.setVersion(1);
            notification.setExpiredAt(new Date(System.currentTimeMillis() + 7 * 24 * 3600 * 1000));
            notifications.add(notificationDao.save(notification).toDto());
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

    protected EndpointProfileDto generateEndpointProfile(String appId, String accessToken, List<String> topicIds) {
        byte[] keyHash = generateBytes();

        if (appId == null) {
            appId = generateStringId();
        }

        if (accessToken == null) {
            accessToken = generateStringId();
        }

        EndpointProfileDto profileDto = new EndpointProfileDto();
        profileDto.setApplicationId(appId);
        profileDto.setSubscriptions(topicIds);
        profileDto.setEndpointKeyHash(keyHash);
        profileDto.setAccessToken(accessToken);
        return endpointProfileDao.save(new CassandraEndpointProfile(profileDto)).toDto();
    }

    protected EndpointUserDto generateEndpointUser() {
        EndpointUserDto endpointUserDto = new EndpointUserDto();
        endpointUserDto.setExternalId(UUID.randomUUID().toString());
        endpointUserDto.setUsername("Test username");
        endpointUserDto.setTenantId(UUID.randomUUID().toString());
        return userEndpointUserDao.save(new CassandraEndpointUser(endpointUserDto)).toDto();
    }

    protected byte[] generateBytes() {
        return UUID.randomUUID().toString().getBytes();
    }

    protected String generateStringId() {
        return UUID.randomUUID().toString();
    }
}
