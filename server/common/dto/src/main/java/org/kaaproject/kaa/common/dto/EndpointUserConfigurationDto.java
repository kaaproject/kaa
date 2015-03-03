package org.kaaproject.kaa.common.dto;

import java.io.Serializable;
import java.util.Arrays;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndpointUserConfigurationDto that = (EndpointUserConfigurationDto) o;

        if (appToken != null ? !appToken.equals(that.appToken) : that.appToken != null) return false;
        if (!Arrays.equals(body, that.body)) return false;
        if (schemaVersion != null ? !schemaVersion.equals(that.schemaVersion) : that.schemaVersion != null)
            return false;
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (appToken != null ? appToken.hashCode() : 0);
        result = 31 * result + (schemaVersion != null ? schemaVersion.hashCode() : 0);
        result = 31 * result + (body != null ? Arrays.hashCode(body) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "EndpointUserConfigurationDto{" +
                "userId='" + userId + '\'' +
                ", appToken='" + appToken + '\'' +
                ", schemaVersion=" + schemaVersion +
                ", body=" + Arrays.toString(body) +
                '}';
    }
}
