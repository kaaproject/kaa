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

package org.kaaproject.kaa.server.node;

import org.kaaproject.kaa.server.common.AbstractServerApplication;
import org.kaaproject.kaa.server.node.service.initialization.InitializationService;
import org.springframework.context.ApplicationContext;
import org.kaaproject.kaa.server.common.utils.KaaUncaughtExceptionHandler;


/**
 * Main class that is used to launch Operations Server.
 */
public class KaaNodeApplication extends AbstractServerApplication {

    private static final String[] DEFAULT_APPLICATION_CONTEXT_XMLS = new String[] { "kaaNodeContext.xml" };

    private static final String[] DEFAULT_APPLICATION_CONFIGURATION_FILES = new String[] {
            "kaa-node.properties", "sql-dao.properties", "nosql-dao.properties"};

    /**
     * The main method. Used to launch Kaa Node.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new KaaUncaughtExceptionHandler());

        KaaNodeApplication app = new KaaNodeApplication(DEFAULT_APPLICATION_CONTEXT_XMLS,
                DEFAULT_APPLICATION_CONFIGURATION_FILES);
        app.startAndWait(args);
    }

    public KaaNodeApplication(String[] defaultContextFiles, String[] defaultConfigurationFiles) {
        super(defaultContextFiles, defaultConfigurationFiles);
    }

    @Override
    protected String getName() {
        return "Kaa Node";
    }

    @Override
    protected void init(ApplicationContext ctx) {
        final InitializationService kaaNodeInitializationService = ctx.getBean("kaaNodeInitializationService", InitializationService.class);
        kaaNodeInitializationService.start();
    }
}
