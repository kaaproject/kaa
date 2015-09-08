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

import java.util.Arrays;

public class UserConfigurationUpdateTest {

    @Test
    public void testUserConfigurationUpdate() {
        String tenantId = "tid";
        String userId = "uid";
        String applicationToken = "token";
        byte[] hash = "hash".getBytes();
        int schemaVersion = 1;

        StringBuilder builder = new StringBuilder();
        builder.append("UserConfigurationUpdate [tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", applicationToken=");
        builder.append(applicationToken);
        builder.append(", schemaVersion=");
        builder.append(schemaVersion);
        builder.append(", hash=");
        builder.append(Arrays.toString(hash));
        builder.append("]");

        UserConfigurationUpdate update = new UserConfigurationUpdate(tenantId, userId, applicationToken, schemaVersion, hash);

        Assert.assertEquals(tenantId, update.getTenantId());
        Assert.assertEquals(userId, update.getUserId());
        Assert.assertEquals(applicationToken, update.getApplicationToken());
        Assert.assertEquals(hash, update.getHash());
        Assert.assertEquals(applicationToken, update.getApplicationToken());
        Assert.assertEquals(builder.toString(), update.toString());

    }

}
