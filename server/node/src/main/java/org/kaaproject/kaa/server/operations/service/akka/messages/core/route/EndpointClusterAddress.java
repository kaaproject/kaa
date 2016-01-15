package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointClusterAddress extends EntityClusterAddress {

    private final EndpointObjectHash endpointKey;

    public EndpointClusterAddress(EntityClusterAddress parent, EndpointObjectHash endpointKey) {
        this(parent.getNodeId(), parent.getTenantId(), parent.getAppToken(), endpointKey);
    }

    public EndpointClusterAddress(String nodeId, String tenantId, String appToken, EndpointObjectHash endpointKey) {
        super(nodeId, tenantId, appToken);
        this.endpointKey = endpointKey;
    }

    public EndpointObjectHash getEndpointKey() {
        return endpointKey;
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
        EndpointClusterAddress other = (EndpointClusterAddress) obj;
        if (endpointKey == null) {
            if (other.endpointKey != null)
                return false;
        } else if (!endpointKey.equals(other.endpointKey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EndpointClusterAddress [endpointKey=" + endpointKey + ", getNodeId()=" + getNodeId() + ", getTenantId()="
                + getTenantId() + ", getAppToken()=" + getAppToken() + "]";
    }

}
