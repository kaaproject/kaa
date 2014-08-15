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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageFactory;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.server.common.server.CommandFactory;
import org.kaaproject.kaa.server.common.server.Track;
import org.kaaproject.kaa.server.common.server.http.NettyHttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KaaTcpDecoder Class.
 *
 * @author Yaroslav Zeygerman
 */
public class KaaTcpDecoder extends SimpleChannelInboundHandler<byte []>  {
    private static final Logger LOG = LoggerFactory.getLogger(KaaTcpDecoder.class);

    public static final String KAA_TCP_COMMAND_NAME = "KaaTcp";

    private final CommandFactory commandFactory;

    public KaaTcpDecoder(CommandFactory commandFactory){
        this.commandFactory = commandFactory;
    }

    private final MessageFactory messageFactory = new MessageFactory() {
        @Override
        public void onMqttFrame(MqttFrame frame) {
            try {
                AbstractKaaTcpCommandProcessor processor = (AbstractKaaTcpCommandProcessor) commandFactory
                        .getCommandProcessorByName(KAA_TCP_COMMAND_NAME);
                processFrame(frame, processor);
            } catch (Exception e) {
                LOG.error("Failed to process KaaTcp frame {}: {}", frame.getMessageType(), e);
            }
            super.onMqttFrame(frame);
        }
    };

    private ChannelHandlerContext currentCtx;

    private void processFrame(MqttFrame frame, AbstractKaaTcpCommandProcessor processor) throws Exception {
        Attribute<Track> sessionTrackAttr = currentCtx.channel().attr(NettyHttpServer.TRACK_KEY);
        if (sessionTrackAttr.get() != null) {
            int id = sessionTrackAttr.get().newRequest(processor.getName());
            processor.setCommandId(id);
        }
        processor.setRequest(frame);
        currentCtx.fireChannelRead(processor);
    }

    public void channelReadCompete(ChannelHandlerContext ctx) throws Exception { //NOSONAR
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, byte [] data)
            throws Exception {
        if(LOG.isTraceEnabled()){
            LOG.trace("channelRead0: {}", Arrays.toString(data));
        }
        currentCtx = ctx;
        messageFactory.getFramer().pushBytes(data);
    }

}
