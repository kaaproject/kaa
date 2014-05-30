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

package org.kaaproject.kaa.server.common.http.server;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * DefaultHandler Class. State in final processing chain in Netty Channel
 * receive HTTP request, decode URI, find processing command and push it to
 * executor thread for processing
 *
 */
public class DefaultHandler extends SimpleChannelInboundHandler<CommandProcessor> {

    private static final Logger LOG = LoggerFactory.getLogger(RequestDecoder.class);

    /**
     * Executor thread group.
     */
    private final EventExecutorGroup executor;

    /**
     * DefaultHandler class constructor.
     * @param executorGroup EventExecutorGroup
     */
    public DefaultHandler(final EventExecutorGroup executorGroup) {
        super(false);
        this.executor = executorGroup;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx,
            final CommandProcessor msg) throws Exception {
        Callable<CommandProcessor> callable = msg;

        final Future<CommandProcessor> future = executor.submit(callable);

        future.addListener(new GenericFutureListener<Future<Object>>() {
            @Override
            public void operationComplete(final Future<Object> future)
                    throws Exception {
                LOG.trace("DefaultHandler().operationComplete...");
                if (future.isSuccess()) {
                    ctx.writeAndFlush(future.get());
                } else {
                    ctx.fireExceptionCaught(future.cause());
                }
            }
        });
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx)
            throws Exception {
        super.channelInactive(ctx);
        // TODO cancel submitted tasks,
        // that works only for not in progress tasks
        // if (future != null && !future.isDone()) {
        // future.cancel(true);
        // }
    }

}
