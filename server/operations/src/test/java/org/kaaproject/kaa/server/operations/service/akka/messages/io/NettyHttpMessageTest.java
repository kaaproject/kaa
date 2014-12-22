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
import org.kaaproject.kaa.server.operations.service.akka.actors.io.platform.AvroEncDec;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ErrorBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.NettyHttpSyncMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ResponseBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SyncStatistics;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.http.commands.SyncCommand;
import org.mockito.Mockito;

public class NettyHttpMessageTest {

    @Test
    public void connectTest(){
        UUID channelId = UUID.randomUUID();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        ChannelType channelType = ChannelType.TCP;

        SyncCommand command = Mockito.mock(SyncCommand.class);
        Mockito.when(command.getRequestData()).thenReturn("syncRequest".getBytes());
        Mockito.when(command.getRequestkey()).thenReturn("aesSessionKey".getBytes());
        Mockito.when(command.getRequestSignature()).thenReturn("signature".getBytes());
        ResponseBuilder responseBuilder = Mockito.mock(ResponseBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);
        SyncStatistics stats = Mockito.mock(SyncStatistics.class);

        NettyHttpSyncMessage message = new NettyHttpSyncMessage(channelId, AvroEncDec.AVRO_ENC_DEC_ID, ctx, channelType, command, responseBuilder, errorBuilder, stats);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());

        Assert.assertEquals(responseBuilder, message.getResponseBuilder());
        Assert.assertEquals(errorBuilder, message.getErrorBuilder());
        Assert.assertEquals(stats, message.getSyncStatistics());

        Assert.assertArrayEquals("syncRequest".getBytes(), message.getEncodedRequestData());
        Assert.assertArrayEquals("aesSessionKey".getBytes(), message.getEncodedSessionKey());
        Assert.assertArrayEquals("signature".getBytes(), message.getSessionKeySignature());
        Assert.assertNotNull(message.toString());
    }

}
