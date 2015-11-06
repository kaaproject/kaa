package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginDto implements HasId, Serializable {

    private static final long serialVersionUID = -5572266074098498423L;

    private String id;
    private String className;
    private String type;
    private String scope;
    private String confSchema;

    private PluginDto() {
        super();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getConfSchema() {
        return confSchema;
    }

    public void setConfSchema(String confSchema) {
        this.confSchema = confSchema;
    }

}
