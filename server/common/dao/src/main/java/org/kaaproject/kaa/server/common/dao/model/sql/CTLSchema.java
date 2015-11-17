package org.kaaproject.kaa.server.common.dao.model.sql;

import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_APPLICATION_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_APPLICATION_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_BODY;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_CREATED_TIME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_CREATED_USERNAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_DESCRIPTION;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_CHILD_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_CHILD_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_PARENT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_JOIN_TABLE_PARENT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_META_INFO_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TABLE_NAME;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TENANT_FK;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_TENANT_ID;
import static org.kaaproject.kaa.server.common.dao.DaoConstants.CTL_SCHEMA_UNIQUE_CONSTRAINT;
import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = CTL_SCHEMA_TABLE_NAME, uniqueConstraints =
@UniqueConstraint(columnNames = {CTL_SCHEMA_META_INFO_ID, CTL_SCHEMA_TENANT_ID}, name = CTL_SCHEMA_UNIQUE_CONSTRAINT))
public class CTLSchema extends GenericModel<CTLSchemaDto> implements Serializable {

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = CTL_SCHEMA_META_INFO_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_META_INFO_FK))
    private CTLSchemaMetaInfo metaInfo;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = CTL_SCHEMA_TENANT_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_TENANT_FK))
    private Tenant tenant;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = CTL_SCHEMA_APPLICATION_ID, foreignKey = @ForeignKey(name = CTL_SCHEMA_APPLICATION_FK))
    private Application application;
    @Lob
    @Column(name = CTL_SCHEMA_BODY)
    private String body;
    @Column(name = CTL_SCHEMA_NAME)
    private String name;
    @Column(length = 1000, name = CTL_SCHEMA_DESCRIPTION)
    private String description;
    @Column(name = CTL_SCHEMA_CREATED_USERNAME)
    private String createdUsername;
    @Column(name = CTL_SCHEMA_CREATED_TIME)
    private long createdTime;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = CTL_SCHEMA_JOIN_TABLE_NAME,
            joinColumns = {@JoinColumn(name = CTL_SCHEMA_JOIN_TABLE_PARENT_ID)}, foreignKey = @ForeignKey(name = CTL_SCHEMA_JOIN_TABLE_PARENT_FK),
            inverseJoinColumns = {@JoinColumn(name = CTL_SCHEMA_JOIN_TABLE_CHILD_ID)}, inverseForeignKey = @ForeignKey(name = CTL_SCHEMA_JOIN_TABLE_CHILD_FK))
    private Set<CTLSchema> dependencySet = new HashSet<>();

    public CTLSchema() {
    }

    public CTLSchema(CTLSchemaDto dto) {
        this.id = getLongId(dto.getId());
        this.metaInfo = new CTLSchemaMetaInfo(dto.getMetaInfo());

        Long tenantId = getLongId(dto.getTenantId());
        this.tenant = tenantId != null ? new Tenant(tenantId) : null;

        Long appId = getLongId(dto.getAppId());
        this.application = appId != null ? new Application(appId) : null;
        this.body = dto.getBody();

        Set<CTLSchemaDto> dependencies = dto.getDependencySet();
        if (dependencies != null && !dependencies.isEmpty()) {
            for (CTLSchemaDto dependency : dependencies) {
                dependencySet.add(new CTLSchema(dependency));
            }
        }
    }

    public CTLSchemaMetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(CTLSchemaMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Set<CTLSchema> getDependencySet() {
        return dependencySet;
    }

    public void setDependencySet(Set<CTLSchema> dependencySet) {
        this.dependencySet = dependencySet;
    }

    private void updateCreatedTime() {
    }

    @Override
    protected CTLSchemaDto createDto() {
        return new CTLSchemaDto();
    }

    @Override
    public CTLSchemaDto toDto() {
        CTLSchemaDto ctlSchemaDto = createDto();
        ctlSchemaDto.setId(getStringId());
        ctlSchemaDto.setAppId(application != null ? application.getStringId() : null);
        ctlSchemaDto.setTenantId(tenant != null ? tenant.getStringId() : null);
        ctlSchemaDto.setMetaInfo(metaInfo.toDto());
        ctlSchemaDto.setBody(body);
        ctlSchemaDto.setDependencySet(DaoUtil.convertDtoSet(dependencySet));
        return ctlSchemaDto;
    }

    @Override
    public String toString() {
        return "CTLSchema{" +
                "id=" + id +
                ", metaInfo=" + metaInfo +
                ", tenant=" + tenant +
                ", application=" + application +
                ", body='" + body + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdUsername='" + createdUsername + '\'' +
                ", createdTime=" + createdTime +
                ", dependencySet=" + dependencySet +
                '}';
    }
}
