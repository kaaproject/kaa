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

package org.kaaproject.kaa.server.transports.tcp.transport.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Arrays;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KaaTcpEncoder Class.
 *
 * @author Yaroslav Zeygerman
 */
public class KaaTcpEncoder extends ChannelOutboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(KaaTcpEncoder.class);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof MqttFrame)) {
            LOG.warn("Message is not a {}", MqttFrame.class.getSimpleName());
            super.write(ctx, msg, promise);
        } else {
            MqttFrame frame = (MqttFrame) msg;
            byte[] data = frame.getFrame().array();
            if(LOG.isTraceEnabled()){
                LOG.trace("Sending {} data for frame {}", Arrays.toString(data), frame);
            }
            if(LOG.isTraceEnabled()){
                LOG.trace("Channel promise before writeAndFlush isSuccess [{}] isDone [{}] isCancelled [{}] for frame {}", promise.isSuccess(),
                        promise.isDone(), promise.isCancelled(), frame);
            }            
            ChannelFuture future = ctx.writeAndFlush(data, promise);
            if(LOG.isTraceEnabled()){
                LOG.trace("Returned future [{}] isSuccess [{}] isDone [{}] isCancelled [{}] cause [{}] for frame {}", future, future.isSuccess(),
                        future.isDone(), future.isCancelled(), future.cause(), frame);
                if (future.cause() != null) {
                    LOG.trace("Write operation failed due to:", future.cause());
                }
            }            
            if (frame.isNeedCloseConnection()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
