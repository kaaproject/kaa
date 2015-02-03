package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPByAccessToken;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;

@Repository
public class CassandraEPByAccessTokenDao extends AbstractCassandraDao<CassandraEPByAccessToken, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraEPByAccessTokenDao.class);
    @Override
    protected Class<?> getColumnFamilyClass() {
        return CassandraEPByAccessToken.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME;
    }

    public ByteBuffer findEPIdByAccessToken(String accessToken) {
        LOG.debug("Try to find endpoint key hash by access token {}", accessToken);
        ByteBuffer endpointKeyHash = null;
        CassandraEPByAccessToken result = findById(accessToken);
        if (result != null) {
            endpointKeyHash = result.getEndpointKeyHash();
        }
        return endpointKeyHash;
    }
}
