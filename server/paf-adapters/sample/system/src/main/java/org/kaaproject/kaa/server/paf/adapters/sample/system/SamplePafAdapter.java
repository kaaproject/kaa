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

package org.kaaproject.kaa.server.paf.adapters.sample.system;

import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessageBuilder;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionId;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionType;
import org.kaaproject.kaa.server.common.paf.shared.system.PafAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class SamplePafAdapter implements PafAdapter<String>, InitializingBean, ApplicationContextAware {

    private ApplicationContext appContext;
    
    private MessageChannel applicationReplyChannel;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        
        applicationReplyChannel = this.appContext.getBean("applicationReplyChannel", MessageChannel.class);
        
    }

    @Override
    public PafMessage<String> onMessage(Message<String> message) {
        String payload = message.getPayload();
        String[] data = payload.split("\\|");
        SessionId sessionId = new SampleHttpSessionId(data[0]); 
        
        PafMessage<String> resultMessage = 
                PafMessageBuilder
                .fromMessage(message).build();
        
        resultMessage.setSessionId(sessionId);
        resultMessage.setSessionType(SessionType.SYNC);
        resultMessage.setSystemLevelReplyChannel(applicationReplyChannel);
        return resultMessage;
    }

}
