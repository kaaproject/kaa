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

package org.kaaproject.kaa.server.common.log.shared;

import java.io.IOException;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.common.utils.FileUtils;

public class RecordWrapperSchemaGenerator {
    
    public static String RECORD_HEADER_FIELD = "recordHeader";
    public static String RECORD_DATA_FIELD = "recordData";

    /** The Constant RECORD_WRAPPER_SCHEMA_TEMPLATE. */
    private static final String RECORD_WRAPPER_SCHEMA_TEMPLATE = "avro/record_wrapper_schema.avsc.template";

    /** The Constant RECORD_HEADER_SCHEMA_VAR. */
    private static final String RECORD_HEADER_SCHEMA_VAR = "\\$\\{record_header_schema\\}";

    /** The Constant RECORD_DATA_SCHEMA_VAR. */
    private static final String RECORD_DATA_SCHEMA_VAR = "\\$\\{record_data_schema\\}";
    
    private static String recordWrapperSchemaTemplate;

    private RecordWrapperSchemaGenerator() {
    }

    public static Schema generateRecordWrapperSchema(String userRecordSchema) throws IOException {
        if (recordWrapperSchemaTemplate == null) {
            recordWrapperSchemaTemplate = FileUtils.readResource(RECORD_WRAPPER_SCHEMA_TEMPLATE);
            String recordHeaderSchema = RecordHeader.getClassSchema().toString();
            recordWrapperSchemaTemplate = recordWrapperSchemaTemplate.replaceAll(RECORD_HEADER_SCHEMA_VAR, recordHeaderSchema);
        }
        String recordWrapperSchemaString = recordWrapperSchemaTemplate.replaceAll(RECORD_DATA_SCHEMA_VAR, userRecordSchema);
        Schema.Parser parser = new Schema.Parser();
        Schema recordWrapperSchema = parser.parse(recordWrapperSchemaString);
        return recordWrapperSchema;
    }
    
}
