package net.paf.system;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import net.paf.ApplicationRouter;


public class ApplicationRouterImpl extends AbstractMappingMessageRouter implements ApplicationRouter {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationRouterImpl.class);
    
    private Map<String, MessageChannel> channelMap = new HashMap<>();
    
    @Override
    protected List<Object> getChannelKeys(Message<?> message) {
        
        String payload = (String) message.getPayload();
        LOG.info("Routing payload [{}]", payload);
        
        String[] data = payload.split("\\|");
        
        String appId = data[0];
        MessageChannel channel = channelMap.get(appId);
        
        LOG.info("Detected appId [{}]", appId);
        LOG.info("Routing to channel [{}]", channel);
        
        return Collections.singletonList(channel);
    }

    @Override
    public void registerAppChannel(String appId, MessageChannel channel) {
        channelMap.put(appId, channel);
        
    }

    @Override
    public void deregisterAppChannel(String appId) {
        channelMap.remove(appId);
    }
     
}
