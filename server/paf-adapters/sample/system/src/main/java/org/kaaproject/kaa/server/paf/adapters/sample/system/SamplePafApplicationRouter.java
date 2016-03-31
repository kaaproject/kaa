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

import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationId;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.kaaproject.kaa.server.common.paf.shared.system.PafApplicationRouter;
import org.kaaproject.kaa.server.paf.adapters.sample.common.SampleApplicationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;


public class SamplePafApplicationRouter extends AbstractMappingMessageRouter implements PafApplicationRouter {

    private static final Logger LOG = LoggerFactory.getLogger(SamplePafApplicationRouter.class);
    
    @Autowired 
    private PafService pafService;
    
    @Override
    protected List<Object> getChannelKeys(Message<?> message) {
        
        String payload = (String) message.getPayload();
        LOG.info("Routing payload [{}]", payload);
        
        String[] data = payload.split("\\|");
        
        ApplicationId applicationId = new SampleApplicationId(data[1]);
        LOG.info("Detected appId [{}]", applicationId);
        
        MessageChannel channel = pafService.findApplicationRequestChannel(applicationId);
        LOG.info("Detected channel [{}]", channel);
        if (channel != null) {
            return Collections.singletonList(channel);
        } else {
            return Collections.emptyList();
        }
    }

}
