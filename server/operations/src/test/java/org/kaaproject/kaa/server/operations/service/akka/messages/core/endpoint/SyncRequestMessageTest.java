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
package org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.protocol.ClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ClientSyncMetaData;
import org.kaaproject.kaa.common.endpoint.protocol.ConfigurationClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.EventClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.LogClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.NotificationClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.ProfileClientSync;
import org.kaaproject.kaa.common.endpoint.protocol.UserClientSync;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

public class SyncRequestMessageTest {

    @Test
    public void testIsValid(){

        NettySessionInfo session = new NettySessionInfo(UUID.randomUUID(), null, ChannelType.HTTP, null, null, "applicationToken", 0, true);

        ClientSync request = new ClientSync();
        request.setSyncRequestMetaData(new ClientSyncMetaData());
        SyncRequestMessage message = new SyncRequestMessage(session, request, null, null);

        ClientSync other = new ClientSync();
        other.setSyncRequestMetaData(new ClientSyncMetaData());
        SyncRequestMessage otherMessage = new SyncRequestMessage(session, other, null, null);

        Assert.assertFalse(message.isValid(TransportType.BOOTSTRAP));

        Assert.assertFalse(message.isValid(TransportType.PROFILE));
        other.setProfileSyncRequest(new ProfileClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.PROFILE));

        Assert.assertFalse(message.isValid(TransportType.CONFIGURATION));
        other.setConfigurationSyncRequest(new ConfigurationClientSync(10, ByteBuffer.wrap("String".getBytes())));
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.CONFIGURATION));

        Assert.assertFalse(message.isValid(TransportType.NOTIFICATION));
        other.setNotificationSyncRequest(new NotificationClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.NOTIFICATION));

        Assert.assertFalse(message.isValid(TransportType.USER));
        other.setUserSyncRequest(new UserClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.USER));

        Assert.assertFalse(message.isValid(TransportType.LOGGING));
        other.setLogSyncRequest(new LogClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.LOGGING));

        Assert.assertFalse(message.isValid(TransportType.EVENT));
        other.setEventSyncRequest(new EventClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.EVENT));

    }

}
