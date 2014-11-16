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

import org.kaaproject.kaa.server.common.AbstractServerApplication;
import org.kaaproject.kaa.server.operations.service.bootstrap.OperationsBootstrapService;
import org.springframework.context.ApplicationContext;

/**
 * Main class that is used to launch Operations Server.
 */
public class OperationsServerApplication extends AbstractServerApplication {

    private static final String[] DEFAULT_APPLICATION_CONTEXT_XML = new String[] { "operationsContext.xml" };

    private static final String[] DEFAULT_APPLICATION_CONFIGURATION_FILES = new String[] {
            "operations-server.properties", "dao.properties" };

    /**
     * The main method. Used to launch Operations Server.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        OperationsServerApplication app = new OperationsServerApplication(DEFAULT_APPLICATION_CONTEXT_XML,
                DEFAULT_APPLICATION_CONFIGURATION_FILES);
        app.startAndWait(args);
    }

    public OperationsServerApplication(String[] defaultContextFiles, String[] defaultConfigurationFiles) {
        super(defaultContextFiles, defaultConfigurationFiles);
    }

    @Override
    protected String getName() {
        return "Operations Server";
    }

    @Override
    protected void init(ApplicationContext ctx) {
        final OperationsBootstrapService operationsService = (OperationsBootstrapService) ctx
                .getBean("operationsBootstrapService");
        operationsService.start();
    }
}
