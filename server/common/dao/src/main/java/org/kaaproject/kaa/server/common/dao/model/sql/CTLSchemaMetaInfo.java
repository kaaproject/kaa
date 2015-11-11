package org.kaaproject.kaa.server.common.dao.model.sql;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaMetaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;

@Entity
@Table(name = "ctl_metainfo", uniqueConstraints =
@UniqueConstraint(columnNames = {"version", "fqn"}, name = "ctl_metainfo_unique_constraint"))
public class CTLSchemaMetaInfo extends GenericModel<CTLSchemaMetaInfoDto> implements Serializable {

    private String fqn;
    private Integer version;
    @Enumerated(EnumType.STRING)
    private CTLSchemaScopeDto schemaScopeDto;
    @Column(name = "count")
    private Long count = 0L;

    public CTLSchemaMetaInfo() {
    }

    public CTLSchemaMetaInfo(CTLSchemaMetaInfoDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.fqn = dto.getFqn();
        this.version = dto.getVersion();
        this.schemaScopeDto = dto.getSchemaScopeDto();
    }

    public CTLSchemaMetaInfo(String fqn, Integer version) {
        this.fqn = fqn;
        this.version = version;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public CTLSchemaScopeDto getSchemaScopeDto() {
        return schemaScopeDto;
    }

    public void setSchemaScopeDto(CTLSchemaScopeDto schemaScopeDto) {
        this.schemaScopeDto = schemaScopeDto;
    }

    @Override
    protected CTLSchemaMetaInfoDto createDto() {
        return new CTLSchemaMetaInfoDto();
    }

    @Override
    public CTLSchemaMetaInfoDto toDto() {
        CTLSchemaMetaInfoDto ctlSchemaMetaInfoDto = createDto();
        ctlSchemaMetaInfoDto.setId(getStringId());
        ctlSchemaMetaInfoDto.setFqn(fqn);
        ctlSchemaMetaInfoDto.setVersion(version);
        ctlSchemaMetaInfoDto.setSchemaScopeDto(schemaScopeDto);
        return ctlSchemaMetaInfoDto;
    }

    public Long getCount() {
        return count;
    }

    public Long incrementCount() {
        return count++;
    }

    @Override
    public String toString() {
        return "CTLSchemaMetaInfo{" +
                "fqn='" + fqn + '\'' +
                ", version=" + version +
                ", schemaScopeDto=" + schemaScopeDto +
                ", count=" + count +
                '}';
    }
}
