package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointAddress extends EntityAddress {

    private final EndpointObjectHash endpointKey;

    public EndpointAddress(String tenantId, String appToken, EndpointObjectHash endpointKey) {
        super(tenantId, appToken);
        this.endpointKey = endpointKey;
    }

    @Override
    public byte[] getEntityId() {
        return endpointKey.getData();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((endpointKey == null) ? 0 : endpointKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EndpointAddress other = (EndpointAddress) obj;
        if (endpointKey == null) {
            if (other.endpointKey != null)
                return false;
        } else if (!endpointKey.equals(other.endpointKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EndpointAddress [endpointKey=" + endpointKey + ", getTenantId()=" + getTenantId() + ", getAppToken()=" + getAppToken()
                + "]";
    }

}
