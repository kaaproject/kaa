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

import static com.datastax.driver.core.querybuilder.QueryBuilder.delete;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ET_NF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ET_NF_ENDPOINT_KEY_HASH_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ET_NF_LAST_MOD_TIME_PROPERTY;

import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointNotificationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointNotificationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter.CassandraEPByAppIdDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;

@Repository(value = "unicastNotificationDao")
public class EndpointNotificationCassandraDao extends AbstractCassandraDao<CassandraEndpointNotification, String> implements EndpointNotificationDao<CassandraEndpointNotification> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointNotificationCassandraDao.class);

    @Autowired
    private CassandraEPByAppIdDao cassandraEPByAppIdDao;

    @Override
    protected Class<CassandraEndpointNotification> getColumnFamilyClass() {
        return CassandraEndpointNotification.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ET_NF_COLUMN_FAMILY_NAME;
    }

    @Override
    public List<CassandraEndpointNotification> findNotificationsByKeyHash(byte[] keyHash) {
        LOG.debug("Try to find endpoint notifications by endpoint key hash {}", keyHash);
        List<CassandraEndpointNotification> cassandraEndpointNotifications = Collections.emptyList();
        if (keyHash != null) {
            Select.Where where = select().from(getColumnFamilyName()).where(eq(ET_NF_ENDPOINT_KEY_HASH_PROPERTY, getByteBuffer(keyHash)));
            LOG.debug("Execute query {}:", where);
            cassandraEndpointNotifications = findListByStatement(where);
        }
        return cassandraEndpointNotifications;
    }

    @Override
    public void removeNotificationsByKeyHash(byte[] keyHash) {
        LOG.debug("Remove endpoint notifications by endpoint key hash {}", keyHash);
        execute(delete().from(getColumnFamilyName()).where(eq(ET_NF_ENDPOINT_KEY_HASH_PROPERTY, getByteBuffer(keyHash))));
    }

    @Override
    public void removeNotificationsByAppId(String appId) {
        LOG.debug("Remove endpoint notifications by app id {}", appId);
        Statement deleteEPNfs = delete().from(getColumnFamilyName()).where(QueryBuilder.in(ET_NF_ENDPOINT_KEY_HASH_PROPERTY, cassandraEPByAppIdDao.getEPIdsListByAppId(appId)));
        LOG.trace("Execute query {}", deleteEPNfs);
        execute(deleteEPNfs);
    }

    @Override
    public CassandraEndpointNotification save(EndpointNotificationDto dto) {
        CassandraEndpointNotification endpointNotification = new CassandraEndpointNotification(dto);
        LOG.debug("Save endpoint notification for endpoint profile {}", endpointNotification.getEndpointKeyHash());
        save(new CassandraEndpointNotification(dto));
        LOG.trace("Saved endpoint notification {}", endpointNotification);
        return endpointNotification;
    }

    @Override
    public CassandraEndpointNotification findById(String id) {
        LOG.debug("Try to find endpoint notifications by id {}", id);
        CassandraEndpointNotification key = new CassandraEndpointNotification(id);
        Select.Where where = select().from(getColumnFamilyName()).where(eq(ET_NF_ENDPOINT_KEY_HASH_PROPERTY, key.getEndpointKeyHash()))
                .and(eq(ET_NF_LAST_MOD_TIME_PROPERTY, key.getLastModifyTime()));
        LOG.debug("[{}] Execute query {}:", id, where);
        CassandraEndpointNotification endpointNotification = findOneByStatement(where);
        LOG.trace("Found endpoint notification {} by id {}:", endpointNotification, id);
        return endpointNotification;
    }

    @Override
    public void removeById(String id) {
        LOG.debug("Remove endpoint notifications by id {}", id);
        CassandraEndpointNotification key = new CassandraEndpointNotification(id);
        Statement delete = delete().from(getColumnFamilyName()).where(eq(ET_NF_ENDPOINT_KEY_HASH_PROPERTY, key.getEndpointKeyHash()))
                .and(eq(ET_NF_LAST_MOD_TIME_PROPERTY, key.getLastModifyTime()));
        execute(delete);
        LOG.debug("[{}] Execute query {}:", id, delete);
    }

}
