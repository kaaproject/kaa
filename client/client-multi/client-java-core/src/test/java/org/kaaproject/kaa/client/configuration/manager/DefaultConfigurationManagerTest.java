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

package org.kaaproject.kaa.client.configuration.manager;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.junit.Test;
import org.kaaproject.kaa.client.common.CommonRecord;
import org.kaaproject.kaa.client.common.CommonValue;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationReceiver;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

public class DefaultConfigurationManagerTest {

    public static Schema getArraySchema(GenericRecord delta, String field) {
        List<Schema> fieldTypes = delta.getSchema().getField(field).schema().getTypes();
        for (Schema type : fieldTypes) {
            if (type.getType() == Schema.Type.ARRAY) {
                return type;
            }
        }
        return null;
    }

    public static Schema getDeltaSchemaByFullName(Schema deltaSchema, String fullName) {
        Schema deltaT = deltaSchema.getElementType();
        Schema deltaUnion = deltaT.getField("delta").schema();
        List<Schema> deltas = deltaUnion.getTypes();
        for (Schema delta : deltas) {
            if (delta.getFullName().equals(fullName)) {
                return delta;
            }
        }
        return null;
    }

    public static Schema getSchemaByFullName(List<Schema> types, String fullName) {
        for (Schema type : types) {
            if (type.getFullName().equals(fullName)) {
                return type;
            }
        }
        return null;
    }

    public static void fillComplexFullResyncDelta(GenericRecord delta) {
        GenericRecord testField2 = new GenericData.Record(getSchemaByFullName(
                delta.getSchema().getField("testField2").schema().getTypes(),
                "org.kaa.config.testRecordT"));
        testField2.put("testField3", 456);
        byte [] rawUuid = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        GenericFixed uuid = new GenericData.Fixed(delta.getSchema()
                .getField("__uuid").schema(), rawUuid);
        delta.put("testField1", "abc");
        delta.put("testField2", testField2);
        delta.put("__uuid", uuid);
    }

    public static void fillComplexPartialDelta(GenericRecord delta) {
        GenericRecord testField22 = new GenericData.Record(getSchemaByFullName(
                delta.getSchema().getField("testField2").schema().getTypes(),
                "org.kaa.config.testRecordT"));
        testField22.put("testField3", 654);
        GenericEnumSymbol unchanged = new GenericData.EnumSymbol(
                getSchemaByFullName(delta.getSchema().getField("testField1")
                        .schema().getTypes(), "org.kaaproject.configuration.unchangedT"),
                "unchanged");
        delta.put("testField1", unchanged);
        delta.put("testField2", testField22);
    }

    @Test
    public void testComplexDelta() throws IOException  {
        DefaultConfigurationManager manager = new DefaultConfigurationManager();
        ConfigurationReceiver receiver = mock(ConfigurationReceiver.class);
        manager.subscribeForConfigurationUpdates(receiver);

        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/complexFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        // First full resync delta
        GenericRecord delta = new GenericData.Record(getDeltaSchemaByFullName(schema, "org.kaa.config.testT"));
        fillComplexFullResyncDelta(delta);

        manager.onDeltaReceived(0, delta, true);
        CommonRecord rootConfig = manager.getConfiguration();

        manager.onConfigurationProcessed();

        assertTrue(rootConfig.hasField("testField1"));
        assertEquals("abc", rootConfig.getField("testField1").getString());
        assertTrue(rootConfig.getField("testField1").isString());
        assertFalse(rootConfig.getField("testField1").isInteger());

        assertTrue(rootConfig.hasField("testField2"));
        assertTrue(rootConfig.getField("testField2").isRecord());
        assertTrue(rootConfig.getField("testField2").getRecord().getField("testField3").isInteger());
        assertEquals(new Integer(456), rootConfig.getField("testField2").getRecord().getField("testField3").getInteger());

        // Partial update delta
        fillComplexPartialDelta(delta);

        manager.onDeltaReceived(0, delta, false);
        rootConfig = manager.getConfiguration();

        manager.onConfigurationProcessed();
        verify(receiver, times(2)).onConfigurationUpdated(any(CommonRecord.class));

        assertTrue(rootConfig.hasField("testField1"));
        assertEquals("abc", rootConfig.getField("testField1").getString());
        assertTrue(rootConfig.getField("testField1").isString());

        assertTrue(rootConfig.hasField("testField2"));
        assertTrue(rootConfig.getField("testField2").isRecord());
        assertTrue(rootConfig.getField("testField2").getRecord().getField("testField3").isInteger());
        assertEquals(new Integer(654), rootConfig.getField("testField2").getRecord().getField("testField3").getInteger());
    }

