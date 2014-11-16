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

public abstract class AbstractServerApplication {

    private static final String XML = ".xml";
    private static final String PROPERTIES = ".properties";

    private static final Logger LOG = LoggerFactory
            .getLogger(AbstractServerApplication.class);
    
    private String[] defaultContextFiles;
    private String[] defaultConfigurationFiles;
    
    public AbstractServerApplication(String[] defaultContextFiles, String[] defaultConfigurationFiles) {
        super();
        this.defaultContextFiles = defaultContextFiles;
        this.defaultConfigurationFiles = defaultConfigurationFiles;
    }

    public void startAndWait(String[] args){
        LOG.info("{} application starting...", getName());
        Environment.logState();

        String[] appContextXmls = defaultContextFiles;
        String[] appPropertiesFiles = defaultConfigurationFiles;
        if (args.length > 0) {
            List<String> contexts = new ArrayList<>();
            List<String> properties = new ArrayList<>();
            for(String arg : args){
                if(arg.endsWith(XML) || arg.endsWith(XML.toUpperCase())){
                    contexts.add(arg);
                }else if (arg.endsWith(PROPERTIES) || arg.endsWith(PROPERTIES.toUpperCase())){
                    properties.add(arg);
                }
            }
            if(contexts.size() > 0){
                appContextXmls = contexts.toArray(new String[contexts.size()]);
                appPropertiesFiles = properties.toArray(new String[contexts.size()]);
            }
        }

        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(appContextXmls, false);
        try{
            MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
            for(String propertyFile : appPropertiesFiles){
                try {
                    sources.addLast(new ResourcePropertySource(propertyFile, AbstractServerApplication.class.getClassLoader()));
                } catch (IOException e) {
                    LOG.error("Can't load properties file {} from classpath", propertyFile);
                    return;
                }
            }
            ctx.refresh();
            init(ctx);
        }finally{
            ctx.close();
        }

        LOG.info("{} application stopped.", getName());
    };

    protected abstract String getName();
    protected abstract void init(ApplicationContext context);

}
