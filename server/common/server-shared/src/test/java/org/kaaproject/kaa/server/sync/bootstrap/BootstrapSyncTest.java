/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BootstrapSyncTest {

    @Test
    public void bootstrapServerSyncTest() {
        int requestId = 10;
        Set<ProtocolConnectionData> protocolList = Collections.EMPTY_SET;
        BootstrapServerSync bootstrapServerSync = new BootstrapServerSync(requestId, protocolList);
        Assert.assertEquals(requestId, bootstrapServerSync.getRequestId());
        Assert.assertEquals(protocolList, bootstrapServerSync.getProtocolList());
    }

    @Test
    public void bootstrapClientSyncTest() {
        int requestId = 10;
        List<ProtocolVersionId> protocolVersionIds = Collections.emptyList();
        BootstrapClientSync clientSync = new BootstrapClientSync(requestId, protocolVersionIds);
        Assert.assertEquals(requestId, clientSync.getRequestId());
        Assert.assertEquals(protocolVersionIds, clientSync.getKeys());
        Assert.assertNotNull(clientSync.toString());
    }


}
