package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

import org.kaaproject.kaa.common.dto.HasId;

public class PluginContractDto implements HasId, Serializable {

    private static final long serialVersionUID = -8204932871903228546L;
    
    private String id;
    private PluginContractDirection direction;
    private ContractDto contract;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public PluginContractDirection getDirection() {
        return direction;
    }

    public void setDirection(PluginContractDirection direction) {
        this.direction = direction;
    }

    public ContractDto getContract() {
        return contract;
    }

    public void setContract(ContractDto contract) {
        this.contract = contract;
    }

}
