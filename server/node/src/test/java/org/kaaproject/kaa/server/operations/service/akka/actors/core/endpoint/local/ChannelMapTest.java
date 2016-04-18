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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.ChannelMap;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint.local.ChannelMap.ChannelMetaData;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.mockito.Mockito;

public class ChannelMapTest {

    @Test
    public void testGetRequestById() {
        ChannelMap map = new ChannelMap("endpointKey", "actorKey");
        Assert.assertNull(map.getByRequestId(UUID.randomUUID()));
        ChannelContext ctxMock = Mockito.mock(ChannelContext.class);
        SessionInfo session = new SessionInfo(UUID.randomUUID(), Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctxMock, ChannelType.SYNC, null,
                null, "applicationToken", "sdkToken", 0, true);
        SyncRequestMessage message = new SyncRequestMessage(session, null, null, null);
        map.addChannel(new ChannelMetaData(message));
        Assert.assertNotNull(map.getByRequestId(message.getChannelUuid()));
        Assert.assertNull(map.getByRequestId(UUID.randomUUID()));
    }

    @Test
    public void testChannelMetaData() {
        ClientSync request = new ClientSync();
        request.setClientSyncMetaData(new ClientSyncMetaData());
        UUID sameUid = UUID.randomUUID();
        ChannelContext ctxMock = Mockito.mock(ChannelContext.class);
        SessionInfo session = new SessionInfo(sameUid, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, ctxMock, ChannelType.SYNC, null, null,
                "applicationToken", "sdkToken", 0, true);
        SyncRequestMessage message = new SyncRequestMessage(session, request, null, null);
        ChannelMetaData md1 = new ChannelMetaData(message);
        SyncRequestMessage message2 = new SyncRequestMessage(session, request, null, null);
        ChannelMetaData md2 = new ChannelMetaData(message2);
        Assert.assertEquals(md1, md2);
        ClientSync newRequest = new ClientSync();
        newRequest.setClientSyncMetaData(new ClientSyncMetaData());
        md2.mergeRequest(new SyncRequestMessage(session, request, null, null));
        Assert.assertEquals(md1, md2);
    }

}
