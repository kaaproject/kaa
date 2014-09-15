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
package org.kaaproject.kaa.server.bootstrap.service.tcp;

import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.UUID;

import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.tcp.handler.BootstrapKaaTcpHandler;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpCommandProcessor;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpServerInitializer;

/**
 * KaaTcpServerInitializer Class.
 * Initialize KaaTcp bootstrap processing netty chain.
 * @author Andrey Panasenko
 *
 */
public class KaaTcpServerInitializer extends AbstractKaaTcpServerInitializer {
    
    private EventExecutorGroup executor;
    
    private OperationsServerListService opListService;
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpServerInitializer#getMainHandler(java.util.UUID)
     */
    @Override
    protected SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> getMainHandler(UUID uuid) {
        
        return new BootstrapKaaTcpHandler(uuid, executor);
    }

    /**
     * KaaTcp command processing executor getter.
     * @return EventExecutorGroup the executor 
     */
    public EventExecutorGroup getExecutor() {
        return executor;
    }

    /**
     * KaaTcp command processing executor setter.
     * @param executor EventExecutorGroup
     */
    public void setExecutor(EventExecutorGroup executor) {
        this.executor = executor;
    }

    /**
     * OperationsServerListService getter.
     * @return OperationsServerListService the Operations Server List gathering service
     */
    public OperationsServerListService getOperatonsServerListService() {
        return opListService;
    }

    /**
     * OperationsServerListService setter.
     * @param opListService OperationsServerListService
     */
    public void setOperatonsServerListService(OperationsServerListService opListService) {
        this.opListService = opListService;
    }

}
