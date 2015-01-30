package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_ID_SEQ_NUMBER_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_ID_SEQ_NUMBER_SEQUENCE_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_ID_SEQ_NUMBER_TOPIC_ID_PROPERTY;

@Table(name = TOPIC_ID_SEQ_NUMBER_COLUMN_FAMILY_NAME)
public class CassandraTopicLastSeqNum implements Serializable {

    @Transient
    private static final long serialVersionUID = 4739723413548891497L;

    @PartitionKey
    @Column(name = TOPIC_ID_SEQ_NUMBER_TOPIC_ID_PROPERTY)
    private String topicId;
    @Column(name = TOPIC_ID_SEQ_NUMBER_SEQUENCE_NUMBER_PROPERTY)
    private Integer sequenceNumber;

    public CassandraTopicLastSeqNum() {
    }

    public CassandraTopicLastSeqNum(String topicId, Integer sequenceNumber) {
        this.topicId = topicId;
        this.sequenceNumber = sequenceNumber;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
