package org.kaaproject.kaa.server.common.paf.shared.context;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.springframework.messaging.MessageChannel;

public interface PafService {

    ApplicationDto getApplicationByToken(String applicationToken);
    
    void registerApplicationChain(ApplicationId applicationId, MessageChannel requestChannel);
    
    void deregisterApplicationChain(ApplicationId applicationId);
    
    MessageChannel findApplicationRequestChannel(ApplicationId applicationId);
    
    void sendMessage(InboundSessionMessage message);
    
    void sendControlMessage(SessionControlMessage controlMessage);
    
}
