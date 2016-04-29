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

package org.kaaproject.kaa.server.admin.services.schema;

import java.io.IOException;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.kaaproject.avro.ui.converter.SchemaFormAvroConverter;

/**
 * The Class EcfSchemaFormAvroConverter.
 */
public class SimpleSchemaFormAvroConverter extends SchemaFormAvroConverter {

    /**
     * Instantiates a new simple schema form avro converter.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public SimpleSchemaFormAvroConverter() throws IOException {
        super();
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.avro.ui.converter.SchemaFormAvroConverter#customizeRecordFields(org.apache.avro.Schema, java.util.List)
     */
    @Override
    protected void customizeRecordFields(Schema recordSchema, List<Field> fields) {
        int index = getFieldIndex(fields, DEFAULT_VALUE);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, DISPLAY_NAME);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, DISPLAY_PROMPT);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, WEIGHT);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, KEY_INDEX);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, MAX_LENGTH);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, INPUT_TYPE);
        if (index > -1) {
            fields.remove(index);
        }
        index = getFieldIndex(fields, MIN_ROW_COUNT);
        if (index > -1) {
            fields.remove(index);
        }
    }

}
