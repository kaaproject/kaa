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
package org.kaaproject.kaa.server.bootstrap.service.tcp.handler;

import java.util.UUID;
import java.util.concurrent.Callable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BootstrapKaaTcpHandler Class.
 * Handle KaaTcp processor in Netty chain. 
 * @author Andrey Panasenko
 *
 */
public class BootstrapKaaTcpHandler extends SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> {
    
    /** Constant logger */
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapKaaTcpHandler.class);
    
    /** Session UUID */
    private UUID uuid;
    
    /** Thread KaaTcp processor job executor */
    private EventExecutorGroup executor;
    
    /**
     * Default constructor.
     * @param  uuid - UUID session uuid.
     * @param  executor EventExecutorGroup Thread KaaTcp processor job executor
     */
    public BootstrapKaaTcpHandler(UUID uuid, EventExecutorGroup executor) {
        this.uuid = uuid;
        this.executor = executor;
        LOG.info("KaaTcp handler initialized for session {}",uuid.toString());
    }

    /* (non-Javadoc)
     * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)
     */
    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final AbstractKaaTcpCommandProcessor arg1) throws Exception {
        LOG.info("KaaTcp handler session {}, got command {}",uuid.toString(), arg1.getName());
        KaaTcpCommand command = (KaaTcpCommand) arg1;
        Callable<KaaTcpCommand> callable = (Callable<KaaTcpCommand>) command;
        
        final Future<KaaTcpCommand> future = executor.submit(callable);
        
        future.addListener(new GenericFutureListener<Future<KaaTcpCommand>>() {

            @Override
            public void operationComplete(Future<KaaTcpCommand> arg0) throws Exception {
                // TODO Auto-generated method stub
                LOG.trace("BootstrapKaaTcpHandler().operationComplete...");
                if (arg0.isSuccess()) {
                    ctx.writeAndFlush(arg0.get().getResponse());
                } else {
                    ctx.fireExceptionCaught(arg0.cause());
                }
            }
        });
    }

}
