package net.paf;

import org.springframework.messaging.MessageChannel;

public interface ApplicationRouter {

    void registerAppChannel(String appId, MessageChannel channel);
    
    void deregisterAppChannel(String appId);
    
}
