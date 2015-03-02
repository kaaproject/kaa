package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.server.common.dao.impl.DaoUtil.getArrayCopy;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONFIGURATION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_APP_TOKEN;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_BODY;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_SCHEMA_VERSION;
import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.USER_CONF_USER_ID;

@Document(collection = USER_CONFIGURATION)
public class MongoEndpointUserConfiguration implements EndpointUserConfiguration, Serializable {

    private static final long serialVersionUID = 7678593961823855167L;
    private static final String ID_DELIMITER = "|";

    @Id
    private String id;
    @Indexed
    @Field(USER_CONF_USER_ID)
    private String userId;
    @Indexed
    @Field(USER_CONF_APP_TOKEN)
    private String appToken;
    @Indexed
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
        this.id = userId + ID_DELIMITER + appToken + ID_DELIMITER + schemaVersion;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MongoEndpointUserConfiguration that = (MongoEndpointUserConfiguration) o;

        if (appToken != null ? !appToken.equals(that.appToken) : that.appToken != null) return false;
        if (!Arrays.equals(body, that.body)) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (schemaVersion != null ? !schemaVersion.equals(that.schemaVersion) : that.schemaVersion != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (appToken != null ? appToken.hashCode() : 0);
        result = 31 * result + (schemaVersion != null ? schemaVersion.hashCode() : 0);
        result = 31 * result + (body != null ? Arrays.hashCode(body) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MongoEndpointUserConfiguration{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", appToken='" + appToken + '\'' +
                ", schemaVersion=" + schemaVersion +
                ", body=" + Arrays.toString(body) +
                '}';
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
