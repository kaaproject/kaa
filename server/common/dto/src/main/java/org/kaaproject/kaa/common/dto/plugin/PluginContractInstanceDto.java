package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginContractInstanceDto implements HasId, Serializable {

    private static final long serialVersionUID = -2398551245259052576L;

    private String id;
    private PluginContractDto contract;
    

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PluginContractDto getContract() {
        return contract;
    }

    public void setContract(PluginContractDto contract) {
        this.contract = contract;
    }

}
