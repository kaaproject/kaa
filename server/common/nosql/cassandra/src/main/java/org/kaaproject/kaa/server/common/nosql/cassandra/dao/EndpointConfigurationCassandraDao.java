package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointConfiguration;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;

@Repository(value = "endpointConfigurationDao")
public class EndpointConfigurationCassandraDao extends AbstractCassandraDao<CassandraEndpointConfiguration, ByteBuffer> implements EndpointConfigurationDao<CassandraEndpointConfiguration> {

    private static final Logger LOG = LoggerFactory.getLogger(EndpointConfigurationCassandraDao.class);

    @Override
    protected Class<?> getColumnFamilyClass() {
        return CassandraEndpointConfiguration.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.ENDPOINT_CONFIGURATION_COLUMN_FAMILY_NAME;
    }

    @Override
    public CassandraEndpointConfiguration findByHash(final byte[] hash) {
        LOG.debug("Find endpoint configuration by hash [{}] ", hash);
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
        CassandraEndpointConfiguration configuration = null;
        if (key != null) {
            configuration = findByHash(getBytes(key));
        }
        return configuration;
    }

    @Override
    public void removeById(ByteBuffer key) {
        if (key != null) {
            removeByHash(getBytes(key));
        }
    }
}
