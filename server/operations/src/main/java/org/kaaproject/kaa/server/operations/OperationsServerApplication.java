/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.kaaproject.kaa.server.common.Environment;
import org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * Main class that is used to launch Operations Server.
 */
public class OperationsServerApplication {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(OperationsServerApplication.class);

    private static final String DEFAULT_APPLICATION_CONTEXT_XML = "operationsContext.xml";

    private static final List<String> DEFAULT_APPLICATION_CONFIGURATION_FILES = Arrays.asList("operations-server.properties", "dao.properties");
    
    /**
     * The main method. Used to launch Operations Server.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        LOG.info("Operations Server application starting...");
        Environment.logState();

        String applicationContextXml = DEFAULT_APPLICATION_CONTEXT_XML;
        List<String> applicationPropertiesFiles = DEFAULT_APPLICATION_CONFIGURATION_FILES;
        if (args.length > 0) {
            applicationContextXml = args[0];
            if(args.length > 1){
                applicationPropertiesFiles = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));
            }
        }

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(new String[]{applicationContextXml}, false);

        MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
        for(String propertyFile : applicationPropertiesFiles){
            try {
                sources.addLast(new ResourcePropertySource(propertyFile, OperationsServerApplication.class.getClassLoader()));
            } catch (IOException e) {
                LOG.error("Can't load properties file {} from classpath", propertyFile);
                return;
            }
        }
        ctx.refresh();
        
        final OperationsBootstrapService operationsService = (OperationsBootstrapService) ctx
                .getBean("operationsBootstrapService");

        operationsService.start();
        ctx.close();
        
        LOG.info("Operations Server Application stopped.");
    }
}
