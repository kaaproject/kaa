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

package org.kaaproject.kaa.server.common.server;

import io.netty.channel.ChannelHandlerContext;

import org.kaaproject.kaa.server.transport.channel.ChannelContext;

/**
 * Implementation of {@link ChannelContext} that is based on Netty channels.
 * 
 * @author Andrew Shvayka
 *
 */
public class NettyChannelContext implements ChannelContext {
    private final ChannelHandlerContext ctx;

    public NettyChannelContext(ChannelHandlerContext ctx) {
        super();
        this.ctx = ctx;
    }

    @Override
    public void writeAndFlush(Object msg) {
        ctx.writeAndFlush(msg);
    }

    @Override
    public void fireExceptionCaught(Exception e) {
        ctx.fireExceptionCaught(e);
    }

    @Override
    public void write(Object msg) {
        ctx.write(msg);
    }

    @Override
    public void flush() {
        ctx.flush();
    }
}