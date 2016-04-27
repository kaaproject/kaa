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

package org.kaaproject.kaa.server.sync;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class SyncEqualsHashCodeTest {
    @Test
    public void logEntryEqualsHashCodeTest() {
        LogEntry entry1 = getLogEntry(10);
        LogEntry entry2 = getLogEntry(9);
        Object[] entries = new Object[]{getLogEntry(8)};
        EqualsVerifier.forExamples(entry1, entry2, entries).verify();
    }

    @Test
    public void topicStateEqualsHashCodeTest() {
        EqualsVerifier.forClass(TopicState.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void clientSyncMetaDataEqualsHashCodeTest() {
        ClientSyncMetaData clientSyncMetaData1 =  new ClientSyncMetaData("token1", null, null, null, 10L);
        ClientSyncMetaData clientSyncMetaData2 =  new ClientSyncMetaData("token1", null, null, null, 10L);
        ClientSyncMetaData clientSyncMetaData3 = new ClientSyncMetaData("token3", null, null, null, 12L);
        Assert.assertEquals(clientSyncMetaData1, clientSyncMetaData1);
        Assert.assertNotEquals(clientSyncMetaData1, clientSyncMetaData3);
        Assert.assertNotEquals(clientSyncMetaData1, null);
        Assert.assertNotEquals(null, clientSyncMetaData1);
        Assert.assertEquals(clientSyncMetaData1, clientSyncMetaData2);
        Assert.assertNotEquals(clientSyncMetaData1, new Object());
        Assert.assertEquals(clientSyncMetaData1.hashCode(), clientSyncMetaData2.hashCode());
        Assert.assertEquals(clientSyncMetaData1.hashCode(), clientSyncMetaData1.hashCode());
        Assert.assertNotEquals(clientSyncMetaData1.hashCode(), clientSyncMetaData3.hashCode());
    }

    @Test
    public void logClientSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(LogClientSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void eventListenersResponseEqualsHashCodeTest() {
        EqualsVerifier.forClass(EventListenersResponse.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void notificationEqualsHashCodeTest() {
        EqualsVerifier.forClass(Notification.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void eventSequenceNumberResponseEqualsHashCodeTest() {
        EqualsVerifier.forClass(EventSequenceNumberResponse.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void eventEqualsHashCodeTest() {
        EqualsVerifier.forClass(Event.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void eventListenersRequestEqualsHashCodeTest() {
        EqualsVerifier.forClass(EventListenersRequest.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void endpointAttachRequestEqualsHashCodeTest() {
        EqualsVerifier.forClass(EndpointAttachRequest.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void endpointDetachRequestEqualsHashCodeTest() {
        EqualsVerifier.forClass(EndpointDetachRequest.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void userClientSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(UserClientSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void userAttachRequestEqualsHashCodeTest() {
        EqualsVerifier.forClass(UserAttachRequest.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void topicEqualsHashCodeTest() {
        EqualsVerifier.forClass(Topic.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void subscriptionCommandEqualsHashCodeTest() {
        EqualsVerifier.forClass(SubscriptionCommand.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void eventClientSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(EventClientSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void notificationClientSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(NotificationClientSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void configurationClientSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(ConfigurationClientSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void profileClientSyncEqualsHashCodeTest() {
        ProfileClientSync profileClientSync1 = new ProfileClientSync(null, null, null, "token1");
        ProfileClientSync profileClientSync2 = new ProfileClientSync(null, null, null, "token1");
        ProfileClientSync profileClientSync3 = new ProfileClientSync(null, null, null, "token3");
        Assert.assertEquals(profileClientSync1, profileClientSync1);
        Assert.assertEquals(profileClientSync1, profileClientSync2);
        Assert.assertNotEquals(profileClientSync1, null);
        Assert.assertNotEquals(null, profileClientSync1);
        Assert.assertNotEquals(profileClientSync1, profileClientSync3);
        Assert.assertNotEquals(profileClientSync1, new Object());
        Assert.assertEquals(profileClientSync1.hashCode(), profileClientSync2.hashCode());
        Assert.assertEquals(profileClientSync1.hashCode(), profileClientSync1.hashCode());
        Assert.assertNotEquals(profileClientSync1.hashCode(), profileClientSync3.hashCode());
    }

    @Test
    public void clientSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(ClientSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void userAttachNotificationEqualsHashCodeTest() {
        EqualsVerifier.forClass(UserAttachNotification.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void endpointAttachResponseEqualsHashCodeTest() {
        EqualsVerifier.forClass(EndpointAttachResponse.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void redirectServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(RedirectServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void userAttachResponseEqualsHashCodeTest() {
        EqualsVerifier.forClass(UserAttachResponse.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void logServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(LogServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void userDetachNotificationEqualsHashCodeTest() {
        EqualsVerifier.forClass(UserDetachNotification.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void logDeliveryStatusEqualsHashCodeTest() {
        EqualsVerifier.forClass(LogDeliveryStatus.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void userServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(UserServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void endpointDetachResponseEqualsHashCodeTest() {
        EqualsVerifier.forClass(EndpointDetachResponse.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void profileServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(ProfileServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void notificationServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(NotificationServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void configurationServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(ConfigurationServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void serverSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(ServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    @Test
    public void eventServerSyncEqualsHashCodeTest() {
        EqualsVerifier.forClass(EventServerSync.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

    private LogEntry getLogEntry(int buffSize) {
        return new LogEntry(ByteBuffer.allocate(buffSize));
    }
}
