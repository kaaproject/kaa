package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_APP_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_BODY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_USER_ID;

@Document(collection = USER_CONFIGURATION)
public class MongoEndpointUserConfiguration implements EndpointUserConfiguration, Serializable {

    private static final long serialVersionUID = 7678593961823855167L;

    @Field(USER_CONF_USER_ID)
    private String userId;
    @Field(USER_CONF_APP_TOKEN)
    private String appToken;
    @Field(USER_CONF_SCHEMA_VERSION)
    private Integer schemaVersion;
    @Field(USER_CONF_BODY)
    private byte[] body;

    public MongoEndpointUserConfiguration() {
    }

    public MongoEndpointUserConfiguration(EndpointUserConfigurationDto dto) {
        this.userId = dto.getUserId();
        this.appToken = dto.getAppToken();
        this.schemaVersion = dto.getSchemaVersion();
        this.body = getArrayCopy(dto.getBody());
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppToken() {
        return appToken;
    }

    public void setAppToken(String appToken) {
        this.appToken = appToken;
    }

    public Integer getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(Integer schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public EndpointUserConfigurationDto toDto() {
        EndpointUserConfigurationDto dto = new EndpointUserConfigurationDto();
        dto.setAppToken(appToken);
        dto.setBody(getArrayCopy(body));
        dto.setSchemaVersion(schemaVersion);
        dto.setUserId(userId);
        return dto;
    }
}
