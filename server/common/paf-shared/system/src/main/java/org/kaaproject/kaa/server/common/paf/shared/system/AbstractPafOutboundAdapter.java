package org.kaaproject.kaa.server.common.paf.shared.system;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafBean;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

public abstract class AbstractPafOutboundAdapter extends AbstractPafBean implements PafOutboundAdapter {
    
    @Override
    public Message<byte[]> transform(PafMessage message) {
        byte[] payload = createPayload(message);
        MessageHeaders headers = createHeaders(message);        
        Message<byte[]> resultMessage = MessageBuilder.createMessage(payload, headers);        
        return resultMessage;
    }
    
    protected byte[] createPayload(PafMessage message) {
        return message.getPayload();
    }
    
    protected MessageHeaders createHeaders(PafMessage message) {
        return message.getHeaders();
    }
    
}
