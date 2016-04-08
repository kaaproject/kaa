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

import java.util.EventListener;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.kaaproject.kaa.server.common.paf.shared.system.AbstractPafInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

public class SampleHttpPafInterface extends AbstractPafInterface {
    
    private static final Logger LOG = LoggerFactory.getLogger(SampleHttpPafInterface.class);

    private String sysId;
    private int httpPort;
    
    private SampleWebContextLoader contextLoader;
    
    private Server server;
    private ServletContextHandler webAppContext;
    
    public void setSysId(String sysId) {
        this.sysId = sysId;
    }
 
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }
    
    public void setContextLoader(SampleWebContextLoader contextLoader) {
        this.contextLoader = contextLoader;
    }

    @Override
    protected void onInit() throws Exception {
        LOG.info("System PAF [{}] Loaded dynamically!", sysId);
        LOG.info("[{}] Http port: {}", sysId, httpPort);
        
        server = new Server(httpPort);
        webAppContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
        webAppContext.setEventListeners(new EventListener[]{contextLoader});
        webAppContext.setContextPath("/");
        
        ServletHolder holder = new ServletHolder("dispatcher", DispatcherServlet.class);
        holder.setInitParameter("contextConfigLocation", "");
        holder.setInitOrder(1);
        webAppContext.addServlet(holder, "/*");
        
        server.setHandler(webAppContext);
    } 
    
    @Override
    protected void doStart() {
        try {
            server.start();
            LOG.info("HTTP server started on port {}", httpPort);
        } catch (Exception e) {
            LOG.error("Error starting HTTP Server!", e);
        }
    }

    @Override
    protected void doStop() {
        try {
            LOG.info("Stopping HTTP Server...");
            server.stop();
            webAppContext.destroy();
            LOG.info("HTTP Server stopped.");
        } catch (Exception e) {
            LOG.error("Error stopping HTTP Server!", e);
        }
    }

}
