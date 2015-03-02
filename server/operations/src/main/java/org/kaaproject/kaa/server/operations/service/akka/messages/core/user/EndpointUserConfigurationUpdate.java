package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointUserConfigurationUpdate implements TenantAwareMessage {

    private final String tenantId;
    private final String userId;
    private final String applicationToken;
    private final EndpointObjectHash key;
    private final byte[] hash;

    public EndpointUserConfigurationUpdate(String tenantId, String userId, String applicationToken, EndpointObjectHash key, byte[] hash) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.applicationToken = applicationToken;
        this.key = key;
        this.hash = hash;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public EndpointObjectHash getKey() {
        return key;
    }

    public byte[] getHash() {
        return hash;
    }

    public static EndpointUserConfigurationUpdate fromThrift(
            org.kaaproject.kaa.server.common.thrift.gen.operations.EndpointUserConfigurationUpdate notification) {
        return new EndpointUserConfigurationUpdate(notification.getTenantId(), notification.getUserId(),
                notification.getApplicationToken(), EndpointObjectHash.fromBytes(notification.getEndpointKey()), notification.getUcfHash());
    }
}
