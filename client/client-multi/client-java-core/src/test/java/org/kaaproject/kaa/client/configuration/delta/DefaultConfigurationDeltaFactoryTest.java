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

package org.kaaproject.kaa.client.configuration.delta;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;
import org.kaaproject.kaa.client.configuration.delta.ConfigurationDelta;
import org.kaaproject.kaa.client.configuration.delta.ConfigurationDeltaFactory;
import org.kaaproject.kaa.client.configuration.delta.DefaultConfigurationDeltaFactory;
import org.kaaproject.kaa.client.configuration.delta.DeltaHandlerId;
import org.kaaproject.kaa.client.configuration.delta.DeltaType;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManagerTest;

public class DefaultConfigurationDeltaFactoryTest {

    @Test
    public void testComplexDelta() throws IOException  {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        // First full resync delta
        GenericRecord delta = new GenericData.Record(DefaultConfigurationManagerTest.getDeltaSchemaByFullName(schema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillComplexFullResyncDelta(delta);

        ConfigurationDeltaFactory factory = new DefaultConfigurationDeltaFactory();
        ConfigurationDelta result = factory.createDelta(delta);

        assertTrue(result.hasChanged("testField1"));
        assertTrue(result.hasChanged("testField2"));

        DeltaType testField1 = result.getDeltaType("testField1");
        assertFalse(testField1.isDefault());
        assertFalse(testField1.isReset());
        assertNull(testField1.getAddedItems());
        assertNull(testField1.getRemovedItems());
        CharSequence abc = (CharSequence) testField1.getNewValue();
        assertEquals("abc", abc);

        DeltaType testField2 = result.getDeltaType("testField2");
        assertFalse(testField2.isDefault());
        assertFalse(testField2.isReset());
        assertNull(testField2.getAddedItems());
        assertNull(testField2.getRemovedItems());
        ConfigurationDelta testField2Delta = (ConfigurationDelta) testField2.getNewValue();
        assertTrue(testField2Delta.hasChanged("testField3"));

        DeltaType testField3 = testField2Delta.getDeltaType("testField3");
        assertFalse(testField3.isDefault());
        assertFalse(testField3.isReset());
        assertNull(testField3.getAddedItems());
        assertNull(testField3.getRemovedItems());
        Integer testField3Value = (Integer) testField3.getNewValue();
        assertEquals(new Integer(456), testField3Value);
        // Partial update delta
        DefaultConfigurationManagerTest.fillComplexPartialDelta(delta);
        result = factory.createDelta(delta);

        assertFalse(result.hasChanged("testField1"));
        assertTrue(result.hasChanged("testField2"));

        testField2 = result.getDeltaType("testField2");
        testField2Delta = (ConfigurationDelta) testField2.getNewValue();
        assertTrue(testField2Delta.hasChanged("testField3"));

        testField3 = testField2Delta.getDeltaType("testField3");
        testField3Value = (Integer) testField3.getNewValue();
        assertEquals(new Integer(654), testField3Value);
    }

    @Test
    public void testArrayFieldsDelta() throws IOException, URISyntaxException {
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/arrayFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        // First full resync delta
        GenericRecord delta = new GenericData.Record(DefaultConfigurationManagerTest.getDeltaSchemaByFullName(schema, "org.kaa.config.testT"));
        DefaultConfigurationManagerTest.fillArrayFullResyncDelta(delta);

        ConfigurationDeltaFactory factory = new DefaultConfigurationDeltaFactory();
        ConfigurationDelta result = factory.createDelta(delta);

        assertTrue(result.hasChanged("testField1"));

        DeltaType testField1 = result.getDeltaType("testField1");
        assertFalse(testField1.isDefault());
        assertFalse(testField1.isReset());
        assertNull(testField1.getNewValue());
        assertNull(testField1.getRemovedItems());

        List<Object> array = testField1.getAddedItems();
        ConfigurationDelta item1 = (ConfigurationDelta) array.get(0);
        Integer item1Value = (Integer) item1.getDeltaType("testField2").getNewValue();
        assertEquals(new Integer(1), item1Value);

        ConfigurationDelta item2 = (ConfigurationDelta) array.get(1);
        Integer item2Value = (Integer) item2.getDeltaType("testField2").getNewValue();
        assertEquals(new Integer(2), item2Value);

        ConfigurationDelta item3 = (ConfigurationDelta) array.get(2);
        Integer item3Value = (Integer) item3.getDeltaType("testField2").getNewValue();
        assertEquals(new Integer(3), item3Value);

        // Partial update of the item
        GenericRecord itemRecord2 = new GenericData.Record(DefaultConfigurationManagerTest.getDeltaSchemaByFullName(schema, "org.kaa.config.testRecordItemT"));
        DefaultConfigurationManagerTest.fillArrayItemUpdateDelta(itemRecord2);

        result = factory.createDelta(itemRecord2);
        assertTrue(result.hasChanged("testField2"));

        Integer testField2 = (Integer) result.getDeltaType("testField2").getNewValue();
        assertEquals(new Integer(22), testField2);

        // Removing one item by uuid
        DefaultConfigurationManagerTest.fillArrayItemRemoveDelta(delta);
        result = factory.createDelta(delta);

        testField1 = result.getDeltaType("testField1");
        assertFalse(testField1.isDefault());
        assertFalse(testField1.isReset());
        assertNull(testField1.getNewValue());
        assertNull(testField1.getAddedItems());

        List<DeltaHandlerId> handlers = testField1.getRemovedItems();
        assertNotNull(handlers);
        assertEquals(1, handlers.size());

        // Reseting container
        DefaultConfigurationManagerTest.fillArrayResetDelta(delta);
        result = factory.createDelta(delta);

        testField1 = result.getDeltaType("testField1");
        assertFalse(testField1.isDefault());
        assertTrue(testField1.isReset());
        assertNull(testField1.getNewValue());
        assertNull(testField1.getAddedItems());
        assertNull(testField1.getRemovedItems());
    }
}
