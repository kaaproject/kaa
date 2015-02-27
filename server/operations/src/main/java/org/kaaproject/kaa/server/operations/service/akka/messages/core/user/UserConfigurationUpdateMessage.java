package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;


public class UserConfigurationUpdateMessage implements GlobalUserAwareMessage{

    private final UserConfigurationUpdate update;
    
    public UserConfigurationUpdateMessage(UserConfigurationUpdate update) {
        super();
        this.update = update;
    }
    
    @Override
    public String getTenantId() {
        return update.getTenantId();
    }

    @Override
    public String getUserId() {
        return update.getUserId();
    }

    public UserConfigurationUpdate getUpdate() {
        return update;
    }
}
