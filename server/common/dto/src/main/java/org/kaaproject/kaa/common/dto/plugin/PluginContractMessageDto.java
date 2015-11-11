package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginContractMessageDto implements HasId, Serializable {

    private static final long serialVersionUID = 5198084684198169441L;

    private String id;
    private String fqn;
    private int version;

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

}
