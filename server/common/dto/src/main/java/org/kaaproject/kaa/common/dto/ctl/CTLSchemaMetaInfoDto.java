package org.kaaproject.kaa.common.dto.ctl;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class CTLSchemaMetaInfoDto implements HasId, Serializable {

    private static final long serialVersionUID = -8767931371388908285L;

    private String id;
    private String fqn;
    private Integer version;
    private CTLSchemaScope scope;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
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

    public CTLSchemaScope getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScope scope) {
        this.scope = scope;
    }
}
