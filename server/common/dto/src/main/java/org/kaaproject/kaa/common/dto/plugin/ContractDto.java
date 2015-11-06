package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class ContractDto implements HasId, Serializable {

    private static final long serialVersionUID = -3635444368421499582L;
    
    private String id;
    private String name;
    private Integer version;
    private ContractType type;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ContractType getType() {
        return type;
    }

    public void setType(ContractType type) {
        this.type = type;
    }

}
