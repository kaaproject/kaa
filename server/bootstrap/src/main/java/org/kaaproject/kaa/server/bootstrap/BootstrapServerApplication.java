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

package org.kaaproject.kaa.server.bootstrap;

import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.kaaproject.kaa.server.common.AbstractServerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * The Class BootstrapServerApplication. Implements main() to start Bootstrap
 * Service.
 */
public class BootstrapServerApplication extends AbstractServerApplication {

    private static final String[] DEFAULT_APPLICATION_CONTEXT_XMLS = new String[] { "bootstrapContext.xml" };

    private static final String[] DEFAULT_APPLICATION_CONFIGURATION_FILES = new String[] { "bootstrap-server.properties" };

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapServerApplication.class);

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread th, Throwable ex) {
                LOG.error("Uncaught exception: ", ex);
            }
        });

        BootstrapServerApplication app = new BootstrapServerApplication(DEFAULT_APPLICATION_CONTEXT_XMLS,
                DEFAULT_APPLICATION_CONFIGURATION_FILES);
        app.startAndWait(args);
    }

    public BootstrapServerApplication(String[] defaultContextFiles, String[] defaultConfigurationFiles) {
        super(defaultContextFiles, defaultConfigurationFiles);
    }

    @Override
    protected String getName() {
        return "Bootstrap Server";
    }

    @Override
    protected void init(ApplicationContext context) {
        final BootstrapInitializationService bootstrapInitializationService = (BootstrapInitializationService) context
                .getBean("bootstrapInitializationService");
        bootstrapInitializationService.start();
    }
}
