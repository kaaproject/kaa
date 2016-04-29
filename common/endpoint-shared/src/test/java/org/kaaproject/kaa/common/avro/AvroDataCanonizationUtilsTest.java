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

package org.kaaproject.kaa.common.avro;

import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;

public class AvroDataCanonizationUtilsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testRemoveUuid() {
        Schema uuidSchema = Schema.createFixed("uuidT", "", "org.kaaproject.configuration", 16);
        Schema.Field uuidField = new Schema.Field("__uuid", uuidSchema, "", null);
        Schema recordSchema = Schema.createRecord(Arrays.asList(uuidField));

        GenericRecord recordWithUuid = new GenericData.Record(recordSchema);
        byte[] uuid_value = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };
        GenericFixed uuid = (GenericFixed) GenericData.get().createFixed(null, uuid_value, uuidSchema);
        recordWithUuid.put("__uuid", uuid);

        AvroDataCanonizationUtils.removeUuid(recordWithUuid);

        Assert.assertNotNull("recordWithoutUuid is null", recordWithUuid);
        Assert.assertNull("Uuid is still present after removal in recordWithoutUuid", recordWithUuid.get("__uuid"));

        Schema otherFixedSchema = Schema.createFixed("__uuid", "", "not.a", 1);
        Schema.Field otherFixed = new Schema.Field("some_field", otherFixedSchema, "", null);
        byte[] otherFixedValue = { 1 };
        Schema.Field innerRecordField = new Schema.Field("inner", recordSchema, "", null);

        Schema intFieldSchema = Schema.create(Type.INT);
        Schema.Field intField = new Schema.Field("int_value", intFieldSchema, "", null);
        Schema.Field uuidInArrayField = new Schema.Field("__uuid", uuidSchema, "", null);
        Schema inArrayRecordSchema = Schema.createRecord(Arrays.asList(uuidInArrayField, intField));

        GenericRecord inArrayRecord1 = new GenericData.Record(inArrayRecordSchema);
        inArrayRecord1.put("__uuid", GenericData.get().createFixed(null, uuid_value, uuidSchema));
        inArrayRecord1.put("int_value", new Integer(100));

        GenericRecord inArrayRecord2 = new GenericData.Record(inArrayRecordSchema);
        inArrayRecord2.put("__uuid", GenericData.get().createFixed(null, uuid_value, uuidSchema));
        inArrayRecord2.put("int_value", new Integer(200));

        Schema arraySchema = Schema.createArray(inArrayRecordSchema);
        Schema.Field arrayField = new Schema.Field("array1", arraySchema, "", null);
        GenericArray<GenericRecord> array1 = new GenericData.Array<GenericRecord>(2, arraySchema);
        array1.add(inArrayRecord1);
        array1.add(inArrayRecord2);
        Schema.Field uuidOfComplexRecordField = new Schema.Field("__uuid", uuidSchema, "", null);
        Schema complexRecordSchemaWithUuid = Schema.createRecord(Arrays.asList(innerRecordField, arrayField, uuidOfComplexRecordField, otherFixed));

        GenericRecord complexRecordWithUuid = new GenericData.Record(complexRecordSchemaWithUuid);
        complexRecordWithUuid.put("inner", recordWithUuid);
        complexRecordWithUuid.put("some_field", GenericData.get().createFixed(null, otherFixedValue, otherFixedSchema));
        complexRecordWithUuid.put("__uuid", uuid);
        complexRecordWithUuid.put("array1", array1);

        AvroDataCanonizationUtils.removeUuid(complexRecordWithUuid);
        Assert.assertNotNull("complexRecordWithoutUuid is null", complexRecordWithUuid);
        Assert.assertNull("Uuid is still present after removal in complexRecordWithoutUuid", complexRecordWithUuid.get("__uuid"));
        Assert.assertNotNull("some_field in complexRecordWithoutUuid is null", complexRecordWithUuid.get("some_field"));

        GenericRecord innerRecordWithoutUuid = (GenericRecord) complexRecordWithUuid.get("inner");
        Assert.assertNotNull("innerRecordWithoutUuid is null", innerRecordWithoutUuid);
        Assert.assertNull("Uuid is still present after removal in innerRecordWithoutUuid", innerRecordWithoutUuid.get("__uuid"));

        GenericArray<GenericRecord> array1WithoutUuids = (GenericArray<GenericRecord>) complexRecordWithUuid.get("array1");
        for (GenericRecord rec : array1WithoutUuids) {
            Assert.assertNull("Uuid is still present after removal in record in array", rec.get("__uuid"));
            Assert.assertNotNull("Record in array is null", rec.get("int_value"));
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCanonizeRecord() {
        Schema arrayASchema = Schema.createArray(Schema.create(Type.INT));
        Schema arrayBSchema = Schema.createArray(Schema.create(Type.STRING));
        Schema.Field arrayAField = new Schema.Field("arrayA", arrayASchema, "", null);
        Schema.Field arrayBField = new Schema.Field("arrayB", arrayBSchema, "", null);

        Schema recordASchema = Schema.createRecord(Arrays.asList(arrayBField));
        Schema.Field recordAField = new Schema.Field("recordA", recordASchema, "", null);

        Schema rootRecordSchema = Schema.createRecord(Arrays.asList(arrayAField, recordAField));

        GenericRecord mixedRecord = new GenericData.Record(rootRecordSchema);

        GenericArray<Integer> arrayA = new GenericData.Array<Integer>(4, arrayASchema);
        arrayA.add(9);
        arrayA.add(8);
        arrayA.add(2);
        arrayA.add(1);
        mixedRecord.put("arrayA", arrayA);

        GenericRecord recordA = new GenericData.Record(recordASchema);
        GenericArray<String> arrayB = new GenericData.Array<String>(5, arrayBSchema);
        arrayB.add("abc");
        arrayB.add("jkl");
        arrayB.add("ghi");
        arrayB.add("def");
        recordA.put("arrayB", arrayB);
        mixedRecord.put("recordA", recordA);

        AvroDataCanonizationUtils.canonizeRecord(mixedRecord);

        GenericArray<Integer> newArrayA = (GenericArray<Integer>) mixedRecord.get("arrayA");
        for (int i = 0; i < newArrayA.size() - 1; ++i) {
            Assert.assertTrue("Items are in bad order", newArrayA.get(i) < newArrayA.get(i + 1));
        }

        GenericArray<String> newArrayB = (GenericArray<String>) ((GenericRecord) mixedRecord.get("recordA")).get("arrayB");
        for (int i = 0; i < newArrayB.size() - 1; ++i) {
            Assert.assertTrue("Items are in bad order", newArrayB.get(i).compareTo(newArrayB.get(i + 1)) < 0);
        }

    }

}
