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

public class UserConfigurationUpdateMessageTest {

    @Test
    public void testUserConfigurationUpdateMessage() {
        String tenantId = "tid";
        String userId = "uid";
        String applicationToken = "token";
        byte[] hash = "hash".getBytes();
        int schemaVersion = 1;

        UserConfigurationUpdate update = new UserConfigurationUpdate(tenantId, userId, applicationToken, schemaVersion, hash);

        StringBuilder builder = new StringBuilder();
        builder.append("UserConfigurationUpdateMessage [update=");
        builder.append(update);
        builder.append("]");

        UserConfigurationUpdateMessage updateMessage = new UserConfigurationUpdateMessage(update);

        Assert.assertEquals(tenantId, updateMessage.getTenantId());
        Assert.assertEquals(userId, updateMessage.getUserId());
        Assert.assertEquals(update, updateMessage.getUpdate());
        Assert.assertEquals(builder.toString(), updateMessage.toString());

    }

}
