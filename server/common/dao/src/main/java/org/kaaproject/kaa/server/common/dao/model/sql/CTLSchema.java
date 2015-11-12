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
import java.util.Set;

import static org.kaaproject.kaa.server.common.dao.model.sql.ModelUtils.getLongId;

@Entity
@Table(name = "ctl", uniqueConstraints =
@UniqueConstraint(columnNames = {"metainfo_id", "tenant_id"}, name = "ctl_unique_constraint"))
public class CTLSchema extends GenericModel<CTLSchemaDto> implements Serializable {

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "metainfo_id", foreignKey = @ForeignKey(name = "fk_ctl_metainfo_id"))
    private CTLSchemaMetaInfo metaInfo;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "tenant_id", foreignKey = @ForeignKey(name = "fk_ctl_tenant_id"))
    private Tenant tenant;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "app_id", foreignKey = @ForeignKey(name = "fk_ctl_app_id"))
    private Application application;
    @Lob
    private String body;

    private String name;

    @Column(length = 1000)
    private String description;

    private String createdUsername;

    private long createdTime;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "ctl_dependency",
            joinColumns = {@JoinColumn(name = "parent_id")}, foreignKey = @ForeignKey(name = "fk_ctl_pr_id"),
            inverseJoinColumns = {@JoinColumn(name = "child_id")}, inverseForeignKey = @ForeignKey(name = "fk_ctl_ch_id"))
    private Set<CTLSchema> dependencySet;

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

    private void updateCreatedTime(){}

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
}
