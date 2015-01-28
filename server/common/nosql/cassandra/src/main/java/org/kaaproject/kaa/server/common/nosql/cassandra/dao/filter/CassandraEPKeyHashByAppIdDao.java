package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import com.datastax.driver.core.Statement;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEPKeyHashByAppId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Repository
public class CassandraEPKeyHashByAppIdDao extends AbstractCassandraDao<CassandraEPKeyHashByAppId, String> {

    @Override
    protected Class<CassandraEPKeyHashByAppId> getColumnFamilyClass() {
        return CassandraEPKeyHashByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.EP_KEY_HASH_BY_APP_COLUMN_FAMILY_NAME;
    }

    public List<ByteBuffer> findEPKeyHashByAppId(String appId) {
        Statement select = select().from(getColumnFamilyName()).where(eq(CassandraModelConstants.EP_KEY_HASH_BY_APP_APPLICATION_ID_PROPERTY, appId));
        List<CassandraEPKeyHashByAppId> result = findListByStatement(select);
        List<ByteBuffer> epKeyHashList = new ArrayList<>(result.size());
        for (CassandraEPKeyHashByAppId id : result) {
            epKeyHashList.add(id.getEpKeyHash());
        }
        return epKeyHashList;
    }
}
