package org.kaaproject.kaa.server.common.paf.shared.system;

import org.kaaproject.kaa.server.common.paf.shared.common.AbstractPafBean;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;

public abstract class AbstractPafInboundAdapter extends AbstractPafBean implements PafInboundAdapter {

    private MessageChannel systemReplyChannel;
    
    @Override
    protected void onInit() throws Exception {
        systemReplyChannel = getApplicationContext().getBean("systemReplyChannel", MessageChannel.class);
    }

    @Override
    public PafMessage transform(Message<byte[]> message) {
        PafMessage resultMessage = 
                PafMessageBuilder
                .fromMessage(message).build();
        resultMessage.setSystemLevelReplyChannel(systemReplyChannel);
        return transformImpl(resultMessage);
    }
    
    protected abstract PafMessage transformImpl(PafMessage message);
    
}
