package org.kaaproject.kaa.server.common.dao.cassandra;

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEndpointConfiguration;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants;
import org.kaaproject.kaa.server.common.dao.impl.EndpointConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.dao.cassandra.CassandraDaoUtil.getByteBuffer;

@Repository("endpointConfigurationDao")
public class EndpointConfigurationCassandraDao extends AbstractCassandraDao<CassandraEndpointConfiguration> implements EndpointConfigurationDao<CassandraEndpointConfiguration> {

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
}
