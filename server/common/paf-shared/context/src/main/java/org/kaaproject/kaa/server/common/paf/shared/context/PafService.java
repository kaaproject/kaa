package org.kaaproject.kaa.server.common.paf.shared.context;

import java.util.Set;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.springframework.messaging.MessageChannel;

public interface PafService {

    ApplicationDto getApplicationByToken(String applicationToken);
    
    EndpointProfileDto getEndpointProfileById(EndpointId endpointId);
    
    void registerApplicationRoutes(Set<ApplicationRoute> applicationRoutes, MessageChannel requestChannel);
    
    void deregisterApplicationRoutes(Set<ApplicationRoute> applicationRoutes);
    
    MessageChannel findApplicationRequestChannel(ApplicationRoute applicationRoute);

    void registerApplicationProfileRoutes(Set<ApplicationProfileRoute> applicationProfileRoutes, MessageChannel requestChannel);
    
    void deregisterApplicationProfileRoutes(Set<ApplicationProfileRoute> applicationProfileRoutes);

    MessageChannel findApplicationProfileRequestChannel(ApplicationProfileRoute applicationProfileRoute);
    
    void sendMessage(InboundSessionMessage message);
    
    void sendRegistrationMessage(InboundSessionMessage message);
    
    void sendControlMessage(SessionControlMessage controlMessage);
    
}
