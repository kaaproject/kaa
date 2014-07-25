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

package org.kaaproject.kaa.server.operations.service.http;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.kaaproject.kaa.server.common.http.server.CommandFactory;
import org.kaaproject.kaa.server.common.http.server.CommandProcessor;
import org.kaaproject.kaa.server.common.http.server.DefaultServerInitializer;
import org.kaaproject.kaa.server.operations.service.config.NettyHttpServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.http.handler.AkkaHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class OperationsServerInitializer.
 */
public class OperationsServerInitializer extends DefaultServerInitializer {

    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultServerInitializer.class);

    /**
     * Instantiates a new end point server initializer.
     */
    public OperationsServerInitializer() {
        super();
        LOG.info("Operations Server Initializer ...");
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.http.server.DefaultServerInitializer
     * #init()
     */
    @Override
    public void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LOG.info("Operations Server Initializer Init() started: ");
        for (String commandClass : ((NettyHttpServiceChannelConfig) getConf()).getCommandList()) {
            CommandFactory.addCommandClass(commandClass);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.DefaultServerInitializer#getMainHandler(java.util.UUID)
     */
    @Override
    protected SimpleChannelInboundHandler<CommandProcessor> getMainHandler(UUID uuid){
    	return new AkkaHandler(uuid, ((NettyHttpServiceChannelConfig) getConf()).getOperationServerConfig().getAkkaService(), new DefaultEventExecutorGroup(1));
    }

}
