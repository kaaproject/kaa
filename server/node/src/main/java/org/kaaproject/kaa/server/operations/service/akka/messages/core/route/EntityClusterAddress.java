package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;

public abstract class EntityClusterAddress extends EntityAddress {

    private final String nodeId;

    public EntityClusterAddress(String nodeId, String tenantId, String appToken) {
        super(tenantId, appToken);
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
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
        EntityClusterAddress other = (EntityClusterAddress) obj;
        if (nodeId == null) {
            if (other.nodeId != null)
                return false;
        } else if (!nodeId.equals(other.nodeId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityClusterAddress [nodeId=" + nodeId + ", tenantId=" + getTenantId() + ", appToken=" + getAppToken() + "]";
    }

}
