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

package org.kaaproject.kaa.server.control;

import org.kaaproject.kaa.server.common.AbstractServerApplication;
import org.kaaproject.kaa.server.control.service.ControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * The Class ControlServerApplication.
 */
public class ControlServerApplication extends AbstractServerApplication {

    private static final String[] DEFAULT_APPLICATION_CONTEXT_XMLS = new String[] { "controlContext.xml" };

    private static final String[] DEFAULT_APPLICATION_CONFIGURATION_FILES = new String[] {
            "control-server.properties", "dao.properties" };

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlServerApplication.class);

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

        ControlServerApplication app = new ControlServerApplication(DEFAULT_APPLICATION_CONTEXT_XMLS, DEFAULT_APPLICATION_CONFIGURATION_FILES);
        app.startAndWait(args);
    }

    public ControlServerApplication(String[] defaultContextFiles, String[] defaultConfigurationFiles) {
        super(defaultContextFiles, defaultConfigurationFiles);
    }

    @Override
    protected String getName() {
        return "Control Server";
    }

    @Override
    protected void init(ApplicationContext context) {
        final ControlService controlService = context.getBean("controlService", ControlService.class);
        controlService.start();
    }
}
