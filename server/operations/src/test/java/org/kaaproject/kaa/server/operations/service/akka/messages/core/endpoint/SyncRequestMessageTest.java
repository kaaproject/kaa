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

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.ConfigurationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.EventSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.NotificationSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.UserSyncRequest;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;

public class SyncRequestMessageTest {

    @Test
    public void testIsValid(){

        NettySessionInfo session = new NettySessionInfo(UUID.randomUUID(), null, ChannelType.HTTP, null, null, "applicationToken", 0);

        SyncRequest request = new SyncRequest();
        request.setSyncRequestMetaData(new SyncRequestMetaData());
        SyncRequestMessage message = new SyncRequestMessage(session, request, null, null);

        SyncRequest other = new SyncRequest();
        other.setSyncRequestMetaData(new SyncRequestMetaData());
        SyncRequestMessage otherMessage = new SyncRequestMessage(session, other, null, null);

        Assert.assertFalse(message.isValid(TransportType.BOOTSTRAP));

        Assert.assertFalse(message.isValid(TransportType.PROFILE));
        other.setProfileSyncRequest(new ProfileSyncRequest());
        message.update(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.PROFILE));

        Assert.assertFalse(message.isValid(TransportType.CONFIGURATION));
        other.setConfigurationSyncRequest(new ConfigurationSyncRequest());
        message.update(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.CONFIGURATION));

        Assert.assertFalse(message.isValid(TransportType.NOTIFICATION));
        other.setNotificationSyncRequest(new NotificationSyncRequest());
        message.update(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.NOTIFICATION));

        Assert.assertFalse(message.isValid(TransportType.USER));
        other.setUserSyncRequest(new UserSyncRequest());
        message.update(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.USER));

        Assert.assertFalse(message.isValid(TransportType.LOGGING));
        other.setLogSyncRequest(new LogSyncRequest());
        message.update(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.LOGGING));

        Assert.assertFalse(message.isValid(TransportType.EVENT));
        other.setEventSyncRequest(new EventSyncRequest());
        message.update(otherMessage);
        Assert.assertTrue(message.isValid(TransportType.EVENT));

    }

}
