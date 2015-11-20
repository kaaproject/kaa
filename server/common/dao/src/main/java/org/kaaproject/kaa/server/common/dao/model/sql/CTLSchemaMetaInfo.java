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

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_FQN;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_SCOPE;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_UNIQUE_CONSTRAINT;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_VERSION;

@Entity
@Table(name = CTL_SCHEMA_META_INFO_TABLE_NAME, uniqueConstraints =
@UniqueConstraint(columnNames = {CTL_SCHEMA_META_INFO_VERSION, CTL_SCHEMA_META_INFO_FQN}, name = CTL_SCHEMA_META_INFO_UNIQUE_CONSTRAINT))
public class CTLSchemaMetaInfo extends GenericModel<CTLSchemaMetaInfoDto> implements Serializable {

    @Column(name = CTL_SCHEMA_META_INFO_FQN)
    private String fqn;
    @Column(name = CTL_SCHEMA_META_INFO_VERSION)
    private Integer version;
    @Column(name = CTL_SCHEMA_META_INFO_SCOPE, nullable = false)
    @Enumerated(EnumType.STRING)
    private CTLSchemaScopeDto scope;
    @Column(name = "count")
    private Long count = 0L;

    public CTLSchemaMetaInfo() {
    }

    public CTLSchemaMetaInfo(CTLSchemaMetaInfoDto dto) {
        this.id = ModelUtils.getLongId(dto.getId());
        this.fqn = dto.getFqn();
        this.version = dto.getVersion();
        this.scope = dto.getScope();
        this.count = dto.getCount();
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

    public CTLSchemaScopeDto getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScopeDto scope) {
        this.scope = scope;
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
        ctlSchemaMetaInfoDto.setScope(scope);
        ctlSchemaMetaInfoDto.setCount(count);
        return ctlSchemaMetaInfoDto;
    }

    public Long getCount() {
        return count;
    }

    public Long incrementCount() {
        return count++;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CTLSchemaMetaInfo metaInfo = (CTLSchemaMetaInfo) o;

        if (fqn != null ? !fqn.equals(metaInfo.fqn) : metaInfo.fqn != null) return false;
        if (version != null ? !version.equals(metaInfo.version) : metaInfo.version != null) return false;
        if (scope != metaInfo.scope) return false;
        return count != null ? count.equals(metaInfo.count) : metaInfo.count == null;

    }

    @Override
    public int hashCode() {
        int result = fqn != null ? fqn.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CTLSchemaMetaInfo{" +
                "fqn='" + fqn + '\'' +
                ", version=" + version +
                ", scope=" + scope +
                ", count=" + count +
                '}';
    }
}
