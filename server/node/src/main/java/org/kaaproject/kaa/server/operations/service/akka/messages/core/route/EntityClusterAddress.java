package org.kaaproject.kaa.server.operations.service.akka.messages.core.route;


public abstract class EntityClusterAddress {

    private final String nodeId;
    private final String tenantId;
    private final String appToken;

    public EntityClusterAddress(String nodeId, String tenantId, String appToken) {
        super();
        this.nodeId = nodeId;
        this.tenantId = tenantId;
        this.appToken = appToken;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getAppToken() {
        return appToken;
    }
    
    abstract public byte[] getEntityId();
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((appToken == null) ? 0 : appToken.hashCode());
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
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
        EntityClusterAddress other = (EntityClusterAddress) obj;
        if (appToken == null) {
            if (other.appToken != null)
                return false;
        } else if (!appToken.equals(other.appToken))
            return false;
        if (nodeId == null) {
            if (other.nodeId != null)
                return false;
        } else if (!nodeId.equals(other.nodeId))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "EntityClusterAddress [nodeId=" + nodeId + ", tenantId=" + tenantId + ", appToken=" + appToken + "]";
    }

}
