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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationProfileRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.ApplicationRoute;
import org.kaaproject.kaa.server.common.paf.shared.context.EndpointId;
import org.kaaproject.kaa.server.common.paf.shared.context.InboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.OutboundSessionMessage;
import org.kaaproject.kaa.server.common.paf.shared.context.PafException;
import org.kaaproject.kaa.server.common.paf.shared.context.PafService;
import org.kaaproject.kaa.server.common.paf.shared.context.RegistrationResult;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionContext;
import org.kaaproject.kaa.server.common.paf.shared.context.SessionControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

@Service
public class DefaultPafService implements PafService, InitializingBean, ApplicationContextAware {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPafService.class);   
        
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private EndpointService endpointService;
    
    private ApplicationContext applicationContext;
    
    private Map<ApplicationRoute, MessageChannel> applicationChannels = new HashMap<>();
    
    private Map<ApplicationProfileRoute, MessageChannel> applicationProfileChannels = new HashMap<>();
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        
        startSampleProtocols();
        
    }
    
    private void startSampleProtocols() throws IOException {

        int startHttpPort = 9010;
        
        List<GenericApplicationContext> systemContexts = new ArrayList<>();
        List<GenericApplicationContext> appContexts = new ArrayList<>();
        List<GenericApplicationContext> appProfileContexts = new ArrayList<>();
        
        for (int i=0;i<3;i++) {
            systemContexts.add(createSystemChain("sys"+i, startHttpPort++));
        }
        
        for (int i=0;i<3;i++) {
            appContexts.add(createAppContext("app" + i));
        }   
        
        for (int i=0;i<2;i++) {
            appProfileContexts.add(createAppProfileContext("appProf" + i));
        }   
    }
    
    private GenericApplicationContext createSystemChain(String systemId, int httpPort) throws IOException {
        GenericApplicationContext systemPafContext = loadContext("paf/sample/system/context.xml", applicationContext);
        StandardEnvironment env = new StandardEnvironment();
        Properties props = new Properties();
        props.setProperty("systemId", systemId);
        props.setProperty("httpPort", ""+httpPort);
        PropertiesPropertySource pps = new PropertiesPropertySource("sysprops", props);
        env.getPropertySources().addLast(pps);
        systemPafContext.setEnvironment(env);
        systemPafContext.refresh();
        return systemPafContext;
    }
    
    private GenericApplicationContext createAppContext(String appId) throws IOException {
        GenericApplicationContext applicationPafContext = loadContext("paf/sample/application/context.xml", applicationContext);
        StandardEnvironment env = new StandardEnvironment();
        Properties props = new Properties();
        props.setProperty("applicationPath", appId);
        PropertiesPropertySource pps = new PropertiesPropertySource("appprops", props);
        env.getPropertySources().addLast(pps);
        applicationPafContext.setEnvironment(env);
        applicationPafContext.refresh();
        return applicationPafContext;
    }
    
    private GenericApplicationContext createAppProfileContext(String appProfileId) throws IOException {
        GenericApplicationContext applicationPafContext = loadContext("paf/sample/app_profile/context.xml", applicationContext);
        StandardEnvironment env = new StandardEnvironment();
        Properties props = new Properties();
        props.setProperty("applicationProfilePath", appProfileId);
        PropertiesPropertySource pps = new PropertiesPropertySource("appprops", props);
        env.getPropertySources().addLast(pps);
        applicationPafContext.setEnvironment(env);
        applicationPafContext.refresh();
        return applicationPafContext;
    }

    private GenericApplicationContext loadContext(String path, ApplicationContext parent) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        
        GenericApplicationContext createdContext = new GenericApplicationContext(parent);
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(createdContext);
        reader.loadBeanDefinitions(resource);
        createdContext.setParent(parent);
        
        return createdContext;
    }
    
    @Override
    public ApplicationDto getApplicationByToken(String applicationToken) {
        return applicationService.findAppByApplicationToken(applicationToken);
    }

    @Override
    public EndpointProfileDto getEndpointProfileById(EndpointId endpointId) {
        return endpointService.findEndpointProfileByKeyHash(endpointId.toBytes());
    }

    @Override
    public void sendMessage(InboundSessionMessage message) {
        SessionContext sessionContext = message.getSessionContext();
        LOG.info("Got inbound message with sessionId [{}], sessionType [{}]",
                sessionContext.getSessionId(),
                sessionContext.getSessionType());
        
        String replyPayload = 
                String.format("Message "
                        + " with sessionId [%s], type [%s], endpointId [%s] successfuly received and processed at %d.", 
                        sessionContext.getSessionId(), 
                        sessionContext.getSessionType(), 
                        sessionContext.getEndpointId(), 
                        System.currentTimeMillis());
        
        OutboundSessionMessage outboundMessage = new TestOutboundMessage(sessionContext, replyPayload.getBytes());
        sessionContext.onMessage(outboundMessage);
    }
    
    @Override
    public void sendRegistrationMessage(InboundSessionMessage message) {
        SessionContext sessionContext = message.getSessionContext();
        
        OutboundSessionMessage outboundMessage = 
                new TestOutboundMessage(sessionContext, RegistrationResult.OK);
        sessionContext.onMessage(outboundMessage);
    }
 

    @Override
    public void sendControlMessage(SessionControlMessage controlMessage) {
        // TODO Auto-generated method stub
        
    }
    
    class TestOutboundMessage implements OutboundSessionMessage {
        
        private SessionContext sessionContext;
        private byte[] replyPayload;
        private RegistrationResult registrationResult;
        
        TestOutboundMessage(SessionContext sessionContext, byte[] replyPayload) {
            this.sessionContext = sessionContext;
            this.replyPayload = replyPayload;
        }
        
        TestOutboundMessage(SessionContext sessionContext, RegistrationResult registrationResult) {
            this.sessionContext = sessionContext;
            this.registrationResult = registrationResult;
        }
 
        @Override
        public SessionContext getSessionContext() {
            return sessionContext;
        }

        @Override
        public byte[] replyPayload() {
            return replyPayload;
        }

        @Override
        public RegistrationResult getEndpointRegistrationResult() {
            return registrationResult;
        }
        
    }

    @Override
    public void registerApplicationRoutes(Set<ApplicationRoute> applicationRoutes, MessageChannel requestChannel) {
        for (ApplicationRoute route : applicationRoutes) {
            if (!applicationChannels.containsKey(route)) {
                applicationChannels.put(route, requestChannel);
            } else {
                throw new PafException(String.format("Application chain with route [%s] is already registered!", route));
            }
        }
    }

    @Override
    public void deregisterApplicationRoutes(Set<ApplicationRoute> applicationRoutes) {
        for (ApplicationRoute route : applicationRoutes) {
            applicationChannels.remove(route);
        }
    }
    
    @Override
    public void registerApplicationProfileRoutes(Set<ApplicationProfileRoute> applicationProfileRoutes,
            MessageChannel requestChannel) {
        for (ApplicationProfileRoute route : applicationProfileRoutes) {
            if (!applicationProfileChannels.containsKey(route)) {
                applicationProfileChannels.put(route, requestChannel);
            } else {
                throw new PafException(String.format("Application profile chain with route [%s] is already registered!", route));
            }
        }
    }

    @Override
    public void deregisterApplicationProfileRoutes(Set<ApplicationProfileRoute> applicationProfileRoutes) {
        for (ApplicationProfileRoute route : applicationProfileRoutes) {
            applicationProfileChannels.remove(route);
        }
    }

    @Override
    public MessageChannel findApplicationRequestChannel(ApplicationRoute applicationRoute) {
        return applicationChannels.get(applicationRoute);
    }

    @Override
    public MessageChannel findApplicationProfileRequestChannel(ApplicationProfileRoute applicationProfileRoute) {
        return applicationProfileChannels.get(applicationProfileRoute);
    }

}
