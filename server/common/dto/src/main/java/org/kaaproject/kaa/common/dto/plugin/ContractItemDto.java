package org.kaaproject.kaa.common.dto.plugin;

public class ContractItemDto {

    private String id;
    private String name;
    private ContractMessageDto inMessage;
    private ContractMessageDto outMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContractMessageDto getInMessage() {
        return inMessage;
    }

    public void setInMessage(ContractMessageDto inMessage) {
        this.inMessage = inMessage;
    }

    public ContractMessageDto getOutMessage() {
        return outMessage;
    }

    public void setOutMessage(ContractMessageDto outMessage) {
        this.outMessage = outMessage;
    }
}
