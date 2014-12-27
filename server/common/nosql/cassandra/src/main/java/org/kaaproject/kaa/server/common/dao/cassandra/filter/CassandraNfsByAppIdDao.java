package org.kaaproject.kaa.server.common.dao.cassandra.filter;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

import com.datastax.driver.core.Statement;
import org.kaaproject.kaa.server.common.dao.cassandra.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraNfsByAppId;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NOTIFICATIONS_BY_APPLICATION_APPLICATION_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.dao.cassandra.model.CassandraModelConstants.NOTIFICATIONS_BY_APPLICATION_COLUMN_FAMILY_NAME;

@Repository
public class CassandraNfsByAppIdDao extends AbstractCassandraDao<CassandraNfsByAppId> {

    @Override
    protected Class<CassandraNfsByAppId> getColumnFamilyClass() {
        return CassandraNfsByAppId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return NOTIFICATIONS_BY_APPLICATION_COLUMN_FAMILY_NAME;
    }

    public List<String> findNotificationIdsByAppId(String appId) {
        Statement select = select().from(getColumnFamilyName()).where(eq(NOTIFICATIONS_BY_APPLICATION_APPLICATION_ID_PROPERTY, appId));
        List<CassandraNfsByAppId> result = findListByStatement(select);
        List<String> ids = new ArrayList<>(result.size());
        for (CassandraNfsByAppId id : result) {
            ids.add(id.getNotificationId());
        }
        return ids;
    }
}
