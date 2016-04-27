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

package org.kaaproject.kaa.server.transports.tcp.transport.messages;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder.CipherPair;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.session.SessionCreateListener;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.mockito.Mockito;

public class NettyTcpMessageTest {

    @Test
    public void connectTest() {
        UUID channelId = UUID.randomUUID();
        ChannelContext ctx = Mockito.mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        String sdkToken = "SdkToken";
        int keepAlive = 100;

        SessionInfo session = new SessionInfo(channelId, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctx, channelType, sessionKey, key,
                applicationToken, sdkToken, keepAlive, true);

        Connect command = new Connect(keepAlive, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, "aesSessionKey".getBytes(),
                "syncRequest".getBytes(), "signature".getBytes());
        SessionCreateListener listener = Mockito.mock(SessionCreateListener.class);
        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        NettyTcpConnectMessage message = new NettyTcpConnectMessage(channelId, ctx, command, channelType, listener, responseBuilder,
                errorBuilder);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());

        Assert.assertEquals(responseBuilder, message.getMessageBuilder());
        Assert.assertEquals(errorBuilder, message.getErrorBuilder());

        Assert.assertArrayEquals("syncRequest".getBytes(), message.getEncodedMessageData());
        Assert.assertArrayEquals("aesSessionKey".getBytes(), message.getEncodedSessionKey());
        Assert.assertArrayEquals("signature".getBytes(), message.getSessionKeySignature());
        message.onSessionCreated(session);
        Mockito.verify(listener).onSessionCreated(session);
        Assert.assertNotNull(message.toString());
    }

    @Test
    public void syncTest() {
        SyncRequest syncRequest = new SyncRequest();
        Assert.assertNotNull(syncRequest);
        UUID channelId = UUID.randomUUID();
        ChannelContext ctx = Mockito.mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        String sdkToken = "SdkToken";
        int keepAlive = 100;

        SessionInfo session = new SessionInfo(channelId, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctx, channelType, sessionKey, key,
                applicationToken, sdkToken, keepAlive, true);

        SyncRequest command = new SyncRequest("avroObject".getBytes(), false, false);

        NettyTcpSyncMessage message = new NettyTcpSyncMessage(command, session, null, null);

        Assert.assertArrayEquals("avroObject".getBytes(), message.getEncodedMessageData());
        Assert.assertEquals(session, message.getSessionInfo());
        Assert.assertNotNull(message.toString());
    }

    @Test
    public void disconnectTest() {
        UUID channelId = UUID.randomUUID();
        ChannelContext ctx = Mockito.mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        String sdkToken = "SdkToken";
        int keepAlive = 100;

        SessionInfo session = new SessionInfo(channelId, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctx, channelType, sessionKey, key,
                applicationToken, sdkToken, keepAlive, true);

        NettyTcpDisconnectMessage message = new NettyTcpDisconnectMessage(session);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());
        Assert.assertEquals(session, message.getSessionInfo());
    }

    @Test
    public void pingTest() {
        UUID channelId = UUID.randomUUID();
        ChannelContext ctx = Mockito.mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;
        CipherPair sessionKey = Mockito.mock(CipherPair.class);
        EndpointObjectHash key = EndpointObjectHash.fromSHA1("key");
        String applicationToken = "AppToken";
        String sdkToken = "SdkToken";
        int keepAlive = 100;

        SessionInfo session = new SessionInfo(channelId, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctx, channelType, sessionKey, key,
                applicationToken, sdkToken, keepAlive, true);

        NettyTcpPingMessage message = new NettyTcpPingMessage(session);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());
        Assert.assertEquals(session, message.getSessionInfo());
    }
}
