/*
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

package org.kaaproject.kaa.server.control.service.admin;

import java.util.EventListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.kaaproject.kaa.server.node.service.initialization.AbstractInitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminInitializationService extends AbstractInitializationService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInitializationService.class);
    
    @Autowired
    private AdminContextLoaderListener adminContextLoaderListener;
    
    @Value("#{properties[admin_port]}")
    private int adminPort;
    
    private Server server;
    private WebAppContext webAppContext;
    
    @Override
    public void start() {
        LOG.info("Starting Kaa Admin Web Server...");
        
        server = new Server(adminPort);
        webAppContext = new WebAppContext();
        webAppContext.setEventListeners(new EventListener[]{adminContextLoaderListener});
        webAppContext.setContextPath("/");
        String webXmlLocation = AdminInitializationService.class.getResource("/admin-web/WEB-INF/web.xml").toString();
        webAppContext.setDescriptor(webXmlLocation);
        String resLocation = AdminInitializationService.class.getResource("/admin-web").toString();
        webAppContext.setResourceBase(resLocation);
        webAppContext.setParentLoaderPriority(true);
        
        server.setHandler(webAppContext);
        
        try {
            server.start();
            LOG.info("Kaa Admin Web Server started.");
        } catch (Exception e) {
            LOG.error("Error starting Kaa Admin Web Server!", e);
        }
    }

    @Override
    public void stop() {
        try {
            LOG.info("Stopping Kaa Admin Web Server...");
            server.stop();
            webAppContext.destroy();
            LOG.info("Kaa Admin Web Server stopped.");
        } catch (Exception e) {
            LOG.error("Error stopping Kaa Admin Web Server!", e);
        }
    }

}
