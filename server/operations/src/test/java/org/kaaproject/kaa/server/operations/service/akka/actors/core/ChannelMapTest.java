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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.ChannelMap.ChannelMetaData;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.http.commands.ChannelType;


public class ChannelMapTest {

    @Test
    public void testGetRequestById(){
        ChannelMap map = new ChannelMap("endpointKey", "actorKey");
        Assert.assertNull(map.getByRequestId(UUID.randomUUID()));
        SyncRequestMessage message = new SyncRequestMessage(null, null, null, null, null, null, null, null);
        map.addChannel(new ChannelMetaData(message));
        Assert.assertNotNull(map.getByRequestId(message.getUuid()));
        Assert.assertNull(map.getByRequestId(UUID.randomUUID()));
    }

    @Test
    public void testChannelMetaData(){
        SyncRequest request = new SyncRequest();
        request.setSyncRequestMetaData(new SyncRequestMetaData());
        UUID sameUid = UUID.randomUUID();
        SyncRequestMessage message = new SyncRequestMessage(null, sameUid.toString(), null, ChannelType.HTTP, null, null, request, null);
        ChannelMetaData md1 = new ChannelMetaData(message);
        SyncRequestMessage message2 = new SyncRequestMessage(null, sameUid.toString(), null, ChannelType.HTTP, null, null, request, null);
        ChannelMetaData md2 = new ChannelMetaData(message2);
        Assert.assertEquals(md1, md2);
        SyncRequest newRequest = new SyncRequest();
        newRequest.setSyncRequestMetaData(new SyncRequestMetaData());
        md2.updateRequest(new SyncRequestMessage(null, sameUid.toString(), null, ChannelType.TCP, "appToken", EndpointObjectHash.fromBytes(new byte[0]), request, null));
        Assert.assertEquals(md1, md2);

    }

}
