/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.client.configuration.delta.manager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import nl.jqno.equalsverifier.EqualsVerifier;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;
import org.kaaproject.kaa.client.configuration.delta.ConfigurationDelta;
import org.kaaproject.kaa.client.configuration.delta.DeltaHandlerId;
import org.kaaproject.kaa.client.configuration.delta.manager.DefaultDeltaManager;
import org.kaaproject.kaa.client.configuration.delta.manager.DeltaReceiver;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManagerTest;

public class DefaultDeltaManagerTest {
    DeltaHandlerId itemId;

    class TestRootReceiver implements DeltaReceiver {

        @Override
        public void loadDelta(ConfigurationDelta delta) {
            List<Object> array = delta.getDeltaType("testField1").getAddedItems();
            ConfigurationDelta item2 = (ConfigurationDelta) array.get(1);
            itemId = item2.getHandlerId();
        }
    }

    @Test
    public void testSubscriptions() throws IOException {
        DefaultDeltaManager manager = new DefaultDeltaManager();
        DeltaReceiver rootReceiver = new TestRootReceiver();
        manager.registerRootReceiver(rootReceiver);

        // First full resync delta
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/arrayFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));
        GenericRecord delta = new GenericData.Record(DefaultConfigurationManagerTest.getDeltaSchemaByFullName(schema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillArrayFullResyncDelta(delta);
        manager.onDeltaReceived(0, delta, true);

        assertNotNull(itemId);
        DeltaReceiver receiver = mock(DeltaReceiver.class);
        manager.subscribeForDeltaUpdates(itemId, receiver);

        // Partial update of the item
        GenericRecord itemRecord2 = new GenericData.Record(DefaultConfigurationManagerTest.getDeltaSchemaByFullName(schema, "org.kaa.config.testRecordItemT"));
        DefaultConfigurationManagerTest.fillArrayItemUpdateDelta(itemRecord2);
        manager.onDeltaReceived(1, itemRecord2, false);

        manager.unsubscribeFromDeltaUpdates(itemId);
        manager.onDeltaReceived(1, itemRecord2, false);

        verify(receiver, times(1)).loadDelta(any(ConfigurationDelta.class));
    }

    @Test
    public void testHandlerId() {
        DeltaHandlerId handler = new DeltaHandlerId(12345);
        assertEquals(0, handler.compareTo(new DeltaHandlerId(12345)));
        assertEquals("12345", handler.toString());
        EqualsVerifier.forClass(DeltaHandlerId.class).verify();
    }
}
