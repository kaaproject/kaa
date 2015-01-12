package org.kaaproject.kaa.server.common.dao.cassandra.filter;

import com.datastax.driver.core.Statement;
import org.kaaproject.kaa.server.common.dao.cassandra.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNfsByAppId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_BY_APP_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NF_BY_APP_COLUMN_FAMILY_NAME;

@Repository
public class CassandraNfsByAppIdDao extends AbstractCassandraDao<CassandraNfsByAppId, String> {

    @Override
    protected Class<CassandraNfsByAppId> getColumnFamilyClass() {
        return CassandraNfsByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return NF_BY_APP_COLUMN_FAMILY_NAME;
    }

    public List<String> findNotificationIdsByAppId(String appId) {
        Statement select = select().from(getColumnFamilyName()).where(eq(NF_BY_APP_APPLICATION_ID_PROPERTY, appId));
        List<CassandraNfsByAppId> result = findListByStatement(select);
        List<String> ids = new ArrayList<>(result.size());
        for (CassandraNfsByAppId id : result) {
            ids.add(id.getNotificationId());
        }
        return ids;
    }
}
