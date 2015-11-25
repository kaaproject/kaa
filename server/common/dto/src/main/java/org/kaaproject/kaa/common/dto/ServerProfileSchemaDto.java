package org.kaaproject.kaa.common.dto;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

public class ServerProfileSchemaDto implements HasId {

    private String id;
    private Long createdTime;
    private String applicationId;
    private CTLSchemaDto schemaDto;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public CTLSchemaDto getSchemaDto() {
        return schemaDto;
    }

    public void setSchemaDto(CTLSchemaDto schemaDto) {
        this.schemaDto = schemaDto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerProfileSchemaDto that = (ServerProfileSchemaDto) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (createdTime != null ? !createdTime.equals(that.createdTime) : that.createdTime != null) return false;
        if (applicationId != null ? !applicationId.equals(that.applicationId) : that.applicationId != null)
            return false;
        return schemaDto != null ? schemaDto.equals(that.schemaDto) : that.schemaDto == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (createdTime != null ? createdTime.hashCode() : 0);
        result = 31 * result + (applicationId != null ? applicationId.hashCode() : 0);
        result = 31 * result + (schemaDto != null ? schemaDto.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ServerProfileSchemaDto{" +
                "id='" + id + '\'' +
                ", createdTime=" + createdTime +
                ", applicationId='" + applicationId + '\'' +
                ", schemaDto=" + schemaDto +
                '}';
    }
}
