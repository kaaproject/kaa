package org.kaaproject.kaa.server.common.nosql.cassandra.dao.model;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;

import java.io.Serializable;
import java.nio.ByteBuffer;

import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getByteBuffer;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.CassandraDaoUtil.getBytes;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_APP_TOKEN_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_BODY_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_COLUMN_FAMILY_NAME;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_USER_ID_PROPERTY;
import static org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraModelConstants.EP_USER_CONF_VERSION_PROPERTY;

@Table(name = EP_USER_CONF_COLUMN_FAMILY_NAME)
public class CassandraEndpointUserConfiguration implements EndpointUserConfiguration, Serializable {

    @Transient
    private static final long serialVersionUID = 7678593961823855167L;

    @PartitionKey
    @Column(name = EP_USER_CONF_USER_ID_PROPERTY)
    private String userId;
    @ClusteringColumn(value = 0)
    @Column(name = EP_USER_CONF_APP_TOKEN_PROPERTY)
    private String appToken;
    @ClusteringColumn(value = 1)
    @Column(name = EP_USER_CONF_VERSION_PROPERTY)
    private Integer schemaVersion;
    @Column(name = EP_USER_CONF_BODY_PROPERTY)
    private ByteBuffer body;

    public CassandraEndpointUserConfiguration() {
    }

    public CassandraEndpointUserConfiguration(EndpointUserConfigurationDto dto) {
        this.userId = dto.getUserId();
        this.appToken = dto.getAppToken();
        this.schemaVersion = dto.getSchemaVersion();
        this.body = getByteBuffer(dto.getBody());
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getAppToken() {
        return appToken;
    }

    @Override
    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    @Override
    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    @Override
    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ByteBuffer getBody() {
        return body;
    }

    public void setBody(ByteBuffer body) {
        this.body = body;
    }

    @Override
    public EndpointUserConfigurationDto toDto() {
        EndpointUserConfigurationDto dto = new EndpointUserConfigurationDto();
        dto.setAppToken(appToken);
        dto.setBody(getBytes(body));
        dto.setSchemaVersion(schemaVersion);
        dto.setUserId(userId);
        return dto;
    }
}
