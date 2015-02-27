package org.kaaproject.kaa.common.dto;

import java.io.Serializable;

public class EndpointUserConfigurationDto implements Serializable {

    private static final long serialVersionUID = -1463982688020241482L;

    private String userId;
    private String appToken;
    private Integer schemaVersion;
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
}
