package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import com.datastax.driver.core.querybuilder.Select;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraTopicKeysByTopicId;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraNfSchemaVersionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_IDS_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_IDS_TOPIC_ID_PROPERTY;

@Repository
public class CassandraTopicKeysByTopicIdDao extends AbstractCassandraDao<CassandraTopicKeysByTopicId, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraTopicKeysByTopicIdDao.class);

    @Override
    protected Class<CassandraTopicKeysByTopicId> getColumnFamilyClass() {
        return CassandraTopicKeysByTopicId.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return TOPIC_IDS_COLUMN_FAMILY_NAME;
    }

    public CassandraNfSchemaVersionType[] getTopicKeysByTopicId(String topicId) {
        LOG.debug("Try to find topic row keys by topic id {}", topicId);
        Select.Where select = select().from(getColumnFamilyName()).where(eq(TOPIC_IDS_TOPIC_ID_PROPERTY, topicId));
        List<CassandraTopicKeysByTopicId> topicKeys = findListByStatement(select);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found topic keys {}", Arrays.toString(topicKeys.toArray()));
        }
        CassandraNfSchemaVersionType[] topicKeyArray = new CassandraNfSchemaVersionType[topicKeys.size()];
        int i = 0;
        for (CassandraTopicKeysByTopicId key : topicKeys) {
            topicKeyArray[i++] = key.getVersionType();
        }
        return topicKeyArray;
    }
}
