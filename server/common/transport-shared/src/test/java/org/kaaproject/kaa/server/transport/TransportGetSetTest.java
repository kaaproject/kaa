/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.transport;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.AbstractMessage;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.SessionControlMessage;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

import java.util.UUID;

import static org.mockito.Mockito.mock;

public class TransportGetSetTest {
    @Test
    public void abstractMessageGetSetTest() {
        UUID uuid = UUID.randomUUID();
        ChannelContext channelContext = mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        ErrorBuilder errorBuilder = mock(ErrorBuilder.class);
        int platformId = 10;
        AbstractMessage abstractMessage = new AbstractMessage(uuid, platformId, channelContext, channelType, messageBuilder, errorBuilder) {
            @Override
            public UUID getChannelUuid() {
                return super.getChannelUuid();
            }
        };
        Assert.assertEquals(uuid, abstractMessage.getChannelUuid());
        Assert.assertEquals(channelContext, abstractMessage.getChannelContext());
        Assert.assertEquals(channelType, abstractMessage.getChannelType());
        Assert.assertEquals(messageBuilder, abstractMessage.getMessageBuilder());
        Assert.assertEquals(errorBuilder, abstractMessage.getErrorBuilder());
    }

    @Test
    public void sessionControlMessageGetSetTest() {
        SessionInfo sessionInfo = new SessionInfo(UUID.randomUUID(), 10, mock(ChannelContext.class),
                ChannelType.ASYNC, null, null, null, 10, true);
        SessionControlMessage sessionControlMessage = new SessionControlMessage(sessionInfo) {
            @Override
            public UUID getChannelUuid() {
                return super.getChannelUuid();
            }
        };
        Assert.assertEquals(sessionInfo, sessionControlMessage.getSessionInfo());
        Assert.assertEquals(sessionInfo.getUuid(), sessionControlMessage.getChannelUuid());
        Assert.assertEquals(sessionInfo.getChannelType(), sessionControlMessage.getChannelType());
        Assert.assertEquals(sessionInfo.getCtx(), sessionControlMessage.getChannelContext());
    }

    @Test
    public void transportMetadataGetSetTest() {
        int version = 2;
        byte[] connectionInfo = new byte[]{1, 2, 3};
        TransportMetaData transportMetaData = new TransportMetaData(1, 10, new byte[2]);
        transportMetaData.setConnectionInfo(version, connectionInfo);
        Assert.assertArrayEquals(connectionInfo, transportMetaData.getConnectionInfo(2));
        Assert.assertNotNull(new TransportMetaData(1,1,new byte[1]).toString());
    }

    @Test
    public void channelTypeGetSetTest() {
        ChannelType sync = ChannelType.SYNC;
        Assert.assertFalse(sync.isLongPoll());
        Assert.assertFalse(sync.isAsync());
        ChannelType syncWithTimeout = ChannelType.SYNC_WITH_TIMEOUT;
        Assert.assertFalse(syncWithTimeout.isAsync());
        Assert.assertTrue(syncWithTimeout.isLongPoll());
        ChannelType async = ChannelType.ASYNC;
        Assert.assertTrue(async.isAsync());
        Assert.assertFalse(async.isLongPoll());
    }

    @Test
    public void exceptionTest(){
        Assert.assertNotNull(new InvalidApplicationTokenException("msg"));
        Assert.assertNotNull(new TransportLifecycleException(new Exception()));
    }
}
