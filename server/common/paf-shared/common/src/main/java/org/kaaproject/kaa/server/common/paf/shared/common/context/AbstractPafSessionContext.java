/**
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.common.paf.shared.common.context;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;
import org.kaaproject.kaa.server.common.paf.shared.context.OutboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionContext;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionControlMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;
import org.springframework.messaging.MessageChannel;

public abstract class AbstractPafSessionContext implements SessionContext {

    protected final PafMessage sourceMessage;
    protected final MessageChannel replyChannel;

    public AbstractPafSessionContext(PafMessage sourceMessage, MessageChannel replyChannel) {
        super();
        this.sourceMessage = sourceMessage;
        this.replyChannel = replyChannel;
    }

    @Override
    public SessionId getSessionId() {
        return sourceMessage.getSessionId();
    }

    @Override
    public SessionType getSessionType() {
        return sourceMessage.getSessionType();
    }
    
    @Override
    public EndpointId getEndpointId() {
        return sourceMessage.getEndpointId();
    }

    @Override
    public void onMessage(OutboundSessionMessage message) {
        PafMessage replyMessage =  processOutboundMessage(message);
        replyChannel.send(replyMessage);
    }

    @Override
    public void onControlMessage(SessionControlMessage controlMessage) {
        PafMessage replyControlMessage =  processControlMessage(controlMessage);
        replyChannel.send(replyControlMessage);
    }
    
    protected abstract PafMessage processOutboundMessage(OutboundSessionMessage outboundMessage); 
    
    protected abstract PafMessage processControlMessage(SessionControlMessage controlMessage); 
    
}
