package org.kaaproject.kaa.server.common.dao.cassandra.filter;

import org.kaaproject.kaa.server.common.dao.cassandra.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEPByAppId;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_BY_APP_ID_COLUMN_FAMILY_NAME;
import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Repository
public class CassandraEPByAppIdDao extends AbstractCassandraDao<CassandraEPByAppId> {

    @Override
    protected Class<CassandraEPByAppId> getColumnFamilyClass() {
        return CassandraEPByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_BY_APP_ID_COLUMN_FAMILY_NAME;
    }

    public List<ByteBuffer> getEPIdsListByAppId(String appId) {
        List<CassandraEPByAppId> filter = findListByStatement(select().from(getColumnFamilyName()).where(eq(EP_BY_APP_ID_APPLICATION_ID_PROPERTY, appId)));
        List<ByteBuffer> result = new ArrayList<>(filter.size());
        for (CassandraEPByAppId ep : filter) {
            result.add(ep.getEndpointKeyHash());
        }
        return result;
    }
}
