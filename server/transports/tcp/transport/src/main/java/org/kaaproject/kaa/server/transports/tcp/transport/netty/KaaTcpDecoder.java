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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Arrays;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageFactory;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessor;
import org.kaaproject.kaa.server.common.server.KaaCommandProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KaaTcpDecoder Class.
 *
 * @author Yaroslav Zeygerman
 */
public class KaaTcpDecoder extends SimpleChannelInboundHandler<byte[]> {
    private static final Logger LOG = LoggerFactory.getLogger(KaaTcpDecoder.class);

    public static final String KAA_TCP_COMMAND_NAME = "KaaTcp";

    private ChannelHandlerContext currentCtx;
    private KaaCommandProcessorFactory<MqttFrame, MqttFrame> commandFactory;

    public KaaTcpDecoder(KaaCommandProcessorFactory<MqttFrame, MqttFrame> commandFactory) {
        super();
        this.commandFactory = commandFactory;
    }

    private final MessageFactory messageFactory = new MessageFactory() {
        @Override
        public void onMqttFrame(MqttFrame frame) {
            try {
                KaaCommandProcessor<MqttFrame, MqttFrame> processor = commandFactory.createCommandProcessor();
                processFrame(frame, processor);
            } catch (Exception e) {
                LOG.error("Failed to process KaaTcp frame {}: {}", frame.getMessageType(), e);
            }
            super.onMqttFrame(frame);
        }
    };

    private void processFrame(MqttFrame frame, KaaCommandProcessor<MqttFrame, MqttFrame> processor) throws Exception {
        processor.setRequest(frame);
        currentCtx.fireChannelRead(processor);
    }

    public void channelReadCompete(ChannelHandlerContext ctx) throws Exception { // NOSONAR
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte[] data) throws Exception {
        if (LOG.isTraceEnabled()) {
            LOG.trace("channelRead0: {}", Arrays.toString(data));
        }
        currentCtx = ctx;
        messageFactory.getFramer().pushBytes(data);
    }

}
