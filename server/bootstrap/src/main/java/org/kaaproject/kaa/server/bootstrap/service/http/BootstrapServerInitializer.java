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

package org.kaaproject.kaa.server.bootstrap.service.http;

import io.netty.util.concurrent.EventExecutorGroup;

import java.lang.reflect.InvocationTargetException;

import org.kaaproject.kaa.server.common.http.server.CommandFactory;
import org.kaaproject.kaa.server.common.http.server.DefaultServerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class BootstrapServerInitializer.
 */
public class BootstrapServerInitializer extends DefaultServerInitializer {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultServerInitializer.class);

    /**
     * Instantiates a new bootstrap server initializer.
     */
    public BootstrapServerInitializer() {
        super();
        LOG.info("Initializing Bootstrap server...");
    }

    /**
     * Instantiates a new bootstrap server initializer.
     *
     * @param conf the conf
     * @param executor the executor
     */
    public BootstrapServerInitializer(BootstrapConfig conf, EventExecutorGroup executor) {
        super(conf, executor);
        LOG.info("Initializing Bootstrap server...");
        try {
            init();
        } catch (Exception e) {
            LOG.error("Error initializing Bootstrap server", e);
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.DefaultServerInitializer#init()
     */
    @Override
    public void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (String commandClass : ((BootstrapConfig) getConf()).getCommandList()) {
            CommandFactory.addCommandClass(commandClass);
        }
    }

}
