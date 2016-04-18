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

public class EndpointProfileTest {

    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(MongoEndpointProfile.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void getSetTest(){
        MongoEndpointProfile profile = new MongoEndpointProfile();

        profile.setAccessToken(null);
        profile.setGroupState(null);
        profile.setChangedFlag(null);
        profile.setConfigurationHash(null);
        profile.setEcfVersionStates(null);
        profile.setEndpointKey(null);
        profile.setEndpointKeyHash(null);
        profile.setEndpointUserId(null);
        profile.setEcfVersionStates(null);
        profile.setId(null);
        profile.setTopicHash(null);
        profile.setProfileHash(null);
        profile.setServerHash(null);
        profile.setSubscriptions(null);
        profile.setUserConfigurationHash(null);

        profile.setSequenceNumber(1);
        profile.setConfigurationVersion(1);
        profile.setLogSchemaVersion(1);
        profile.setNotificationVersion(1);
        profile.setProfileVersion(1);
        profile.setSystemNfVersion(1);
        profile.setUserNfVersion(1);

        Assert.assertNull(profile.getAccessToken());
        Assert.assertNull(profile.getGroupState());
        Assert.assertNull(profile.getChangedFlag());
        Assert.assertNull(profile.getConfigurationHash());
        Assert.assertNull(profile.getEcfVersionStates());
        Assert.assertNull(profile.getEndpointKey());
        Assert.assertNull(profile.getEndpointKeyHash());
        Assert.assertNull(profile.getEndpointUserId());
        Assert.assertNull(profile.getEcfVersionStates());
        Assert.assertNull(profile.getId());
        Assert.assertNull(profile.getTopicHash());
        Assert.assertNull(profile.getProfileHash());
        Assert.assertNull(profile.getServerHash());
        Assert.assertNull(profile.getSubscriptions());
        Assert.assertNull(profile.getUserConfigurationHash());

        Assert.assertEquals(1,profile.getSequenceNumber());
        Assert.assertEquals(1,profile.getConfigurationVersion());
        Assert.assertEquals(1,profile.getLogSchemaVersion());
        Assert.assertEquals(1,profile.getNotificationVersion());
        Assert.assertEquals(1,profile.getProfileVersion());
        Assert.assertEquals(1,profile.getSystemNfVersion());
        Assert.assertEquals(1,profile.getUserNfVersion());
    }
}
