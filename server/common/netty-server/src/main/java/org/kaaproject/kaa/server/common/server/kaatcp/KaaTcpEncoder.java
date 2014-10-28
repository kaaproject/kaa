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

package org.kaaproject.kaa.server.common.server.kaatcp;

import java.util.Arrays;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

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
            return;
        } else {
            MqttFrame frame = (MqttFrame) msg;
            byte[] data = frame.getFrame().array();
            if(LOG.isTraceEnabled()){
                LOG.trace("Sending {} data for frame {}", Arrays.toString(data), frame);
            }
            ChannelFuture future = ctx.writeAndFlush(data, promise);
            if (frame.isNeedCloseConnection()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
