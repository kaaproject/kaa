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
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.sync.ConfigurationClientSync;
import org.kaaproject.kaa.server.sync.EventClientSync;
import org.kaaproject.kaa.server.sync.LogClientSync;
import org.kaaproject.kaa.server.sync.NotificationClientSync;
import org.kaaproject.kaa.server.sync.ProfileClientSync;
import org.kaaproject.kaa.server.sync.UserClientSync;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

public class SyncRequestMessageTest {

    @Test
    public void testIsValid() {

        SessionInfo session = new SessionInfo(UUID.randomUUID(), Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, null, ChannelType.SYNC, null, null,
                "applicationToken", 0, true);

        ClientSync request = new ClientSync();
        request.setClientSyncMetaData(new ClientSyncMetaData());
        SyncRequestMessage message = new SyncRequestMessage(session, request, null, null);

        ClientSync other = new ClientSync();
        other.setClientSyncMetaData(new ClientSyncMetaData());
        SyncRequestMessage otherMessage = new SyncRequestMessage(session, other, null, null);

        Assert.assertFalse(message.isValid(TransportType.BOOTSTRAP));

        Assert.assertFalse(message.isValid(TransportType.PROFILE));
        other.setProfileSync(new ProfileClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.PROFILE));

        Assert.assertFalse(message.isValid(TransportType.CONFIGURATION));
        other.setConfigurationSync(new ConfigurationClientSync(10, ByteBuffer.wrap("String".getBytes()), false));
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.CONFIGURATION));

        Assert.assertFalse(message.isValid(TransportType.NOTIFICATION));
        other.setNotificationSync(new NotificationClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.NOTIFICATION));

        Assert.assertFalse(message.isValid(TransportType.USER));
        other.setUserSync(new UserClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.USER));

        Assert.assertFalse(message.isValid(TransportType.LOGGING));
        other.setLogSync(new LogClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.LOGGING));

        Assert.assertFalse(message.isValid(TransportType.EVENT));
        other.setEventSync(new EventClientSync());
        message.merge(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.EVENT));

    }

}
