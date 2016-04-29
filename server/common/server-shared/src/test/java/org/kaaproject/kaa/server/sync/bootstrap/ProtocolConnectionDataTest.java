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

package org.kaaproject.kaa.server.sync.bootstrap;

import org.junit.Assert;
import org.junit.Test;

public class ProtocolConnectionDataTest {

    @Test
    public void protocolConnectionDataTest() {
        int accessPointId = 1;
        int protocolId = 1;
        int protocolVersion = 1;
        ProtocolVersionId protocolVersionId = new ProtocolVersionId(protocolId, protocolVersion);
        byte[] connectionData = new byte[10];
        ProtocolConnectionData protocolConnectionData = new ProtocolConnectionData(accessPointId, protocolVersionId, connectionData);
        Assert.assertEquals(accessPointId, protocolConnectionData.getAccessPointId());
        Assert.assertEquals(connectionData, protocolConnectionData.getConnectionData());
        Assert.assertEquals(protocolId, protocolConnectionData.getProtocolId());
        Assert.assertEquals(protocolVersion, protocolConnectionData.getProtocolVersion());
        Assert.assertNotNull(protocolConnectionData.toString());
    }
}
