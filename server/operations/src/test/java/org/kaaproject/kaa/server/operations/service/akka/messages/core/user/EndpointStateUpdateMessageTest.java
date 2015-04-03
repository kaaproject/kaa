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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

public class EndpointStateUpdateMessageTest {

    @Test
    public void testEndpointStateUpdateMessage(){
        String tenantId = "tid";
        String userId = "uid";
        String applicationToken = "token";
        byte[] hash = "hash".getBytes();
        EndpointObjectHash key = EndpointObjectHash.fromBytes(hash);

        EndpointUserConfigurationUpdate update = new EndpointUserConfigurationUpdate(tenantId, userId, applicationToken, key, hash);

        StringBuilder builder = new StringBuilder();
        builder.append("EndpointStateUpdateMessage [update=");
        builder.append(update);
        builder.append("]");

        EndpointStateUpdateMessage message = new EndpointStateUpdateMessage(update);

        Assert.assertEquals(tenantId, message.getTenantId());
        Assert.assertEquals(userId, update.getUserId());
        Assert.assertEquals(builder.toString(), message.toString());
        Assert.assertEquals(update, message.getUpdate());
        Assert.assertEquals(userId, message.getUserId());
    }
}
