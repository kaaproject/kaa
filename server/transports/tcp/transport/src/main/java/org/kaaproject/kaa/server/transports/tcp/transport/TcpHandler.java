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

package org.kaaproject.kaa.server.transports.tcp.transport;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

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
import org.kaaproject.kaa.server.common.server.NettyChannelContext;
import org.kaaproject.kaa.server.transport.EndpointVerificationException;
import org.kaaproject.kaa.server.transport.InvalidSDKTokenException;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.kaaproject.kaa.server.transport.session.SessionCreateListener;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.kaaproject.kaa.server.transports.tcp.transport.messages.NettyTcpConnectMessage;
import org.kaaproject.kaa.server.transports.tcp.transport.messages.NettyTcpDisconnectMessage;
import org.kaaproject.kaa.server.transports.tcp.transport.messages.NettyTcpPingMessage;
import org.kaaproject.kaa.server.transports.tcp.transport.messages.NettyTcpSyncMessage;
import org.kaaproject.kaa.server.transports.tcp.transport.netty.AbstractKaaTcpCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpHandler extends SimpleChannelInboundHandler<AbstractKaaTcpCommandProcessor> implements SessionCreateListener {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(TcpHandler.class);

    private static final boolean NOT_ZIPPED = false;

    private final UUID uuid;
    private final MessageHandler handler;
    private volatile SessionInfo session;
    private volatile boolean sessionDisconnected;

    private final static ErrorBuilder connectErrorConverter = new ErrorBuilder() { //NOSONAR
        @Override
        public Object[] build(Exception e) {
            Object[] responses = new Object[1];
            if (e instanceof GeneralSecurityException || e instanceof IOException ||
                    e instanceof IllegalArgumentException || e instanceof InvalidSDKTokenException) {
                responses[0] = new ConnAck(ReturnCode.REFUSE_BAD_CREDENTIALS);
            } else if (e instanceof EndpointVerificationException) {
                responses[0] = new ConnAck(ReturnCode.REFUSE_VERIFICATION_FAILED);
            } else {
                responses[0] = new ConnAck(ReturnCode.REFUSE_SERVER_UNAVAILABLE);
            }
            return responses;
        }
    };

    private final static MessageBuilder syncResponseConverter = new MessageBuilder() { //NOSONAR
        @Override
        public Object[] build(byte[] encriptedResponseData, byte[] encriptedResponseSignature, boolean isEncrypted) {
            Object[] responses = new Object[1];
            responses[0] = new org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncResponse(encriptedResponseData, NOT_ZIPPED,
                    isEncrypted);
            LOG.debug("Sending {} response objects", responses.length);
            return responses;
        }

        @Override
        public Object[] build(byte[] messageData, boolean isEncrypted) {
            return build(messageData, null, isEncrypted);
        }
    };

    private final static ErrorBuilder syncErrorConverter = new ErrorBuilder() { //NOSONAR
        @Override
        public Object[] build(Exception e) {
            Object[] responses = new Object[1];
            if (e instanceof GeneralSecurityException || e instanceof IOException ||
                    e instanceof IllegalArgumentException || e instanceof InvalidSDKTokenException) {
                responses[0] = new Disconnect(DisconnectReason.BAD_REQUEST);
            } else if (e instanceof EndpointVerificationException) {
                responses[0] = new Disconnect(DisconnectReason.CREDENTIALS_REVOKED);
            } else {
                responses[0] = new Disconnect(DisconnectReason.INTERNAL_ERROR);
            }
            return responses;
        }
    };

    private MessageBuilder connectResponseConverter;

    public TcpHandler(UUID uuid, MessageHandler akkaService) {
        this.uuid = uuid;
        this.handler = akkaService;
        this.connectResponseConverter = new MessageBuilder() {
            volatile boolean connAckSent = false;

            @Override
            public Object[] build(byte[] encriptedResponseData, byte[] encriptedResponseSignature, boolean isEncrypted) {
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
                return syncResponseConverter.build(encriptedResponseData, encriptedResponseSignature, isEncrypted);
            }

            @Override
            public Object[] build(byte[] messageData, boolean isEncrypted) {
                return build(messageData, null, isEncrypted);
            }
        };
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractKaaTcpCommandProcessor msg) throws Exception {
        MqttFrame frame = msg.getRequest();
        LOG.trace("[{}] Processing {}", uuid, frame);
        if (frame.getMessageType() == MessageType.CONNECT) {
            ChannelFuture closeFuture = ctx.channel().closeFuture();
            closeFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (!sessionDisconnected) {
                        if (session != null) {
                            handler.process(new NettyTcpDisconnectMessage(session));
                            LOG.trace("[{}] Channel is closed - sending disconnect", uuid);
                        } else {
                            LOG.trace("[{}] Session is not yet established. Skip sending disconnect", uuid);
                        }
                        sessionDisconnected = true;
                    } else {
                        LOG.trace("[{}] Channel is closed and disconnect is already sent", uuid);
                    }
                }
            });

            if (session == null) {
                handler.process(new NettyTcpConnectMessage(uuid, new NettyChannelContext(ctx), (Connect) frame, ChannelType.ASYNC, this,
                        connectResponseConverter, connectErrorConverter));
            } else {
                LOG.info("[{}] Ignoring duplicate {} message ", uuid, MessageType.CONNECT);
            }
        } else {
            if (session != null) {
                switch (frame.getMessageType()) {
                case KAASYNC:
                    if (((KaaSync) frame).getKaaSyncMessageType() == KaaSyncMessageType.SYNC) {
                        handler.process(new NettyTcpSyncMessage((SyncRequest) frame, session, syncResponseConverter, syncErrorConverter));
                    }
                    break;
                case PINGREQ:
                    handler.process(new NettyTcpPingMessage(session));
                    break;
                case DISCONNECT:
                    sessionDisconnected = true;
                    handler.process(new NettyTcpDisconnectMessage(session));
                    break;
                default:
                    break;
                }
            } else {
                LOG.info("[{}] Ignoring {} message due to incomplete CONNECT sequence", uuid, frame.getMessageType());
                ctx.writeAndFlush(new ConnAck(ReturnCode.REFUSE_BAD_PROTOCOL));
            }
        }
    }

    @Override
    public void onSessionCreated(SessionInfo session) {
        LOG.trace("[{}] Session info is set to {}", uuid, session);
        this.session = session;
    }

}
