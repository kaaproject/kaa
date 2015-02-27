package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;


public class UserConfigurationUpdate implements GlobalUserAwareMessage{

    private final String tenantId;
    private final String userId;
    private final String applicationToken;
    private final int schemaVersion;
    
    public UserConfigurationUpdate(String tenantId, String userId, String applicationToken, int schemaVersion) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.applicationToken = applicationToken;
        this.schemaVersion = schemaVersion;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public String getApplicationToken() {
        return applicationToken;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }
}
