package org.kaaproject.kaa.server.common.nosql.cassandra.dao.filter;

import org.kaaproject.kaa.server.common.nosql.cassandra.dao.AbstractCassandraDao;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraTopicLastSeqNum;
import org.springframework.stereotype.Repository;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_ID_SEQ_NUMBER_COLUMN_FAMILY_NAME;

@Repository
public class CassandraTopicLastSeqNumDao extends AbstractCassandraDao<CassandraTopicLastSeqNum, String> {

    @Override
    protected Class<CassandraTopicLastSeqNum> getColumnFamilyClass() {
        return CassandraTopicLastSeqNum.class;
    }

    @Override
    protected String getColumnFamilyName() {
        return TOPIC_ID_SEQ_NUMBER_COLUMN_FAMILY_NAME;
    }

    public int getLastTopicSequenceNumber(String topicId) {
        int lastSeq = 0;
        CassandraTopicLastSeqNum filter = findById(topicId);
        if (filter != null) {
            lastSeq = filter.getSequenceNumber();
        }
        return lastSeq;
    }
}
