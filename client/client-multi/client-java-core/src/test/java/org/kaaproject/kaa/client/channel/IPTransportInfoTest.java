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

package org.kaaproject.kaa.client.channel;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolVersionPair;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;

/**
 * 
 * @author Andrew Shvayka
 *
 */
public class IPTransportInfoTest {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    protected static final int SIZE_OF_INT = 4;

    @Test
    public void testInit() throws NoSuchAlgorithmException {
        IPTransportInfo info = new IPTransportInfo(createTestServerInfo(ServerType.OPERATIONS, TransportProtocolIdConstants.TCP_TRANSPORT_ID,
                "localhost", 80, KeyUtil.generateKeyPair().getPublic()));
        
        Assert.assertEquals(ServerType.OPERATIONS, info.getServerType());
        Assert.assertEquals(TransportProtocolIdConstants.TCP_TRANSPORT_ID, info.getTransportId());
        Assert.assertEquals("localhost", info.getHost());
        Assert.assertEquals(80, info.getPort());
    }

    public static TransportConnectionInfo createTestServerInfo(ServerType serverType, TransportProtocolId id, String host, int port, PublicKey key) {
        ProtocolMetaData md = buildMetaData(id, host, port, key);
        return new GenericTransportInfo(serverType, md);
    }

    public static ProtocolMetaData buildMetaData(TransportProtocolId id, String host, int port, PublicKey key) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[SIZE_OF_INT * 3 + host.getBytes(UTF8).length + key.getEncoded().length]);
        buf.putInt(key.getEncoded().length);
        buf.put(key.getEncoded());
        buf.putInt(host.getBytes(UTF8).length);
        buf.put(host.getBytes(UTF8));
        buf.putInt(port);
        ProtocolMetaData md = new ProtocolMetaData((host + ":" + port).hashCode(), new ProtocolVersionPair(id.getProtocolId(), id.getProtocolVersion()), buf);
        return md;
    }

}
