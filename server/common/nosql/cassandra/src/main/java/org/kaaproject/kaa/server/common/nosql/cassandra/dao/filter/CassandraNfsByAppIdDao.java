package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import com.datastax.driver.core.Statement;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraNfsByAppId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;

@Repository
public class CassandraNfsByAppIdDao extends AbstractCassandraDao<CassandraNfsByAppId, String> {

    @Override
    protected Class<CassandraNfsByAppId> getColumnFamilyClass() {
        return CassandraNfsByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return CassandraModelConstants.NF_BY_APP_COLUMN_FAMILY_NAME;
    }

    public List<String> findNotificationIdsByAppId(String appId) {
        Statement select = select().from(getColumnFamilyName()).where(eq(CassandraModelConstants.NF_BY_APP_APPLICATION_ID_PROPERTY, appId));
        List<CassandraNfsByAppId> result = findListByStatement(select);
        List<String> ids = new ArrayList<>(result.size());
        for (CassandraNfsByAppId id : result) {
            ids.add(id.getNotificationId());
        }
        return ids;
    }
}
