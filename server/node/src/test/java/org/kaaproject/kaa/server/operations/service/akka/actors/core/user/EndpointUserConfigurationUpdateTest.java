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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.user;


import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.EndpointUserConfigurationUpdate;

import java.util.Arrays;

public class EndpointUserConfigurationUpdateTest {

    @Test
    public void testEndpointUserConfigurationUpdate() {
        String tenantId = "tid";
        String userId = "uid";
        String applicationToken = "token";
        byte[] hash = "hash".getBytes();
        EndpointObjectHash key = EndpointObjectHash.fromBytes(hash);

        StringBuilder builder = new StringBuilder();
        builder.append("EndpointUserConfigurationUpdate [tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", applicationToken=");
        builder.append(applicationToken);
        builder.append(", key=");
        builder.append(key);
        builder.append(", hash=");
        builder.append(Arrays.toString(hash));
        builder.append("]");

        EndpointUserConfigurationUpdate update = new EndpointUserConfigurationUpdate(tenantId, userId, applicationToken, key, hash);

        Assert.assertEquals(tenantId, update.getTenantId());
        Assert.assertEquals(userId, update.getUserId());
        Assert.assertEquals(applicationToken, update.getApplicationToken());
        Assert.assertEquals(hash, update.getHash());
        Assert.assertEquals(key, update.getKey());
        Assert.assertEquals(builder.toString(), update.toString());
    }
}
