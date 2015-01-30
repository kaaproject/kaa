package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.type;

import com.datastax.driver.mapping.EnumType;
import com.datastax.driver.mapping.annotations.Enumerated;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.Transient;
import com.datastax.driver.mapping.annotations.UDT;
import org.kaaproject.kaa.common.dto.NotificationTypeDto;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.KEY_DELIMITER;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SCHEMA_VER_BATCH_NUMBER_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SCHEMA_VER_NF_TYPE_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SCHEMA_VER_USER_TYPE_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.NF_SCHEMA_VER_VERSION_PROPERTY;

@UDT(name = NF_SCHEMA_VER_USER_TYPE_NAME)
public class CassandraNfSchemaVersionType implements Serializable {

    @Transient
    private static final long serialVersionUID = 2473526262335623879L;

    @Field(name = NF_SCHEMA_VER_VERSION_PROPERTY)
    private int version;
    @Field(name = NF_SCHEMA_VER_NF_TYPE_PROPERTY)
    private String type;
    @Field(name = NF_SCHEMA_VER_BATCH_NUMBER_PROPERTY)
    private int batchNumber;

    public CassandraNfSchemaVersionType() {
    }

    public CassandraNfSchemaVersionType(String type, String version, String batchNumber) {
        this.version = Integer.valueOf(version);
        this.type = type;
        this.batchNumber = Integer.valueOf(batchNumber);
    }

    public CassandraNfSchemaVersionType(NotificationTypeDto type, int version, int batchNumber) {
        this.version = version;
        this.type = type.name();
        this.batchNumber = batchNumber;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public NotificationTypeDto getType() {
        return NotificationTypeDto.valueOf(type);
    }

    public void setType(NotificationTypeDto type) {
        this.type = type.name();
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(int batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getStringId() {
        return type + KEY_DELIMITER + version + KEY_DELIMITER + batchNumber;
    }
}
