package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class CTLSchemaDto implements HasId, Serializable {

    private static final long serialVersionUID = 6967757225688280884L;
    
    private String id;
    private String fqn;
    private int version;
    private CTLSchemaScope scope;
    private String tenantId;
    private String applicationId;
    private String body;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public CTLSchemaScope getScope() {
        return scope;
    }

    public void setScope(CTLSchemaScope scope) {
        this.scope = scope;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
