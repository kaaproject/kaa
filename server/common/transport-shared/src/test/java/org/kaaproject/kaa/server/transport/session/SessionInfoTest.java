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

package org.kaaproject.kaa.server.transport.session;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.session.SessionInfo;

import static org.mockito.Mockito.mock;

/**
 *
 * @author Andrew Shvayka
 *
 */
public class SessionInfoTest {
    @Test
    public void equalsHashCodeTest() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        SessionInfo info1 = new SessionInfo(uuid1, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, null, null, null, null, null, null, 0, false);
        SessionInfo info2 = new SessionInfo(uuid1, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, null, null, null, null, null, null, 0, false);
        SessionInfo info3 = new SessionInfo(uuid2, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, null, null, null, null, null, null, 0, false);
        SessionInfo info4 = new SessionInfo(null, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID, null, null, null, null, null, null, 0, false);
        Assert.assertEquals(info1, info2);
        Assert.assertEquals(info1.hashCode(), info2.hashCode());
        Assert.assertNotEquals(info1, info3);
        Assert.assertNotEquals(info1.hashCode(), info3.hashCode());
        Assert.assertEquals(info1, info1);
        Assert.assertNotEquals(info1, null);
        Assert.assertNotEquals(info1, new Object());
        Assert.assertNotEquals(info4, info1);
    }

    @Test
    public void getSetTest() {
        UUID uuid = UUID.randomUUID();
        int platformId = 1;
        ChannelContext channelContext = mock(ChannelContext.class);
        ChannelType channelType = ChannelType.ASYNC;
        MessageEncoderDecoder.CipherPair cipherPair = mock(MessageEncoderDecoder.CipherPair.class);
        EndpointObjectHash objectHash = EndpointObjectHash.fromString("Some str");
        String appToken = "12423521";
        String sdkToken ="85121532";
        int keepAlive = 100;
        boolean isEncrypted = true;
        SessionInfo info = new SessionInfo(uuid, platformId, channelContext, channelType, cipherPair, objectHash, appToken, sdkToken, keepAlive, isEncrypted);
        Assert.assertEquals(uuid, info.getUuid());
        Assert.assertEquals(platformId, info.getPlatformId());
        Assert.assertEquals(channelContext, info.getCtx());
        Assert.assertEquals(channelType, info.getChannelType());
        Assert.assertEquals(cipherPair, info.getCipherPair());
        Assert.assertEquals(objectHash, info.getKey());
        Assert.assertEquals(appToken, info.getApplicationToken());
        Assert.assertEquals(keepAlive, info.getKeepAlive());
        Assert.assertEquals(isEncrypted, info.isEncrypted());
        Assert.assertNotNull(info.toString());
    }
}
