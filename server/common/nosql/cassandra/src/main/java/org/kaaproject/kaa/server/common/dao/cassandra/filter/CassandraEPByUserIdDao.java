package org.kaaproject.kaa.server.common.dao.cassandra.filter;

import org.kaaproject.kaa.server.common.dao.cassandra.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEPByUserId;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_USER_ID_COLUMN_FAMILY_NAME;

@Repository
public class CassandraEPByUserIdDao extends AbstractCassandraDao<CassandraEPByUserId> {

    @Override
    protected Class<CassandraEPByUserId> getColumnFamilyClass() {
        return CassandraEPByUserId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_BY_USER_ID_COLUMN_FAMILY_NAME;
    }

    public List<ByteBuffer> findEPKeyHashListByUserId(String userId) {
        List<CassandraEPByUserId> filter = findListByStatement(getMapper().getQuery(userId));
        List<ByteBuffer> result = new ArrayList<>(filter.size());
        for (CassandraEPByUserId ep : filter) {
            result.add(ep.getEndpointKeyHash());
        }
        return result;
    }

}
