/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.paf.adapters.sample.system;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

public final class SampleWebContextLoader extends ContextLoader implements ApplicationContextAware, ServletContextListener {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns the parent application context as set by the
     * {@link ApplicationContextAware} interface.
     * 
     * @return The initial ApplicationContext that loads the Jetty server.
     */
    @Override
    protected ApplicationContext loadParentContext(ServletContext servletContext) {
        return this.applicationContext;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.initWebApplicationContext(sce.getServletContext());
    }
    
    @Override
    protected WebApplicationContext createWebApplicationContext(ServletContext sc) {
        ConfigurableWebApplicationContext cwac = new XmlWebApplicationContext() {
            
            @Override
            protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
            }
            
        };
        
        return cwac;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //not needed
    }
}
