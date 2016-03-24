package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Arrays;

import static org.kaaproject.kaa.server.common.nosql.mongo.dao.model.MongoModelConstants.*;

@Document(collection = CREDENTIALS)
public class MongoCredentials implements Credentials, Serializable {

    private static final long serialVersionUID = 817998992561126368L;

    @Id
    private String id;
    @Field(CREDENTIALS_BODY)
    private byte[] credentialsBody;
    @Field(CREDENTIAL_STATUS)
    private CredentialsStatus status;
    @Version
    private Long version;

    public MongoCredentials(CredentialsDto dto) {
        this(dto, null);
    }

    public MongoCredentials(CredentialsDto dto, Long version) {
        this.id = dto.getId();
        this.credentialsBody = dto.getCredentialsBody();
        this.status = dto.getStatus();
        this.version = version;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getCredentialsBody() {
        return credentialsBody;
    }

    public void setCredentialsBody(byte[] credentialsBody) {
        this.credentialsBody = credentialsBody;
    }

    public CredentialsStatus getStatus() {
        return status;
    }

    public void setStatus(CredentialsStatus status) {
        this.status = status;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MongoCredentials that = (MongoCredentials) o;

        if (!Arrays.equals(credentialsBody, that.credentialsBody)) return false;
        if (!id.equals(that.id)) return false;
        if (status != that.status) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + Arrays.hashCode(credentialsBody);
        result = 31 * result + status.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MongoCredential{" +
                "id='" + id + '\'' +
                ", credentialsBody=" + Arrays.toString(credentialsBody) +
                ", status=" + status +
                ", version=" + version +
                '}';
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public CredentialsDto toDto() {
        CredentialsDto dto = new CredentialsDto();
        dto.setId(id);
        dto.setCredentialsBody(credentialsBody);
        dto.setStatus(status);
        return dto;
    }
}
