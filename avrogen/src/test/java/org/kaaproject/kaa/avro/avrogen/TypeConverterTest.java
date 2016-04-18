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

package org.kaaproject.kaa.avro.avrogen;

import java.util.Arrays;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.junit.Assert;
import org.junit.Test;

public class TypeConverterTest {
    @Test
    public void testTypes() {
        Assert.assertTrue(TypeConverter.isAvroBytes(Schema.create(Type.BYTES)));
        Assert.assertFalse(TypeConverter.isAvroBytes(Schema.create(Type.STRING)));

        Assert.assertTrue(TypeConverter.isAvroArray(Schema.createArray(Schema.create(Type.BYTES))));
        Assert.assertFalse(TypeConverter.isAvroArray(Schema.create(Type.BYTES)));

        Assert.assertTrue(TypeConverter.isAvroString(Schema.create(Type.STRING)));
        Assert.assertFalse(TypeConverter.isAvroString(Schema.create(Type.BOOLEAN)));

        Assert.assertTrue(TypeConverter.isAvroEnum(Schema.createEnum("name", "doc", "namespace", Arrays.asList("node"))));
        Assert.assertFalse(TypeConverter.isAvroEnum(Schema.create(Type.BOOLEAN)));

        Assert.assertTrue(TypeConverter.isAvroNull(Schema.create(Type.NULL)));
        Assert.assertFalse(TypeConverter.isAvroNull(Schema.create(Type.BOOLEAN)));

        Assert.assertTrue(TypeConverter.isAvroPrimitive(Schema.create(Type.BOOLEAN)));
        Assert.assertTrue(TypeConverter.isAvroPrimitive(Schema.create(Type.INT)));
        Assert.assertTrue(TypeConverter.isAvroPrimitive(Schema.create(Type.LONG)));
        Assert.assertTrue(TypeConverter.isAvroPrimitive(Schema.createEnum("name", "doc", "namespace", Arrays.asList("node"))));
        Assert.assertFalse(TypeConverter.isAvroPrimitive(Schema.create(Type.STRING)));
        Assert.assertFalse(TypeConverter.isAvroPrimitive(Schema.create(Type.BYTES)));

        Assert.assertTrue(TypeConverter.isAvroUnion(Schema.createUnion(
                Arrays.asList(Schema.create(Type.BYTES), Schema.create(Type.STRING)))));
        Assert.assertFalse(TypeConverter.isAvroUnion(Schema.create(Type.STRING)));

        Assert.assertTrue(TypeConverter.isAvroRecord(Schema.createRecord("name", "doc", "namespace", false)));
        Assert.assertFalse(TypeConverter.isAvroRecord(Schema.create(Type.STRING)));

        Assert.assertTrue(TypeConverter.isAvroUnion(Schema.createUnion(
                Arrays.asList(Schema.create(Type.BYTES), Schema.create(Type.STRING)))));
        Assert.assertFalse(TypeConverter.isAvroUnion(Schema.create(Type.STRING)));

        Assert.assertTrue(TypeConverter.isAvroString(Schema.create(Type.STRING)));
        Assert.assertFalse(TypeConverter.isAvroString(Schema.create(Type.INT)));
    }

    @Test
    public void testDirection() {
        final String DIRECTION_FIELD = "direction";

        Schema inOutSchema = Schema.createEnum("test1", "doc", "namespace", Arrays.asList("node"));
        Assert.assertTrue(TypeConverter.isTypeOut(inOutSchema));
        Assert.assertTrue(TypeConverter.isTypeIn(inOutSchema));

        Schema outSchema = Schema.createRecord("test2", "doc", "namespace", false);
        outSchema.addProp(DIRECTION_FIELD, "out");

        Assert.assertTrue(TypeConverter.isTypeOut(outSchema));
        Assert.assertFalse(TypeConverter.isTypeIn(outSchema));

        Schema inSchema = Schema.createRecord("test13", "doc", "namespace", false);
        inSchema.addProp(DIRECTION_FIELD, "in");

        Assert.assertTrue(TypeConverter.isTypeIn(inSchema));
        Assert.assertFalse(TypeConverter.isTypeOut(inSchema));

        Schema invalidDirectionSchema = Schema.createRecord("test4", "doc", "namespace", false);
        invalidDirectionSchema.addProp(DIRECTION_FIELD, "inOut");

        Assert.assertFalse(TypeConverter.isTypeIn(invalidDirectionSchema));
        Assert.assertFalse(TypeConverter.isTypeOut(invalidDirectionSchema));
    }

    @Test
    public void testIsRecordNeedDeallocator() {
        Assert.assertFalse(TypeConverter.isRecordNeedDeallocator(Schema.create(Type.INT)));
    }
}
