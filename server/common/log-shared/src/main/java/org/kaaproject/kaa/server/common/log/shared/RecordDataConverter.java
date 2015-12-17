/*
 * Copyright 2015 CyberVision, Inc.
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


import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecordBase;
import org.json.JSONObject;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.avro.AvroJsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class RecordDataConverter<T extends SpecificRecordBase> {

    private static final Logger LOG = LoggerFactory.getLogger(RecordDataConverter.class);

    private static final String SCHEMA_FIELD = "SCHEMA$";

    private final Schema recordSchema;

    private ThreadLocal<Map<String, AvroByteArrayConverter<SpecificRecordBase>>> bytaArrayConverters = new ThreadLocal<Map<String, AvroByteArrayConverter<SpecificRecordBase>>>() {
        @Override
        protected Map<String, AvroByteArrayConverter<SpecificRecordBase>> initialValue() {
            return new HashMap<String, AvroByteArrayConverter<SpecificRecordBase>>();
        }
    };

    private ThreadLocal<Map<String, AvroJsonConverter<SpecificRecordBase>>> jsonConverters = new ThreadLocal<Map<String, AvroJsonConverter<SpecificRecordBase>>>() {
        @Override
        protected Map<String, AvroJsonConverter<SpecificRecordBase>> initialValue() {
            return new HashMap<String, AvroJsonConverter<SpecificRecordBase>>();
        }
    };

    public RecordDataConverter(Class<T> clazz) {
        this.recordSchema = getSchema(clazz);
        initByteArrayConverter(recordSchema.toString(), (Class<SpecificRecordBase>) clazz);
        initJsonConverter(recordSchema, (Class<SpecificRecordBase>) clazz);
    }

    public T decode(String recordStringJson, String fieldName) throws IOException {
        AvroJsonConverter<SpecificRecordBase> recordConverter = getJsonConverter(recordSchema.toString());
        JSONObject jsonData = new JSONObject(recordStringJson);
        String fieldData = jsonData.getJSONObject(fieldName).toString();
        return (T) recordConverter.decodeJson(fieldData);
    }

    public T decode(String recordStringJson) throws IOException {
        AvroJsonConverter<SpecificRecordBase> recordConverter = getJsonConverter(recordSchema.toString());
        return (T) recordConverter.decodeJson(recordStringJson);
    }

    public T decode(byte[] recordBinary) throws IOException {
        AvroByteArrayConverter<SpecificRecordBase> headerConverter = getByteArrayConverter(recordSchema.toString());
        return (T) headerConverter.fromByteArray(recordBinary);
    }

    private AvroByteArrayConverter<SpecificRecordBase> getByteArrayConverter(String schema) {
        Map<String, AvroByteArrayConverter<SpecificRecordBase>> converterMap = bytaArrayConverters.get();
        AvroByteArrayConverter<SpecificRecordBase> genAvroConverter = converterMap.get(schema);
        return genAvroConverter;
    }

    private AvroJsonConverter<SpecificRecordBase> getJsonConverter(String schema) {
        Map<String, AvroJsonConverter<SpecificRecordBase>> converterMap = jsonConverters.get();
        AvroJsonConverter<SpecificRecordBase> genAvroConverter = converterMap.get(schema);
        return genAvroConverter;
    }

    private void initByteArrayConverter(String schema, Class<SpecificRecordBase> clazz) {
        Map<String, AvroByteArrayConverter<SpecificRecordBase>> converterMap = bytaArrayConverters.get();
        AvroByteArrayConverter<SpecificRecordBase> genAvroConverter = new AvroByteArrayConverter<SpecificRecordBase>(clazz);
        converterMap.put(schema, genAvroConverter);
        bytaArrayConverters.set(converterMap);
    }

    private void initJsonConverter(Schema schema, Class<SpecificRecordBase> clazz) {
        Map<String, AvroJsonConverter<SpecificRecordBase>> converterMap = jsonConverters.get();
        AvroJsonConverter<SpecificRecordBase> genAvroConverter = new AvroJsonConverter<SpecificRecordBase>(schema, clazz);
        converterMap.put(schema.toString(), genAvroConverter);
        jsonConverters.set(converterMap);
    }

    private static Schema getSchema(Class<?> c) {
        Schema result = null;
        try {
            Field f = c.getDeclaredField(SCHEMA_FIELD);
            f.setAccessible(true);
            if (f.isAccessible()) {
                result = (Schema) f.get(null);
            } else {
                return null;
            }

        } catch (Exception e) {
            LOG.error("Error parsing schema: ", e);
        }
        return result;
    }
}