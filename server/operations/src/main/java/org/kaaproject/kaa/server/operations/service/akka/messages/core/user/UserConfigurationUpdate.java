package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

public class UserConfigurationUpdate {

    private final String tenantId;
    private final String userId;
    private final String applicationToken;
    private final int schemaVersion;
    private final byte[] hash;

    public UserConfigurationUpdate(String tenantId, String userId, String applicationToken, int schemaVersion, byte[] hash) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.applicationToken = applicationToken;
        this.schemaVersion = schemaVersion;
        this.hash = hash;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public byte[] getHash() {
        return hash;
    }

    public static UserConfigurationUpdate fromThrift(
            org.kaaproject.kaa.server.common.thrift.gen.operations.UserConfigurationUpdate notification) {
        return new UserConfigurationUpdate(notification.getTenantId(), notification.getUserId(), notification.getApplicationToken(),
                notification.getCfSchemaVersion(), notification.getUcfHash());
    }
}
