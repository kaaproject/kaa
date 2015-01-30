package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type.CassandraNfSchemaVersionType;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_IDS_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_IDS_VER_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.TOPIC_IDS_TOPIC_ID_PROPERTY;

@Table(name = TOPIC_IDS_COLUMN_FAMILY_NAME)
public class CassandraTopicKeysByTopicId implements Serializable {

    @Transient
    private static final long serialVersionUID = 273632080362485980L;

    @PartitionKey
    @Column(name = TOPIC_IDS_TOPIC_ID_PROPERTY)
    private String topicId;
    @Frozen
    @ClusteringColumn
    @Column(name = TOPIC_IDS_VER_TYPE_PROPERTY)
    private CassandraNfSchemaVersionType versionType;

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public CassandraNfSchemaVersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(CassandraNfSchemaVersionType versionType) {
        this.versionType = versionType;
    }
}
