package org.kaaproject.kaa.server.common.paf.shared.common;

import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public abstract class AbstractPafChannelBean extends AbstractPafBean implements SubscribableChannel {

    private DirectChannel nestedChannel;
    
    public AbstractPafChannelBean() {
        nestedChannel = new DirectChannel();
    }
    
    @Override
    public boolean send(Message<?> message) {
        return nestedChannel.send(message);
    }

    @Override
    public boolean send(Message<?> message, long timeout) {
        return nestedChannel.send(message, timeout);
    }

    @Override
    public boolean subscribe(MessageHandler handler) {
        return nestedChannel.subscribe(handler);
    }

    @Override
    public boolean unsubscribe(MessageHandler handler) {
        return nestedChannel.unsubscribe(handler);
    }
}
