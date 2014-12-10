package org.kaaproject.kaa.server.common.dao.cassandra;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.kaaproject.kaa.common.dto.NotificationDto;
import org.kaaproject.kaa.common.dto.NotificationSchemaDto;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;
import org.kaaproject.kaa.common.dto.UpdateNotificationDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNotification;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.apache.commons.lang.StringUtils.isBlank;

public class AbstractCassandraTest {

    private static final Random RANDOM = new Random();

//    @Before
//    public void before() throws Exception {
//
//        EmbeddedCassandraServerHelper.startEmbeddedCassandra("embedded-cassandra.yaml");
//        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
//    }
//
//    @After
//    public void after() {
//
//    }

    @Autowired
    protected EndpointNotificationDao<CassandraEndpointNotification> endpointNotificationCassandraDao;

    protected List<CassandraEndpointNotification> generateEndpointNotification(ByteBuffer endpointKeyHash, int count) {
        List<CassandraEndpointNotification> savedNotifications = new ArrayList<>();
        if (endpointKeyHash == null) {
            endpointKeyHash = ByteBuffer.allocate(8).putLong(RANDOM.nextLong()).compact();
        }
        for (int i = 0; i < count; i++) {
            CassandraEndpointNotification endpointNotification = new CassandraEndpointNotification();
            endpointNotification.setEndpointKeyHash(endpointKeyHash);
            NotificationDto notificationDto = generateNotifications(UUID.randomUUID().toString(), 1, NotificationTypeDto.USER).get(0);
            endpointNotification.setId(notificationDto.getId());
            endpointNotification.setNotification(new CassandraNotification(notificationDto));
            savedNotifications.add(endpointNotificationCassandraDao.save(endpointNotification));
        }
        return savedNotifications;
    }

    protected List<NotificationDto> generateNotifications(String topicId, int count, NotificationTypeDto type) {
        List<NotificationDto> notifications = new ArrayList<>(count);
        NotificationDto notification = null;
        for (int i = 0; i < count; i++) {
            notification = new NotificationDto();
            notification.setId(UUID.randomUUID().toString());
            notification.setApplicationId(UUID.randomUUID().toString());
            notification.setSchemaId(UUID.randomUUID().toString());
            notification.setTopicId(topicId);
            notification.setType(type != null ? type : NotificationTypeDto.USER);
            byte[] body = "dao/schema/testBaseData.json".getBytes(Charset.forName("UTF-8"));
            notification.setBody(body);
            notifications.add(notification);
        }
        return notifications;
    }
}
