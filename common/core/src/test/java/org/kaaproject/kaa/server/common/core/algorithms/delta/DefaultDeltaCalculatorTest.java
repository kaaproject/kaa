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

package org.kaaproject.kaa.server.common.core.algorithms.delta;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.apache.commons.compress.utils.IOUtils;
import org.junit.Test;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;

public class DefaultDeltaCalculatorTest {

    public static final String NEW_COMPLEX_CONFIG = "delta/complexFieldsDeltaNew.json";
    public static final String OLD_COMPLEX_CONFIG = "delta/complexFieldsDeltaCurrent.json";
    public static final String COMPLEX_SCHEMA = "delta/complexFieldsDeltaSchema.json";
    public static final String COMPLEX_PROTOCOL_SCHEMA = "delta/complexFieldsDeltaProtocolSchema.json";

    private static Schema getArraySchema(GenericRecord delta, String field) {
        List<Schema> fieldTypes = delta.getSchema().getField(field).schema().getTypes();
        for (Schema type : fieldTypes) {
            if (type.getType() == Schema.Type.ARRAY) {
                return type;
            }
        }
        return null;
    }

    private static Schema getDeltaSchemaByFullName(Schema deltaSchema, String fullName) {
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

    private static Schema getSchemaByFullName(List<Schema> types, String fullName) {
        for (Schema type : types) {
            if (type.getFullName().equals(fullName)) {
                return type;
            }
        }
        return null;
    }

    @Test
    public void testPrimitiveFieldsDelta() throws IOException, DeltaCalculatorException {
        URL protocolSchemaUrl = Thread.currentThread().getContextClassLoader().getResource("delta/primitiveFieldsDeltaProtocolSchema.json");
        Schema protocolSchema = new Schema.Parser().parse(new File(protocolSchemaUrl.getPath()));
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("delta/primitiveFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));
        DeltaCalculationAlgorithm calculator = new DefaultDeltaCalculationAlgorithm(protocolSchema, schema);

        ByteArrayOutputStream baosOld = new ByteArrayOutputStream();
        URL oldConfigUrl = Thread.currentThread().getContextClassLoader().getResource("delta/primitiveFieldsDeltaCurrent.json");
        IOUtils.copy(new FileInputStream(oldConfigUrl.getPath()), baosOld, 1024);
        String oldStr = new String(baosOld.toByteArray(), "UTF-8");

        ByteArrayOutputStream baosNew = new ByteArrayOutputStream();
        URL newConfigUrl = Thread.currentThread().getContextClassLoader().getResource("delta/primitiveFieldsDeltaNew.json");
        IOUtils.copy(new FileInputStream(newConfigUrl.getPath()), baosNew, 1024);
        String newStr = new String(baosNew.toByteArray(), "UTF-8");

        BaseData oldData = new BaseData(new BaseSchema(schema.toString()), oldStr);
        BaseData newData = new BaseData(new BaseSchema(schema.toString()), newStr);
        RawBinaryDelta deltaResult = calculator.calculate(oldData, newData);

        GenericRecord delta = new GenericData.Record(getDeltaSchemaByFullName(protocolSchema, "org.kaa.config.testT"));
        GenericEnumSymbol unchanged = new GenericData.EnumSymbol(getSchemaByFullName(delta.getSchema().getField("testField1").schema().getTypes(),
                "org.kaaproject.configuration.unchangedT"), "unchanged");
        byte[] rawUuid = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        GenericFixed uuid = new GenericData.Fixed(delta.getSchema().getField("__uuid").schema(), rawUuid);
        delta.put("testField1", unchanged);
        delta.put("testField2", 456);
        delta.put("__uuid", uuid);

        AvroBinaryDelta deltaExpected = new AvroBinaryDelta(protocolSchema);
        deltaExpected.addDelta(delta);

        assertArrayEquals(deltaExpected.getData(), deltaResult.getData());
    }

    @Test
    public void testComplexFieldsDelta() throws IOException, URISyntaxException, DeltaCalculatorException {
        URL protocolSchemaUrl = Thread.currentThread().getContextClassLoader().getResource(COMPLEX_PROTOCOL_SCHEMA);
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(COMPLEX_SCHEMA);
        Schema protocolSchema = new Schema.Parser().parse(new File(protocolSchemaUrl.getPath()));
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));
        DeltaCalculationAlgorithm calculator = new DefaultDeltaCalculationAlgorithm(protocolSchema, schema);

        ByteArrayOutputStream baosOld = new ByteArrayOutputStream();
        URL oldConfigUrl = Thread.currentThread().getContextClassLoader().getResource(OLD_COMPLEX_CONFIG);
        IOUtils.copy(new FileInputStream(oldConfigUrl.getPath()), baosOld, 1024);
        String oldStr = new String(baosOld.toByteArray(), "UTF-8");

        ByteArrayOutputStream baosNew = new ByteArrayOutputStream();
        URL newConfigUrl = Thread.currentThread().getContextClassLoader().getResource(NEW_COMPLEX_CONFIG);
        IOUtils.copy(new FileInputStream(newConfigUrl.getPath()), baosNew, 1024);
        String newStr = new String(baosNew.toByteArray(), "UTF-8");

        BaseData oldData = new BaseData(new BaseSchema(schema.toString()), oldStr);
        BaseData newData = new BaseData(new BaseSchema(schema.toString()), newStr);
        RawBinaryDelta deltaResult = calculator.calculate(oldData, newData);

        AvroBinaryDelta deltaExpected = getComplexFieldDelta(protocolSchema);

        assertTrue(deltaResult.hasChanges());
        assertArrayEquals(deltaExpected.getData(), deltaResult.getData());
        assertTrue(deltaResult.hasChanges());
    }

