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
package org.kaaproject.kaa.server.operations.service.akka.messages.io;

import io.netty.channel.ChannelHandlerContext;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder.CipherPair;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.actors.io.platform.AvroEncDec;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ErrorBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpConnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpDisconnectMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpPingMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyTcpSyncMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ResponseBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SyncStatistics;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.kaaproject.kaa.server.operations.service.netty.SessionCreateListener;
import org.mockito.Mockito;

public class NettyTcpMessageTest {

    @Test
    public void connectTest() {
        UUID channelId = UUID.randomUUID();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        ChannelType channelType = ChannelType.TCP;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        int keepAlive = 100;

        NettySessionInfo session = new NettySessionInfo(channelId, AvroEncDec.AVRO_ENC_DEC_ID, ctx, channelType, sessionKey, key,
                applicationToken, keepAlive, true);

        Connect command = new Connect(keepAlive, "aesSessionKey".getBytes(), "syncRequest".getBytes(), "signature".getBytes());
        SessionCreateListener listener = Mockito.mock(SessionCreateListener.class);
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        SyncStatistics stats = Mockito.mock(SyncStatistics.class);
        NettyTcpConnectMessage message = new NettyTcpConnectMessage(channelId, AvroEncDec.AVRO_ENC_DEC_ID, ctx, command, channelType,
                listener, responseBuilder, errorBuilder, stats);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());

        Assert.assertEquals(responseBuilder, message.getResponseBuilder());
        Assert.assertEquals(errorBuilder, message.getErrorBuilder());
        Assert.assertEquals(stats, message.getSyncStatistics());

        Assert.assertArrayEquals("syncRequest".getBytes(), message.getEncodedRequestData());
        Assert.assertArrayEquals("aesSessionKey".getBytes(), message.getEncodedSessionKey());
        Assert.assertArrayEquals("signature".getBytes(), message.getSessionKeySignature());
        message.onSessionCreated(session);
        Mockito.verify(listener).onSessionCreated(session);
        Assert.assertNotNull(message.toString());
    }

    @Test
    public void syncTest() {
        UUID channelId = UUID.randomUUID();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        ChannelType channelType = ChannelType.TCP;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        int keepAlive = 100;

        NettySessionInfo session = new NettySessionInfo(channelId, AvroEncDec.AVRO_ENC_DEC_ID, ctx, channelType, sessionKey, key,
                applicationToken, keepAlive, true);

        SyncRequest command = new SyncRequest("avroObject".getBytes(), false, false);

        NettyTcpSyncMessage message = new NettyTcpSyncMessage(command, session, null, null, null);

        Assert.assertArrayEquals("avroObject".getBytes(), message.getEncodedRequestData());
        Assert.assertEquals(session, message.getSessionInfo());
        Assert.assertNotNull(message.toString());
    }

    @Test
    public void disconnectTest() {
        UUID channelId = UUID.randomUUID();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        ChannelType channelType = ChannelType.TCP;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        int keepAlive = 100;

        NettySessionInfo session = new NettySessionInfo(channelId, AvroEncDec.AVRO_ENC_DEC_ID, ctx, channelType, sessionKey, key,
                applicationToken, keepAlive, true);

        NettyTcpDisconnectMessage message = new NettyTcpDisconnectMessage(session);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());
        Assert.assertEquals(session, message.getSessionInfo());
    }

    @Test
    public void pingTest() {
        UUID channelId = UUID.randomUUID();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        ChannelType channelType = ChannelType.TCP;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        int keepAlive = 100;

        NettySessionInfo session = new NettySessionInfo(channelId, AvroEncDec.AVRO_ENC_DEC_ID, ctx, channelType, sessionKey, key,
                applicationToken, keepAlive, true);

        NettyTcpPingMessage message = new NettyTcpPingMessage(session);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());
        Assert.assertEquals(session, message.getSessionInfo());
    }

}