    public static void fillArrayFullResyncDelta(GenericRecord delta) {
        byte [] rawUuid = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        GenericFixed uuid = new GenericData.Fixed(delta.getSchema()
                .getField("__uuid").schema(), rawUuid);
        delta.put("__uuid", uuid);

        GenericArray testField1 = new GenericData.Array(3, getArraySchema(delta, "testField1"));
        delta.put("testField1", testField1);

        GenericRecord itemRecord1 = new GenericData.Record(getSchemaByFullName(
                testField1.getSchema().getElementType().getTypes(),
                "org.kaa.config.testRecordItemT"));
        itemRecord1.put("testField2", 1);
        byte [] rawItemUuid1 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        GenericFixed itemUuid1 = new GenericData.Fixed(itemRecord1
                .getSchema().getField("__uuid").schema(), rawItemUuid1);
        itemRecord1.put("__uuid", itemUuid1);

        GenericRecord itemRecord2 = new GenericData.Record(getSchemaByFullName(
                testField1.getSchema().getElementType().getTypes(),
                "org.kaa.config.testRecordItemT"));
        itemRecord2.put("testField2", 2);
        byte [] rawItemUuid2 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
        GenericFixed itemUuid2 = new GenericData.Fixed(itemRecord2
                .getSchema().getField("__uuid").schema(), rawItemUuid2);
        itemRecord2.put("__uuid", itemUuid2);

        GenericRecord itemRecord3 = new GenericData.Record(getSchemaByFullName(
                testField1.getSchema().getElementType().getTypes(),
                "org.kaa.config.testRecordItemT"));
        itemRecord3.put("testField2", 3);
        byte [] rawItemUuid3 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3};
        GenericFixed itemUuid3 = new GenericData.Fixed(itemRecord3
                .getSchema().getField("__uuid").schema(), rawItemUuid3);
        itemRecord3.put("__uuid", itemUuid3);

        testField1.add(itemRecord1);
        testField1.add(itemRecord2);
        testField1.add(itemRecord3);
    }

    public static void fillArrayItemUpdateDelta(GenericRecord item) {
        byte [] rawItemUuid2 = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2};
        GenericFixed itemUuid2 = new GenericData.Fixed(item
                .getSchema().getField("__uuid").schema(), rawItemUuid2);
        item.put("__uuid", itemUuid2);
        item.put("testField2", 22);
    }

    public static void fillArrayItemRemoveDelta(GenericRecord delta) {
        GenericArray testField1 = new GenericData.Array(1, getArraySchema(delta, "testField1"));

        byte [] rawUuidToRemove = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        GenericFixed uuidToRemove = new GenericData.Fixed(getSchemaByFullName(
                testField1.getSchema().getElementType().getTypes(),
                "org.kaaproject.configuration.uuidT"), rawUuidToRemove);
        testField1.add(uuidToRemove);
        delta.put("testField1", testField1);
    }

    public static void fillArrayResetDelta(GenericRecord delta) {
        GenericEnumSymbol reset = new GenericData.EnumSymbol(
                getSchemaByFullName(delta.getSchema().getField("testField1")
                        .schema().getTypes(), "org.kaaproject.configuration.resetT"), "reset");
        delta.put("testField1", reset);
    }

    @Test
    public void testArrayFieldsDelta() throws IOException, URISyntaxException {
        DefaultConfigurationManager manager = new DefaultConfigurationManager();
        ConfigurationReceiver receiver = mock(ConfigurationReceiver.class);
        manager.subscribeForConfigurationUpdates(receiver);

        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("configuration/manager/arrayFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        // First full resync delta
        GenericRecord delta = new GenericData.Record(getDeltaSchemaByFullName(schema, "org.kaa.config.testT"));
        fillArrayFullResyncDelta(delta);

        manager.onDeltaReceived(0, delta, true);
        manager.onConfigurationProcessed();

        CommonRecord rootConfig = manager.getConfiguration();

        List<CommonValue> configArray = rootConfig.getField("testField1").getArray().getList();
        assertTrue(rootConfig.getField("testField1").isArray());
        assertEquals(3, configArray.size());

        assertEquals(new Integer(1), configArray.get(0).getRecord().getField("testField2").getInteger());
        assertEquals(new Integer(2), configArray.get(1).getRecord().getField("testField2").getInteger());
        assertEquals(new Integer(3), configArray.get(2).getRecord().getField("testField2").getInteger());

        // Partial update of the item
        GenericRecord itemRecord2 = new GenericData.Record(getDeltaSchemaByFullName(schema, "org.kaa.config.testRecordItemT"));
        fillArrayItemUpdateDelta(itemRecord2);

        manager.onDeltaReceived(1, itemRecord2, false);
        manager.onConfigurationProcessed();

        rootConfig = manager.getConfiguration();
        configArray = rootConfig.getField("testField1").getArray().getList();

        assertEquals(new Integer(1), configArray.get(0).getRecord().getField("testField2").getInteger());
        assertEquals(new Integer(22), configArray.get(1).getRecord().getField("testField2").getInteger());
        assertEquals(new Integer(3), configArray.get(2).getRecord().getField("testField2").getInteger());

        // Removing one item by uuid
        fillArrayItemRemoveDelta(delta);

        manager.onDeltaReceived(0, delta, false);
        manager.onConfigurationProcessed();

        rootConfig = manager.getConfiguration();
        configArray = rootConfig.getField("testField1").getArray().getList();

        assertEquals(2, configArray.size());

        assertEquals(new Integer(22), configArray.get(0).getRecord().getField("testField2").getInteger());
        assertEquals(new Integer(3), configArray.get(1).getRecord().getField("testField2").getInteger());

        // Reseting container
        fillArrayResetDelta(delta);

        manager.onDeltaReceived(0, delta, false);
        manager.onConfigurationProcessed();

        rootConfig = manager.getConfiguration();
        configArray = rootConfig.getField("testField1").getArray().getList();

        assertTrue(configArray.isEmpty());
        verify(receiver, times(4)).onConfigurationUpdated(any(CommonRecord.class));
    }
}
