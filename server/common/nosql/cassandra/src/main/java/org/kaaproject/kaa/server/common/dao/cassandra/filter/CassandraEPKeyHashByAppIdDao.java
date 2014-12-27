package org.kaaproject.kaa.server.common.dao.cassandra.filter;

import com.datastax.driver.core.Statement;
import org.kaaproject.kaa.server.common.dao.cassandra.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraEPKeyHashByAppId;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_KEY_HASH_BY_APP_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.EP_KEY_HASH_BY_APP_COLUMN_FAMILY_NAME;

@Repository
public class CassandraEPKeyHashByAppIdDao extends AbstractCassandraDao<CassandraEPKeyHashByAppId> {

    @Override
    protected Class<CassandraEPKeyHashByAppId> getColumnFamilyClass() {
        return CassandraEPKeyHashByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return EP_KEY_HASH_BY_APP_COLUMN_FAMILY_NAME;
    }

    public List<ByteBuffer> findEPKeyHashByAppId(String appId) {
        Statement select = select().from(getColumnFamilyName()).where(eq(EP_KEY_HASH_BY_APP_APPLICATION_ID_PROPERTY, appId));
        List<CassandraEPKeyHashByAppId> result = findListByStatement(select);
        List<ByteBuffer> epKeyHashList = new ArrayList<>(result.size());
        for (CassandraEPKeyHashByAppId id : result) {
            epKeyHashList.add(id.getEpKeyHash());
        }
        return epKeyHashList;
    }
}
