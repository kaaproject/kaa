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

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.ENDPOINT_CONFIGURATION_COLUMN_FAMILY_NAME;

import java.nio.ByteBuffer;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository(value = "endpointConfigurationDao")
public class EndpointConfigurationCassandraDao extends AbstractCassandraDao<CassandraEndpointConfiguration, ByteBuffer>
        implements EndpointConfigurationDao<CassandraEndpointConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfigurationCassandraDao.class);

    @Override
    protected Class<CassandraEndpointConfiguration> getColumnFamilyClass() {
        return CassandraEndpointConfiguration.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return ENDPOINT_CONFIGURATION_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointConfiguration findByHash(final byte[] hash) {
        LOG.debug("Try to find endpoint configuration by hash [{}] ", hash);
        return (CassandraEndpointConfiguration) getMapper().get(getByteBuffer(hash));
    }

    @Override
    public void removeByHash(final byte[] hash) {
        LOG.debug("Remove endpoint configuration by hash [{}] ", hash);
        getMapper().delete(getByteBuffer(hash));
    }

    @Override
    public CassandraEndpointConfiguration save(EndpointConfigurationDto dto) {
        LOG.debug("Save endpoint configuration [{}] ", dto);
        return save(new CassandraEndpointConfiguration(dto));
    }

    @Override
    public CassandraEndpointConfiguration findById(ByteBuffer key) {
        LOG.debug("Try to find endpoint configuration by hash [{}] ", key);
        CassandraEndpointConfiguration configuration = null;
        if (key != null) {
            configuration = findByHash(getBytes(key));
        }
        LOG.debug("[{}] Found endpoint configuration by hash {} ", key, configuration);
        return configuration;
    }

    @Override
    public void removeById(ByteBuffer key) {
        LOG.debug("Remove endpoint configuration by hash [{}] ", key);
        if (key != null) {
            removeByHash(getBytes(key));
        }
    }

}
