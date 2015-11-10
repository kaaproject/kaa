package org.kaaproject.kaa.server.common.dao.model.sql;

import org.kaaproject.kaa.common.dto.CTLSchemaDto;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "ctl", uniqueConstraints =
@UniqueConstraint(columnNames = {"version", "fqn", "tenant_id"}, name = "ctl_unique_constraint"))
public class CTLSchema extends GenericModel<CTLSchemaDto> implements Serializable {

    private Integer version;
    private String fqn;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "tenant_id", foreignKey = @ForeignKey(name = "fk_ctl_tenant_id"))
    private Tenant tenant;
    @Enumerated(EnumType.STRING)
    private CTLSchemaScope scope;
    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(nullable = true, name = "app_id", foreignKey = @ForeignKey(name = "fk_ctl_app_id"))
    private Application application;
    private String body;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "ctl_dependency",
            joinColumns = {@JoinColumn(name = "parent_id")}, foreignKey = @ForeignKey(name = "fk_ctl_pr_id"),
            inverseJoinColumns = {@JoinColumn(name = "child_id")}, inverseForeignKey = @ForeignKey(name = "fk_ctl_ch_id"))
    private Set<CTLSchema> dependencySet;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public CTLSchemaScope getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScope scope) {
        this.scope = scope;
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

    @Override
    protected CTLSchemaDto createDto() {
        return new CTLSchemaDto();
    }

    @Override
    public CTLSchemaDto toDto() {
        CTLSchemaDto ctlSchemaDto = createDto();
        return ctlSchemaDto;
    }
}
