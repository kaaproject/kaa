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

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;

/**
 * Class containing algorithms for canonization Avro data.
 */
public class AvroDataCanonizationUtils {

    private static final String UUIDT = "org.kaaproject.configuration.uuidT";
    private static final AvroDataComparator COMPARATOR = new AvroDataComparator();

    private AvroDataCanonizationUtils() {
    }

    /**
     * Performs canonization of records in array if they are present.
     *
     * @param     baseArray the array to be canonized.
     */
    private static void canonizeArray(GenericArray baseArray) {
        for (Object obj : baseArray) {
            if (obj instanceof GenericRecord) {
                canonizeRecord((GenericRecord)obj);
            } else if (obj instanceof GenericArray) {
                canonizeArray((GenericArray)obj);
            }
        }
    }

    /**
     * Check if the field contains UUID and returns value which should be on its place.
     *
     * @param     uuidField    GenericFixed field which is supposed to contain UUID.
     * @param     fieldSchema    Schema of given field.
     * @return    null if field is assumed as an UUID or field without changes otherwise
     */
    private static GenericFixed clearUuid(GenericFixed uuidField, Schema.Field fieldSchema) {
        if (fieldSchema.schema().getType() == Schema.Type.UNION) {
            for (Schema unionedSchema : fieldSchema.schema().getTypes()) {
                if  (unionedSchema.getFullName().equalsIgnoreCase(UUIDT)) {
                    return null;
                }
            }
        } else {
            if (uuidField.getSchema().getFullName().equalsIgnoreCase(UUIDT)) {
                return null;
            }
        }
        return uuidField;
    }

    /**
     * Recursively removes UUIDs from the record.
     *
     * @param     baseRecord The record containing UUID fields.
     */
    public static void removeUuid(GenericRecord baseRecord) {
        Schema recordSchema = baseRecord.getSchema();
        for (Schema.Field fieldSchema : recordSchema.getFields()) {
            if (baseRecord.get(fieldSchema.name()) != null) {
                Object field = baseRecord.get(fieldSchema.name());
                if (field instanceof GenericFixed) {
                    baseRecord.put(fieldSchema.name(), clearUuid((GenericFixed) field, fieldSchema));
                } else if (field instanceof GenericRecord) {
                    removeUuid((GenericRecord) field);
                } else if (field instanceof GenericArray) {
                    GenericArray arrayField = (GenericArray) field;
                    for (Object obj : arrayField) {
                        if (obj instanceof GenericRecord) {
                            removeUuid((GenericRecord) obj);
                        }
                    }
                }
            }
        }
    }

    /**
     * Performs canonization of the given record.
     *
     * @param     baseRecord The record to be canonized.
     */
    public static void canonizeRecord(GenericRecord baseRecord) {
        Schema recordSchema = baseRecord.getSchema();

        for (Schema.Field fieldSchema : recordSchema.getFields()) {
            if (baseRecord.get(fieldSchema.name()) != null) {
                Object field = baseRecord.get(fieldSchema.name());
                if (field instanceof GenericArray) {
                    if (fieldSchema.schema().getType() == Schema.Type.UNION) {
                        for (Schema unoinedSchema : fieldSchema.schema().getTypes()) {
                            if  (unoinedSchema.getType() == Schema.Type.ARRAY) {
                                COMPARATOR.setSchema(unoinedSchema.getElementType());
                                break;
                            }
                        }
                    } else {
                        COMPARATOR.setSchema(fieldSchema.schema().getElementType());
                    }
                    GenericArray arrayField = (GenericArray) baseRecord.get(fieldSchema.name());
                    canonizeArray(arrayField);
                    Collections.sort(arrayField, COMPARATOR);
                } else if (field instanceof GenericRecord) {
                    canonizeRecord((GenericRecord)field);
                }
            }
        }
    }
}
