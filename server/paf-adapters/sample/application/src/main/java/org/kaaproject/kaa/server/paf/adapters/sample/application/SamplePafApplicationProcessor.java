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

import org.kaaproject.kaa.server.common.paf.shared.application.PafApplicationProcessor;
import org.kaaproject.kaa.server.common.paf.shared.common.context.PafInboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.common.data.PafMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationId;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.kaaproject.kaa.server.paf.adapters.sample.common.SampleApplicationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.MessageChannel;

public class SamplePafApplicationProcessor implements PafApplicationProcessor<String>, InitializingBean, DisposableBean, ApplicationContextAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(SamplePafApplicationProcessor.class);

    private ApplicationId applicationId;

    private ApplicationContext appContext;

    @Autowired 
    private PafService pafService;
    
    @Override
    public void setApplicationId(String applicationId) {
        this.applicationId = new SampleApplicationId(applicationId);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("Application PAF [{}] Loaded dynamically!", applicationId);
        MessageChannel requestChannel = this.appContext.getBean("applicationRequestChannel", MessageChannel.class);
        pafService.registerApplicationChain(applicationId, requestChannel);
    }
    
    @Override
    public void onMessage(PafMessage<String> kaaPafMessage) {
        LOG.info("[{}] Received message with payload [{}]", applicationId, kaaPafMessage.getPayload());
        SamplePafSessionContext context = new SamplePafSessionContext(kaaPafMessage, 
                kaaPafMessage.getSystemLevelReplyChannel());
        pafService.sendMessage(new PafInboundSessionMessage(context));
    }

    @Override
    public void destroy() throws Exception {
        pafService.deregisterApplicationChain(applicationId);
    }

}
