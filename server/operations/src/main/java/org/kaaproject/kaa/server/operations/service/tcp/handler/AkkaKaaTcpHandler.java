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
package org.kaaproject.kaa.server.operations.service.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck.ReturnCode;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.KaaSync;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.KaaSyncMessageType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpCommandProcessor;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.actors.io.platform.AvroEncDec;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ErrorBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpPingMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpSyncMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ResponseBuilder;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.SessionCreateListener;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AkkaKaaTcpHandler extends SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> implements SessionCreateListener {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AkkaKaaTcpHandler.class);

    private static final boolean NOT_ZIPPED = false;

    private final UUID uuid;
    private final AkkaService akkaService;
    private volatile NettySessionInfo session;

    private final static ErrorBuilder connectErrorConverter = new ErrorBuilder() {
        @Override
        public Object[] build(Exception e) {
            Object[] responses = new Object[1];
            if (e instanceof GeneralSecurityException || e instanceof IOException || e instanceof IllegalArgumentException) {
                responses[0] = new ConnAck(ReturnCode.REFUSE_BAD_CREDETIALS);
            } else {
                responses[0] = new ConnAck(ReturnCode.REFUSE_SERVER_UNAVAILABLE);
            }
            return responses;
        }
    };

    private final static ResponseBuilder syncResponseConverter = new ResponseBuilder() {
        @Override
        public Object[] build(byte[] encriptedResponseData, boolean isEncrypted) {
            Object[] responses = new Object[1];
            responses[0] = new org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncResponse(encriptedResponseData, NOT_ZIPPED,
                    isEncrypted);
            LOG.debug("Sending {} response objects", responses.length);
            return responses;
        }
    };

    private final static ErrorBuilder syncErrorConverter = new ErrorBuilder() {
        @Override
        public Object[] build(Exception e) {
            Object[] responses = new Object[1];
            if (e instanceof GeneralSecurityException || e instanceof IOException || e instanceof IllegalArgumentException) {
                responses[0] = new Disconnect(DisconnectReason.BAD_REQUEST);
            } else {
                responses[0] = new Disconnect(DisconnectReason.INTERNAL_ERROR);
            }
            return responses;
        }
    };

    private ResponseBuilder connectResponseConverter;

    public AkkaKaaTcpHandler(UUID uuid, AkkaService akkaService) {
        this.uuid = uuid;
        this.akkaService = akkaService;
        this.connectResponseConverter = new ResponseBuilder() {
            volatile boolean connAckSent = false;

            @Override
            public Object[] build(byte[] encriptedResponseData, boolean isEncrypted) {
                if (!connAckSent) {
                    synchronized (this) {
                        if (!connAckSent) {
                            connAckSent = true;
                            Object[] responses = new Object[2];
                            responses[0] = new ConnAck(ReturnCode.ACCEPTED);
                            responses[1] = new org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncResponse(
                                    encriptedResponseData, NOT_ZIPPED, isEncrypted);
                            LOG.debug("Sending {} response objects", responses.length);
                            return responses;
                        }
                    }
                }
                return syncResponseConverter.build(encriptedResponseData, isEncrypted);
            }
        };
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractKaaTcpCommandProcessor msg) throws Exception {
        MqttFrame frame = msg.getRequest();
        LOG.trace("[{}] Processing {}", uuid, frame);
        if (frame.getMessageType() == MessageType.CONNECT) {
            if (session == null) {
                akkaService.process(new NettyTcpConnectMessage(uuid, ctx, (Connect) frame, ChannelType.TCP,
                        this, connectResponseConverter, connectErrorConverter, null));
            } else {
                LOG.warn("[{}] Ignoring duplicate {} message ", uuid, MessageType.CONNECT);
            }
        } else {
            if (session != null) {
                switch (frame.getMessageType()) {
                case KAASYNC:
                    if (((KaaSync) frame).getKaaSyncMessageType() == KaaSyncMessageType.SYNC) {
                        akkaService.process(new NettyTcpSyncMessage((SyncRequest) frame, session, syncResponseConverter,
                                syncErrorConverter, null));
                    }
                    break;
                case PINGREQ:
                    akkaService.process(new NettyTcpPingMessage(session));
                    break;
                case DISCONNECT:
                    akkaService.process(new NettyTcpDisconnectMessage(session));
                    break;
                default:
                    break;
                }
            } else {
                LOG.warn("[{}] Ignoring {} message due to incomplete CONNECT sequence", uuid, frame.getMessageType());
            }
        }
    }

    @Override
    public void onSessionCreated(NettySessionInfo session) {
        LOG.trace("[{}] Session info is set to {}", uuid, session);
        this.session = session;
    }

}
