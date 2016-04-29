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

package org.kaaproject.kaa.server.operations.service.akka.messages.io;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.NettyChannelContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettySessionResponseMessage;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

import java.util.UUID;

public class NettySessionResponseMessageTest {

    @Test
    public void testNettySessionResponseMessage() {
        UUID uuid = UUID.randomUUID();
        ChannelContext channelContext = new NettyChannelContext(null);
        ErrorBuilder errorBuilder = new ErrorBuilder() {
            @Override
            public Object[] build(Exception e) {
                return new Object[0];
            }
        };
        SessionInfo sessionInfo = new SessionInfo(uuid, 1, channelContext, ChannelType.SYNC, null, null, null, null, 1, true);
        NettySessionResponseMessage responseMessage = new NettySessionResponseMessage(sessionInfo, null, null, null, errorBuilder);
        Assert.assertEquals(uuid, responseMessage.getChannelUuid());
        Assert.assertEquals(errorBuilder, responseMessage.getErrorBuilder());
        Assert.assertEquals(ChannelType.SYNC, responseMessage.getChannelType());
        Assert.assertEquals(channelContext, responseMessage.getChannelContext());
    }
}
