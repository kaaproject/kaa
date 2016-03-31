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

package org.kaaproject.kaa.server.operations.service.paf;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationId;
import org.kaaproject.kaa.server.common.paf.shared.context.InboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.OutboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.PafException;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionContext;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class DefaultPafService implements PafService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPafService.class);
    
    @Autowired
    private ApplicationService applicationService;
    
    private Map<ApplicationId, MessageChannel> applicationChannels = new HashMap<>();

    @Override
    public ApplicationDto getApplicationByToken(String applicationToken) {
        return applicationService.findAppByApplicationToken(applicationToken);
    }

    @Override
    public void sendMessage(InboundSessionMessage message) {
        SessionContext sessionContext = message.getSessionContext();
        LOG.info("Got inbound message with sessionId [{}], sessionType [{}]",
                sessionContext.getSessionId(),
                sessionContext.getSessionType());
        
        String replyPayload = 
                String.format("Message "
                        + " with sessionId [%s] and type [%s] successfuly received and processed at %d.", 
                        sessionContext.getSessionId(), sessionContext.getSessionType(), System.currentTimeMillis());
        
        OutboundSessionMessage outboundMessage = new TestOutboundMessage(sessionContext, replyPayload);
        sessionContext.onMessage(outboundMessage);
    }

    @Override
    public void sendControlMessage(SessionControlMessage controlMessage) {
        // TODO Auto-generated method stub
        
    }
    
    class TestOutboundMessage implements OutboundSessionMessage {
        
        private SessionContext sessionContext;
        private String replyPayload;
        
        TestOutboundMessage(SessionContext sessionContext, String replyPayload) {
            this.sessionContext = sessionContext;
            this.replyPayload = replyPayload;
        }
 
        @Override
        public SessionContext getSessionContext() {
            return sessionContext;
        }

        @Override
        public String replyPayload() {
            return replyPayload;
        }
        
    }

    @Override
    public void registerApplicationChain(ApplicationId applicationId, MessageChannel requestChannel) {
        if (!applicationChannels.containsKey(applicationId)) {
            applicationChannels.put(applicationId, requestChannel);
        } else {
            throw new PafException(String.format("Application chain with id [%s] already registerd!", applicationId));
        }
    }

    @Override
    public void deregisterApplicationChain(ApplicationId applicationId) {
        applicationChannels.remove(applicationId);
    }

    @Override
    public MessageChannel findApplicationRequestChannel(ApplicationId applicationId) {
        return applicationChannels.get(applicationId);
    }
    
}
