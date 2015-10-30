package org.kaaproject.kaa.server.common.dao.model.sql.plugin;

import org.kaaproject.kaa.server.common.dao.model.sql.GenericModel;

import java.io.Serializable;

public class ContractMessage extends GenericModel implements Serializable {

    private String fqn;
    private Integer version;

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

    @Override
    public Object toDto() {
        return null;
    }

    @Override
    protected Object createDto() {
        return null;
    }
}
