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

package org.kaaproject.kaa.server.common.nosql.mongo.dao.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

public class MongoEndpointUserConfigurationTest {
    @Test
    public void hashCodeEqualsTest() {
        EqualsVerifier.forClass(MongoEndpointConfiguration.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void getSetTest() {
        MongoEndpointUserConfiguration mongoEndpointUserConfiguration = new MongoEndpointUserConfiguration();
        String appToken = "appToken";
        String body = "body";
        int schemaVersion = 2;
        String userId = "userId";
        mongoEndpointUserConfiguration.setAppToken(appToken);
        mongoEndpointUserConfiguration.setBody(body);
        mongoEndpointUserConfiguration.setSchemaVersion(schemaVersion);
        mongoEndpointUserConfiguration.setUserId(userId);
        Assert.assertEquals(appToken, mongoEndpointUserConfiguration.getAppToken());
        Assert.assertEquals(body, mongoEndpointUserConfiguration.getBody());
        Assert.assertTrue(schemaVersion == mongoEndpointUserConfiguration.getSchemaVersion());
        Assert.assertEquals(userId, mongoEndpointUserConfiguration.getUserId());
    }
}