    public static AvroBinaryDelta getComplexFieldDelta(Schema schema) {
        GenericRecord delta = new GenericData.Record(getDeltaSchemaByFullName(schema, "org.kaa.config.testT"));
        GenericEnumSymbol unchanged = new GenericData.EnumSymbol(getSchemaByFullName(delta.getSchema().getField("testField1").schema().getTypes(),
                "org.kaaproject.configuration.unchangedT"), "unchanged");
        GenericRecord testField2 = new GenericData.Record(getSchemaByFullName(delta.getSchema().getField("testField2").schema().getTypes(),
                "org.kaa.config.testRecordT"));
        testField2.put("testField3", 456);
        byte[] rawUuid = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        GenericFixed uuid = new GenericData.Fixed(delta.getSchema().getField("__uuid").schema(), rawUuid);
        delta.put("testField1", unchanged);
        delta.put("testField2", testField2);
        delta.put("__uuid", uuid);

        AvroBinaryDelta deltaExpected = new AvroBinaryDelta(schema);
        deltaExpected.addDelta(delta);
        return deltaExpected;
    }

    @Test
    public void testArrayFieldsDelta() throws IOException, URISyntaxException, DeltaCalculatorException {
        URL protocolSchemaUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayFieldsDeltaProtocolSchema.json");
        Schema protocolSchema = new Schema.Parser().parse(new File(protocolSchemaUrl.getPath()));
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));

        DeltaCalculationAlgorithm calculator = new DefaultDeltaCalculationAlgorithm(protocolSchema, schema);

        ByteArrayOutputStream baosOld = new ByteArrayOutputStream();
        URL oldConfigUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayFieldsDeltaCurrent.json");
        IOUtils.copy(new FileInputStream(oldConfigUrl.getPath()), baosOld, 1024);
        String oldStr = new String(baosOld.toByteArray(), "UTF-8");

        ByteArrayOutputStream baosNew = new ByteArrayOutputStream();
        URL newConfigUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayFieldsDeltaNew.json");
        IOUtils.copy(new FileInputStream(newConfigUrl.getPath()), baosNew, 1024);
        String newStr = new String(baosNew.toByteArray(), "UTF-8");

        BaseData oldData = new BaseData(new BaseSchema(schema.toString()), oldStr);
        BaseData newData = new BaseData(new BaseSchema(schema.toString()), newStr);
        RawBinaryDelta deltaResult = calculator.calculate(oldData, newData);

        /* The first delta - the item object was changed */
        GenericRecord delta1 = new GenericData.Record(getDeltaSchemaByFullName(protocolSchema, "org.kaa.config.testRecordItemT"));
        byte[] rawUuid1 = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3 };
        GenericFixed uuid1 = new GenericData.Fixed(delta1.getSchema().getField("__uuid").schema(), rawUuid1);
        delta1.put("testField4", 36);
        delta1.put("__uuid", uuid1);

        /* The second delta - one item was removed from the array */
        GenericRecord delta2 = new GenericData.Record(getDeltaSchemaByFullName(protocolSchema, "org.kaa.config.testT"));
        byte[] rawUuid2 = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        GenericFixed uuid2 = new GenericData.Fixed(delta2.getSchema().getField("__uuid").schema(), rawUuid2);

        GenericEnumSymbol unchanged = new GenericData.EnumSymbol(getSchemaByFullName(delta2.getSchema().getField("testField1").schema().getTypes(),
                "org.kaaproject.configuration.unchangedT"), "unchanged");
        delta2.put("testField1", unchanged);
        delta2.put("__uuid", uuid2);
        delta2.put("testField5", unchanged);

        GenericRecord testField21 = new GenericData.Record(getSchemaByFullName(delta2.getSchema().getField("testField2").schema().getTypes(),
                "org.kaa.config.testRecordT"));
        GenericArray<GenericFixed> testField31 = new GenericData.Array<GenericFixed> (1, getArraySchema(testField21, "testField3"));
        byte[] rawUuidToDelete = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 };
        GenericFixed uuidToDelete = new GenericData.Fixed(getSchemaByFullName(testField31.getSchema().getElementType().getTypes(),
                "org.kaaproject.configuration.uuidT"), rawUuidToDelete);
        testField31.add(uuidToDelete);
        testField21.put("testField3", testField31);
        delta2.put("testField2", testField21);

        /* The third delta - one new item was added to the array */
        GenericRecord delta3 = new GenericData.Record(getDeltaSchemaByFullName(protocolSchema, "org.kaa.config.testT"));
        delta3.put("testField1", unchanged);
        delta3.put("__uuid", uuid2);

        GenericRecord testField22 = new GenericData.Record(getSchemaByFullName(delta3.getSchema().getField("testField2").schema().getTypes(),
                "org.kaa.config.testRecordT"));
        GenericArray<GenericRecord> testField32 = new GenericData.Array<GenericRecord>(1, getArraySchema(testField22, "testField3"));
        GenericRecord itemRecord = new GenericData.Record(getSchemaByFullName(testField32.getSchema().getElementType().getTypes(),
                "org.kaa.config.testRecordItemT"));
        itemRecord.put("testField4", 4);
        byte[] rawNewRecordUuid = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4 };
        GenericFixed newRecordUuid = new GenericData.Fixed(itemRecord.getSchema().getField("__uuid").schema(), rawNewRecordUuid);
        itemRecord.put("__uuid", newRecordUuid);
        testField32.add(itemRecord);
        testField22.put("testField3", testField32);
        delta3.put("testField2", testField22);

        AvroBinaryDelta deltaExpected = new AvroBinaryDelta(protocolSchema);
        deltaExpected.addDelta(delta1);
        deltaExpected.addDelta(delta2);
        deltaExpected.addDelta(delta3);

        assertArrayEquals(deltaExpected.getData(), deltaResult.getData());
    }

    @Test
    public void testArrayPrimitiveFieldsDelta() throws IOException, URISyntaxException, DeltaCalculatorException {
        URL protocolSchemaUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayPrimitiveFieldsDeltaProtocolSchema.json");
        Schema protocolSchema = new Schema.Parser().parse(new File(protocolSchemaUrl.getPath()));
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayPrimitiveFieldsDeltaSchema.json");
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));
        DeltaCalculationAlgorithm calculator = new DefaultDeltaCalculationAlgorithm(protocolSchema, schema);

        ByteArrayOutputStream baosOld = new ByteArrayOutputStream();
        URL oldConfigUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayPrimitiveFieldsDeltaCurrent.json");
        IOUtils.copy(new FileInputStream(oldConfigUrl.getPath()), baosOld, 1024);
        String oldStr = new String(baosOld.toByteArray(), "UTF-8");

        ByteArrayOutputStream baosNew = new ByteArrayOutputStream();
        URL newConfigUrl = Thread.currentThread().getContextClassLoader().getResource("delta/arrayPrimitiveFieldsDeltaNew.json");
        IOUtils.copy(new FileInputStream(newConfigUrl.getPath()), baosNew, 1024);
        String newStr = new String(baosNew.toByteArray(), "UTF-8");

        BaseData oldData = new BaseData(new BaseSchema(schema.toString()), oldStr);
        BaseData newData = new BaseData(new BaseSchema(schema.toString()), newStr);
        RawBinaryDelta deltaResult = calculator.calculate(oldData, newData);

        GenericRecord delta1 = new GenericData.Record(getDeltaSchemaByFullName(protocolSchema, "org.kaa.config.testT"));
        GenericEnumSymbol reset = new GenericData.EnumSymbol(getSchemaByFullName(delta1.getSchema().getField("testField1").schema().getTypes(),
                "org.kaaproject.configuration.resetT"), "reset");
        byte[] rawUuid = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };
        GenericFixed uuid = new GenericData.Fixed(delta1.getSchema().getField("__uuid").schema(), rawUuid);
        delta1.put("testField1", reset);
        delta1.put("__uuid", uuid);

        GenericRecord delta2 = new GenericData.Record(getDeltaSchemaByFullName(protocolSchema, "org.kaa.config.testT"));
        delta2.put("__uuid", uuid);
        GenericArray<Integer> testField1 = new GenericData.Array<Integer>(3, getArraySchema(delta2, "testField1"));
        testField1.add(321);
        testField1.add(456);
        testField1.add(654);
        delta2.put("testField1", testField1);

        AvroBinaryDelta deltaExpected = new AvroBinaryDelta(protocolSchema);
        deltaExpected.addDelta(delta1);
        deltaExpected.addDelta(delta2);

        assertArrayEquals(deltaExpected.getData(), deltaResult.getData());
    }

    @Test
    public void testIdenticalConfigurations() throws IOException, URISyntaxException, DeltaCalculatorException {
        URL protocolSchemaUrl = Thread.currentThread().getContextClassLoader().getResource(COMPLEX_PROTOCOL_SCHEMA);
        URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(COMPLEX_SCHEMA);
        Schema protocolSchema = new Schema.Parser().parse(new File(protocolSchemaUrl.getPath()));
        Schema schema = new Schema.Parser().parse(new File(schemaUrl.getPath()));
        DeltaCalculationAlgorithm calculator = new DefaultDeltaCalculationAlgorithm(protocolSchema, schema);

        ByteArrayOutputStream baosOld = new ByteArrayOutputStream();
        URL oldConfigUrl = Thread.currentThread().getContextClassLoader().getResource(OLD_COMPLEX_CONFIG);
        IOUtils.copy(new FileInputStream(oldConfigUrl.getPath()), baosOld, 1024);
        String oldStr = new String(baosOld.toByteArray(), "UTF-8");

        ByteArrayOutputStream baosNew = new ByteArrayOutputStream();
        URL newConfigUrl = Thread.currentThread().getContextClassLoader().getResource(OLD_COMPLEX_CONFIG);
        IOUtils.copy(new FileInputStream(newConfigUrl.getPath()), baosNew, 1024);
        String newStr = new String(baosNew.toByteArray(), "UTF-8");

        BaseData oldData = new BaseData(new BaseSchema(schema.toString()), oldStr);
        BaseData newData = new BaseData(new BaseSchema(schema.toString()), newStr);
        RawBinaryDelta deltaResult = calculator.calculate(oldData, newData);

        assertFalse(deltaResult.hasChanges());
        assertNull(deltaResult.getData());
        assertFalse(deltaResult.hasChanges());
    }
}
