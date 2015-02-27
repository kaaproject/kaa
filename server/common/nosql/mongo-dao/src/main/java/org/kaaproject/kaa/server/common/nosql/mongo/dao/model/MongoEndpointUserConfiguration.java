package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = MongoEndpointUserConfiguration.COLLECTION_NAME)
public class MongoEndpointUserConfiguration implements EndpointUserConfiguration, Serializable {

    private static final long serialVersionUID = 7678593961823855167L;

    public static final String COLLECTION_NAME = "user_configuration";

    @Field("user_id")
    private String userId;
    @Field("app_token")
    private String appToken;
    @Field("schema_version")
    private Integer schemaVersion;
    @Field("body")
    private byte[] body;

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
        return null;
    }
}
