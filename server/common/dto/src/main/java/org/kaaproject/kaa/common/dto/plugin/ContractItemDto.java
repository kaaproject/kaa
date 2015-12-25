package org.kaaproject.kaa.common.dto.plugin;

import java.io.Serializable;

public class ContractItemDto implements Serializable {

    private static final long serialVersionUID = -1307520126505582838L;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContractItemDto)) {
            return false;
        }

        ContractItemDto that = (ContractItemDto) o;

        if (inMessage != null ? !inMessage.equals(that.inMessage) : that.inMessage != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (outMessage != null ? !outMessage.equals(that.outMessage) : that.outMessage != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (inMessage != null ? inMessage.hashCode() : 0);
        result = 31 * result + (outMessage != null ? outMessage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ContractItemDto{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", inMessage=").append(inMessage);
        sb.append(", outMessage=").append(outMessage);
        sb.append('}');
        return sb.toString();
    }
}
