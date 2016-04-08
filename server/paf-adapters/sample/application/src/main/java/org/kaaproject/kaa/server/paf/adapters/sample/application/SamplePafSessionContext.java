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

package org.kaaproject.kaa.server.paf.adapters.sample.application;

import org.kaaproject.kaa.server.common.paf.shared.common.context.AbstractPafSessionContext;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessageBuilder;
import org.kaaproject.kaa.server.common.paf.shared.context.OutboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionControlMessage;
import org.springframework.messaging.MessageChannel;

public class SamplePafSessionContext extends AbstractPafSessionContext {

    public SamplePafSessionContext(PafMessage message, MessageChannel replyChannel) {
        super(message, replyChannel);
    }

    @Override
    protected PafMessage processOutboundMessage(OutboundSessionMessage outboundMessage) {
        if (outboundMessage.getEndpointRegistrationResult() != null) {
            PafMessage message = PafMessageBuilder.createMessage(
                    sourceMessage.getPayload(), 
                    sourceMessage.getHeaders(),
                    sourceMessage.getMetaData());
            message.setRegistrationResult(outboundMessage.getEndpointRegistrationResult());
            return message;
        } else {
            return PafMessageBuilder.createMessage(
                    outboundMessage.replyPayload(), 
                    sourceMessage.getHeaders(),
                    sourceMessage.getMetaData());
        }
    }

    @Override
    protected PafMessage processControlMessage(SessionControlMessage controlMessage) {
        // TODO Auto-generated method stub
        return null;
    }

}
