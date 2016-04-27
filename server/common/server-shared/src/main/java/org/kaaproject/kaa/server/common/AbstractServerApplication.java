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

package org.kaaproject.kaa.server.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

/**
 * The Class AbstractServerApplication provides implementation for common server
 * bootstrap scenario. This class is responsible for {@link ApplicationContext}
 * initialization. It is also responsible for populating
 * {@link org.springframework.core.env.Environment} with values from properties
 * files.
 */
public abstract class AbstractServerApplication {

    /** The Constant XML. */
    private static final String XML = ".xml";

    /** The Constant PROPERTIES. */
    private static final String PROPERTIES = ".properties";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServerApplication.class);

    /** The default context files. */
    private String[] defaultContextFiles;

    /** The default configuration files. */
    private String[] defaultConfigurationFiles;

    /**
     * Instantiates a new abstract server application.
     * 
     * @param defaultContextFiles
     *            the default context files to use
     * @param defaultConfigurationFiles
     *            the default configuration files to use
     */
    public AbstractServerApplication(String[] defaultContextFiles, String[] defaultConfigurationFiles) {
        super();
        this.defaultContextFiles = defaultContextFiles;
        this.defaultConfigurationFiles = defaultConfigurationFiles;
    }

    /**
     * Initialize {@link ApplicationContext} and populates 
     * {@link org.springframework.core.env.Environment} with values from properties files
     * 
     * @param args
     *            arguments that overwrite default configuration and properties files
     */
    public void startAndWait(String[] args) {
        LOG.info("{} application starting...", getName());
        Environment.logState();

        String[] appContextXmls = defaultContextFiles;
        String[] appPropertiesFiles = defaultConfigurationFiles;
        if (args.length > 0) {
            List<String> contexts = new ArrayList<>();
            List<String> properties = new ArrayList<>();
            for (String arg : args) {
                if (arg.endsWith(XML) || arg.endsWith(XML.toUpperCase())) {
                    contexts.add(arg);
                } else if (arg.endsWith(PROPERTIES) || arg.endsWith(PROPERTIES.toUpperCase())) {
                    properties.add(arg);
                }
            }
            if (!contexts.isEmpty()) {
                appContextXmls = contexts.toArray(new String[contexts.size()]);
            }
            if (!properties.isEmpty()) {
                appPropertiesFiles = properties.toArray(new String[properties.size()]);
            }
        }

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(appContextXmls, false);
        try {
            MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
            for (String propertyFile : appPropertiesFiles) {
                try {
                    sources.addLast(new ResourcePropertySource(propertyFile, AbstractServerApplication.class
                            .getClassLoader()));
                } catch (IOException e) {
                    LOG.error("Can't load properties file {} from classpath, exception catched {}", propertyFile, e);
                    return;
                }
            }
            ctx.refresh();
            init(ctx);
        } catch(Exception e){
            LOG.info("Error during initialization of context", e);
            throw e;
        }finally {
            ctx.close();
        }

        LOG.info("{} application stopped.", getName());
    };

    /**
     * Gets the name of the service.
     * 
     * @return the name
     */
    protected abstract String getName();

    /**
     * Inits custom server components based on already initialized {@link ApplicationContext}.
     * 
     * @param context
     *            the context
     */
    protected abstract void init(ApplicationContext context);

}
