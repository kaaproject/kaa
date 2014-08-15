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

import java.security.GeneralSecurityException;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck.ReturnCode;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.KaaSync;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.server.kaatcp.AbstractKaaTcpCommandProcessor;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpSyncMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAwareRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionInitRequest;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.mockito.Mockito;

import akka.actor.ActorSystem;

public class AkkaKaaTcpHandlerTest {

    AkkaService akkaService = new AkkaService() {

        @Override
        public void process(SessionInitRequest message) {
            Object[] response = message.getResponseBuilder().build("response".getBytes());
            Assert.assertEquals(2, response.length);
            Assert.assertTrue(response[0] instanceof ConnAck);
            Assert.assertTrue(response[1] instanceof KaaSync);
            response = message.getErrorBuilder().build(Mockito.mock(GeneralSecurityException.class));
            Assert.assertTrue(response[0] instanceof ConnAck);
            ConnAck connAck = (ConnAck) response[0];
            Assert.assertEquals(ReturnCode.REFUSE_BAD_CREDETIALS, connAck.getReturnCode());
        }

        @Override
        public void process(SessionAware message) {
            if (message instanceof SessionAwareRequest) {
                SessionAwareRequest request = (SessionAwareRequest) message;
                Object[] response;
                response = request.getErrorBuilder().build(Mockito.mock(GeneralSecurityException.class));
                Assert.assertTrue(response[0] instanceof Disconnect);
                Disconnect disconnect = (Disconnect) response[0];
                Assert.assertEquals(DisconnectReason.BAD_REQUEST, disconnect.getReason());
                response = request.getErrorBuilder().build(Mockito.mock(RuntimeException.class));
                Assert.assertTrue(response[0] instanceof Disconnect);
                disconnect = (Disconnect) response[0];
                Assert.assertEquals(DisconnectReason.INTERNAL_ERROR, disconnect.getReason());
            }

        }

        @Override
        public void onRedirectionRule(RedirectionRule redirectionRule) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onNotification(Notification notification) {
            // TODO Auto-generated method stub

        }

        @Override
        public ActorSystem getActorSystem() {
            // TODO Auto-generated method stub
            return null;
        }
    };

    @Test
    public void testConnect() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new Connect());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService).process(Mockito.any(NettyTcpConnectMessage.class));
    }

    @Test
    public void testConnectHandlers() throws Exception {
        UUID uuid = UUID.randomUUID();

        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new Connect());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.channelRead0(null, msg);
    }

    @Test
    public void testDuplicateConnect() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new Connect());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.onSessionCreated(buildSessionInfo(uuid));
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService, Mockito.never()).process(Mockito.any(NettyTcpConnectMessage.class));
    }

    @Test
    public void testKaaSyncWithoutSession() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new KaaSync());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService, Mockito.never()).process(Mockito.any(NettyTcpConnectMessage.class));
    }

    @Test
    public void testKaaSync() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new KaaSync());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.onSessionCreated(buildSessionInfo(uuid));
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService).process(Mockito.any(NettyTcpSyncMessage.class));
    }

    @Test
    public void testKaaSyncHandlers() throws Exception {
        UUID uuid = UUID.randomUUID();

        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new KaaSync());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.onSessionCreated(buildSessionInfo(uuid));
        handler.channelRead0(null, msg);
    }

    @Test
    public void testPing() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new PingRequest());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.onSessionCreated(buildSessionInfo(uuid));
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService).process(Mockito.any(NettyTcpSyncMessage.class));
    }

    @Test
    public void testDisconnect() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new PingRequest());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.onSessionCreated(buildSessionInfo(uuid));
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService).process(Mockito.any(NettyTcpDisconnectMessage.class));
    }

    @Test
    public void testUnsupportedMessage() throws Exception {
        UUID uuid = UUID.randomUUID();
        AkkaService akkaService = Mockito.mock(AkkaService.class);
        AbstractKaaTcpCommandProcessor msg = Mockito.mock(AbstractKaaTcpCommandProcessor.class);
        Mockito.when(msg.getRequest()).thenReturn(new PingResponse());
        AkkaKaaTcpHandler handler = new AkkaKaaTcpHandler(uuid, akkaService);
        handler.onSessionCreated(buildSessionInfo(uuid));
        handler.channelRead0(null, msg);
        Mockito.verify(akkaService, Mockito.never()).process(Mockito.any(SessionAware.class));
        Mockito.verify(akkaService, Mockito.never()).process(Mockito.any(SessionInitRequest.class));
    }

    protected NettySessionInfo buildSessionInfo(UUID uuid) {
        return new NettySessionInfo(uuid, Mockito.mock(ChannelHandlerContext.class), ChannelType.TCP, Mockito.mock(SecretKey.class),
                EndpointObjectHash.fromSHA1("test"), "applicationToken", 100);
    }
}
