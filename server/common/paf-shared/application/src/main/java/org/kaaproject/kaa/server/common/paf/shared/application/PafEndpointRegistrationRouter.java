package org.kaaproject.kaa.server.common.paf.shared.application;

import org.springframework.messaging.MessageChannel;

public interface PafEndpointRegistrationRouter {

    void setRegistrationChannel(MessageChannel registrationChannel);
    
    void setExtensionChannel(MessageChannel extensionChannel);
    
}
