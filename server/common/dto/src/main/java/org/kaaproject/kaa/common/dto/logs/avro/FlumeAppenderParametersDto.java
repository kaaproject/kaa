package org.kaaproject.kaa.common.dto.logs.avro;

import java.io.Serializable;
import java.util.List;

public class FlumeAppenderParametersDto implements Parameters, Serializable {

    private static final long serialVersionUID = -1018064135016401445L;

    private List<HostInfoDto> hosts;
    private FlumeBalancingTypeDto balancingType;

    public FlumeAppenderParametersDto() {
    }

    public FlumeAppenderParametersDto(List<HostInfoDto> hosts) {
        this.hosts = hosts;
    }

    public List<HostInfoDto> getHosts() {
        return hosts;
    }

    public void setHosts(List<HostInfoDto> hosts) {
        this.hosts = hosts;
    }

    public FlumeBalancingTypeDto getBalancingType() {
        return balancingType;
    }

    public void setBalancingType(FlumeBalancingTypeDto balancingType) {
        this.balancingType = balancingType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((balancingType == null) ? 0 : balancingType.hashCode());
        result = prime * result + ((hosts == null) ? 0 : hosts.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlumeAppenderParametersDto other = (FlumeAppenderParametersDto) obj;
        if (balancingType != other.balancingType)
            return false;
        if (hosts == null) {
            if (other.hosts != null)
                return false;
        } else if (!hosts.equals(other.hosts))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "FlumeAppenderParametersDto [hosts=" + hosts + ", balancingType=" + balancingType + "]";
    }

}
