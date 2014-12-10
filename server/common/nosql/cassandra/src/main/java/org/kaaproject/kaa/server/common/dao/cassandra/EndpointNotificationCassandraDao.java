package org.kaaproject.kaa.server.common.dao.cassandra;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.*;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointNotification;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNotification;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

@Repository("endpointNotificationDao")
public class EndpointNotificationCassandraDao extends AbstractCassandraDao<CassandraEndpointNotification> implements EndpointNotificationDao<CassandraEndpointNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointNotificationCassandraDao.class);

    @Override
    protected Class<CassandraEndpointNotification> getColumnFamilyClass() {
        return CassandraEndpointNotification.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.ENDPOINT_NOTIFICATION_COLUMN_FAMILY_NAME;
    }

    @Override
    public List<CassandraEndpointNotification> findNotificationsByKeyHash(byte[] keyHash) {
        LOG.debug("Find endpoint notifications by endpoint key hash {}", keyHash);
        List<CassandraEndpointNotification> cassandraEndpointNotifications = Collections.emptyList();
        if (keyHash != null) {
            Select.Where where = QueryBuilder.select().from(getColumnFamilyName()).where(QueryBuilder.eq(ENDPOINT_KEY_HASH_PROPERTY, ByteBuffer.wrap(keyHash)));
            LOG.debug("Execute query {}:", where);
            cassandraEndpointNotifications = findListByStatement(where);
        }
        return cassandraEndpointNotifications;
    }

    @Override
    public void removeNotificationsByKeyHash(byte[] keyHash) {
        LOG.debug("Remove endpoint notifications by endpoint key hash {}", keyHash);
        getMapper().delete(keyHash);
    }

    @Override
    public void removeNotificationsByAppId(String appId) {

    }

    @Override
    public CassandraEndpointNotification save(CassandraEndpointNotification endpointNotification) {
        LOG.debug("Save endpoint notification {}", endpointNotification);
        CassandraNotification notification = endpointNotification.getNotification();
        executeBatch(BatchStatement.Type.UNLOGGED, getSaveQuery(notification, notification.getClass()), getSaveQuery(endpointNotification));
        return endpointNotification;
    }

    @Override
    public CassandraEndpointNotification save(EndpointNotificationDto dto) {
        return save(new CassandraEndpointNotification(dto));
    }
}
