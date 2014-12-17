package org.kaaproject.kaa.server.common.dao.cassandra.filter;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.*;

import org.kaaproject.kaa.server.common.dao.cassandra.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEPByAccessToken;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;

@Repository
public class CassandraEPByAccessTokenDao extends AbstractCassandraDao<CassandraEPByAccessToken> {

    @Override
    protected Class<?> getColumnFamilyClass() {
        return CassandraEPByAccessTokenDao.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_BY_ACCESS_TOKEN_COLUMN_FAMILY_NAME;
    }

    public ByteBuffer findEPIdByAccessToken(String accessToken) {
        ByteBuffer endpointKeyHash = null;
        CassandraEPByAccessToken result = findById(accessToken);
        if (result != null) {
            endpointKeyHash = result.getEndpointKeyHash();
        }
        return endpointKeyHash;
    }
}
