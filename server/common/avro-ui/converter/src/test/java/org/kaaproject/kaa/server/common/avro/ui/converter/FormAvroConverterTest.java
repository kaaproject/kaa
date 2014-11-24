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
package org.kaaproject.kaa.server.common.avro.ui.converter;

import java.io.IOException;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.server.common.avro.ui.shared.ArrayField;
import org.kaaproject.kaa.server.common.avro.ui.shared.FormField;
import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;
import org.kaaproject.kaa.server.common.avro.ui.shared.StringField;
import org.kaaproject.kaa.server.common.avro.ui.shared.UnionField;

public class FormAvroConverterTest {
    
    @Test
    public void testSingleFieldRecord() throws IOException {
        Schema schema = TestAvroSchemas.getSingleFieldSchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        checkSingleFieldRecord(field);
    }

    @Test
    public void testArrayRecord() throws IOException {
        Schema schema = TestAvroSchemas.getArraySchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(1, field.getValue().size());
        FormField formField = field.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof ArrayField);
        ArrayField arrayField = (ArrayField)formField;
        Assert.assertNotNull(arrayField.getValue());
        Assert.assertEquals(1, arrayField.getValue().size());
        checkSingleFieldRecord(arrayField.getValue().get(0));
        Assert.assertNotNull(arrayField.getElementMetadata());
        checkSingleFieldRecord(arrayField.getElementMetadata());
    }

    @Test
    public void testUnionRecord() throws IOException {
        Schema schema = TestAvroSchemas.getUnionSchema();
        RecordField field = FormAvroConverter.createRecordFieldFromSchema(schema);
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(1, field.getValue().size());
        FormField formField = field.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof UnionField);
        UnionField unionField = (UnionField)formField;
        Assert.assertNull(unionField.getValue());
        Assert.assertNotNull(unionField.getAcceptableValues());
        Assert.assertEquals(2, unionField.getAcceptableValues().size());
        checkSingleFieldRecord(unionField.getAcceptableValues().get(0));
        checkSingleFieldRecord(unionField.getAcceptableValues().get(1));
    }
    
    private void checkSingleFieldRecord(RecordField field) {
        Assert.assertNotNull(field);
        Assert.assertNotNull(field.getValue());
        Assert.assertEquals(1, field.getValue().size());
        FormField formField = field.getValue().get(0);
        Assert.assertNotNull(formField);
        Assert.assertTrue(formField instanceof StringField);
    }
}
