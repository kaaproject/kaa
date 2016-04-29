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

package org.kaaproject.kaa.server.transports.http.transport.messages;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transports.http.transport.commands.SyncCommand;
import org.mockito.Mockito;

public class NettyHttpMessageTest {

    @Test
    public void connectTest(){
        UUID channelId = UUID.randomUUID();
        ChannelContext ctx = Mockito.mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;

        SyncCommand command = Mockito.mock(SyncCommand.class);
        Mockito.when(command.getRequestData()).thenReturn("syncRequest".getBytes());
        Mockito.when(command.getRequestkey()).thenReturn("aesSessionKey".getBytes());
        Mockito.when(command.getRequestSignature()).thenReturn("signature".getBytes());
        MessageBuilder responseBuilder = Mockito.mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = Mockito.mock(ErrorBuilder.class);

        NettyHttpSyncMessage message = new NettyHttpSyncMessage(channelId, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctx, channelType, command, responseBuilder, errorBuilder);

        Assert.assertEquals(channelId, message.getChannelUuid());
        Assert.assertEquals(channelType, message.getChannelType());
        Assert.assertEquals(ctx, message.getChannelContext());

        Assert.assertEquals(responseBuilder, message.getMessageBuilder());
        Assert.assertEquals(errorBuilder, message.getErrorBuilder());

        Assert.assertArrayEquals("syncRequest".getBytes(), message.getEncodedMessageData());
        Assert.assertArrayEquals("aesSessionKey".getBytes(), message.getEncodedSessionKey());
        Assert.assertArrayEquals("signature".getBytes(), message.getSessionKeySignature());
        Assert.assertNotNull(message.toString());
    }

}
