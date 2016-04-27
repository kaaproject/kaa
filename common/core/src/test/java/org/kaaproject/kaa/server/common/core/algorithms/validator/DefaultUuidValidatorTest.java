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

package org.kaaproject.kaa.server.common.core.algorithms.validator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericFixed;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.algorithms.CommonConstants;
import org.kaaproject.kaa.server.common.core.algorithms.AvroUtils;
import org.kaaproject.kaa.server.common.core.configuration.BaseData;
import org.kaaproject.kaa.server.common.core.configuration.BaseDataFactory;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;

public class DefaultUuidValidatorTest {

    @Test
    public void testGeneration() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/simpleSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        Path configurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/simpleConfiguration.json").toURI());
        String configuraion = new String(Files.readAllBytes(configurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema.getRawSchema());

        // generated default configuration
        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(new BaseData(configuraionSchema, configuraion), null);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());

        Object uuid = processedConfiguration.get(CommonConstants.UUID_FIELD);
        Assert.assertNotNull(uuid);
        Assert.assertTrue(uuid instanceof GenericFixed);
        Assert.assertEquals(CommonConstants.KAA_NAMESPACE + "." + CommonConstants.UUID_TYPE, ((GenericFixed) uuid).getSchema().getFullName());
    }

    @Test
    public void testValidationWithoutOldConfiguration() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/simpleSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema avroSchema = new Schema.Parser().parse(configuraionSchema.getRawSchema());
        GenericRecord record = new GenericData.Record(avroSchema);
        record.put("intField", 5);
        GenericFixed uuid = AvroUtils.generateUuidObject();
        record.put(CommonConstants.UUID_FIELD, uuid);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBody = converter.encodeToJson(record);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(new BaseData(configuraionSchema, configurationBody), null);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        Assert.assertNotEquals(processedConfiguration.get(CommonConstants.UUID_FIELD), uuid);
    }

    @Test
    public void testValidationWithOldConfiguration() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/simpleSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema avroSchema = new Schema.Parser().parse(configuraionSchema.getRawSchema());
        GenericRecord recordNew = new GenericData.Record(avroSchema);
        recordNew.put("intField", 4);
        GenericFixed uuidNew = AvroUtils.generateUuidObject();
        recordNew.put(CommonConstants.UUID_FIELD, uuidNew);

        GenericRecord recordOld = new GenericData.Record(avroSchema);
        recordOld.put("intField", 5);
        GenericFixed uuidOld = AvroUtils.generateUuidObject();
        recordOld.put(CommonConstants.UUID_FIELD, uuidOld);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBodyNew = converter.encodeToJson(recordNew);
        String configurationBodyOld = converter.encodeToJson(recordOld);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(recordNew, recordOld);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        Assert.assertEquals(processedConfiguration.get(CommonConstants.UUID_FIELD), uuidOld);
    }

    @Test
    public void testValidationOfArrayFields() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/arraySchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema.Parser schemaParser = new Schema.Parser();
        Schema avroSchema = schemaParser.parse(configuraionSchema.getRawSchema());

        GenericRecord recordNew1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew1.put("intField", 4);
        GenericFixed uuidNew1 = AvroUtils.generateUuidObject();
        recordNew1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord recordNew2 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew2.put("intField", 5);
        GenericFixed uuidNew2 = AvroUtils.generateUuidObject();
        recordNew2.put(CommonConstants.UUID_FIELD, uuidNew2);

        GenericRecord rootNew = new GenericData.Record(avroSchema);
        GenericArray arrayNew = new GenericData.Array<>(2, rootNew.getSchema().getField("complexArrayField").schema());
        arrayNew.add(recordNew1);
        arrayNew.add(recordNew2);
        rootNew.put("complexArrayField", arrayNew);

        GenericRecord recordOld1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld1.put("intField", 6);
        recordOld1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord recordOld2 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld2.put("intField", 7);
        GenericFixed uuidOld2 = AvroUtils.generateUuidObject();
        recordOld2.put(CommonConstants.UUID_FIELD, uuidOld2);

        GenericRecord rootOld = new GenericData.Record(avroSchema);
        GenericArray arrayOld = new GenericData.Array<>(2, rootOld.getSchema().getField("complexArrayField").schema());
        arrayOld.add(recordOld1);
        arrayOld.add(recordOld2);
        rootOld.put("complexArrayField", arrayOld);
        rootOld.put(CommonConstants.UUID_FIELD, AvroUtils.generateUuidObject());

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBodyNew = converter.encodeToJson(rootNew);
        String configurationBodyOld = converter.encodeToJson(rootOld);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(rootNew, rootOld);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        GenericArray processedArray = (GenericArray) processedConfiguration.get("complexArrayField");
        GenericRecord record1 = (GenericRecord) processedArray.get(0);
        GenericRecord record2 = (GenericRecord) processedArray.get(1);
        Assert.assertEquals(uuidNew1, record1.get(CommonConstants.UUID_FIELD));
        Assert.assertNotEquals(uuidNew2, record2.get(CommonConstants.UUID_FIELD));
        Assert.assertNotNull(processedConfiguration.get(CommonConstants.UUID_FIELD));
    }


    @Test
    public void testValidationOfDifferentTypes() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/arraySchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema.Parser schemaParser = new Schema.Parser();
        Schema avroSchema = schemaParser.parse(configuraionSchema.getRawSchema());

        GenericRecord recordNew1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew1.put("intField", 4);
        GenericFixed uuidNew1 = AvroUtils.generateUuidObject();
        recordNew1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord recordNew2 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew2.put("intField", 5);
        GenericFixed uuidNew2 = AvroUtils.generateUuidObject();
        recordNew2.put(CommonConstants.UUID_FIELD, uuidNew2);

        GenericRecord rootNew = new GenericData.Record(avroSchema);
        GenericArray arrayNew = new GenericData.Array<>(2, rootNew.getSchema().getField("complexArrayField").schema());
        arrayNew.add(recordNew1);
        arrayNew.add(recordNew2);
        rootNew.put("complexArrayField", arrayNew);
        rootNew.put(CommonConstants.UUID_FIELD, uuidNew2);

        GenericRecord recordOld1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld1.put("intField", 6);
        recordOld1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord recordOld2 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld2.put("intField", 7);
        GenericFixed uuidOld2 = AvroUtils.generateUuidObject();
        recordOld2.put(CommonConstants.UUID_FIELD, uuidOld2);

        GenericRecord rootOld = new GenericData.Record(avroSchema);
        GenericArray arrayOld = new GenericData.Array<>(2, rootOld.getSchema().getField("complexArrayField").schema());
        arrayOld.add(recordOld1);
        arrayOld.add(recordOld2);
        rootOld.put("complexArrayField", arrayOld);
        rootOld.put(CommonConstants.UUID_FIELD, uuidNew2);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBodyNew = converter.encodeToJson(rootNew);
        String configurationBodyOld = converter.encodeToJson(rootOld);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(rootNew, rootOld);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        GenericArray processedArray = (GenericArray) processedConfiguration.get("complexArrayField");
        GenericRecord record1 = (GenericRecord) processedArray.get(0);
        GenericRecord record2 = (GenericRecord) processedArray.get(1);
        Assert.assertEquals(uuidNew1, record1.get(CommonConstants.UUID_FIELD));
        Assert.assertNotEquals(uuidNew2, record2.get(CommonConstants.UUID_FIELD));
        Assert.assertEquals(uuidNew2, processedConfiguration.get(CommonConstants.UUID_FIELD));
    }

    @Test
    public void testValidationOfIdenticalUuids() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/arraySchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema.Parser schemaParser = new Schema.Parser();
        Schema avroSchema = schemaParser.parse(configuraionSchema.getRawSchema());

        GenericRecord recordNew1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew1.put("intField", 4);
        GenericFixed uuidNew1 = AvroUtils.generateUuidObject();
        recordNew1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord recordNew2 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew2.put("intField", 5);
        recordNew2.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord rootNew = new GenericData.Record(avroSchema);
        GenericArray arrayNew = new GenericData.Array<>(2, rootNew.getSchema().getField("complexArrayField").schema());
        arrayNew.add(recordNew1);
        arrayNew.add(recordNew2);
        rootNew.put("complexArrayField", arrayNew);

        GenericRecord recordOld1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld1.put("intField", 6);
        recordOld1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord recordOld2 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld2.put("intField", 7);
        GenericFixed uuidOld2 = AvroUtils.generateUuidObject();
        recordOld2.put(CommonConstants.UUID_FIELD, uuidOld2);

        GenericRecord rootOld = new GenericData.Record(avroSchema);
        GenericArray arrayOld = new GenericData.Array<>(2, rootOld.getSchema().getField("complexArrayField").schema());
        arrayOld.add(recordOld1);
        arrayOld.add(recordOld2);
        rootOld.put("complexArrayField", arrayOld);
        rootOld.put(CommonConstants.UUID_FIELD, AvroUtils.generateUuidObject());

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBodyNew = converter.encodeToJson(rootNew);
        String configurationBodyOld = converter.encodeToJson(rootOld);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(rootNew, rootOld);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        GenericArray processedArray = (GenericArray) processedConfiguration.get("complexArrayField");
        GenericRecord record1 = (GenericRecord) processedArray.get(0);
        GenericRecord record2 = (GenericRecord) processedArray.get(1);
        Assert.assertEquals(uuidNew1, record1.get(CommonConstants.UUID_FIELD));
        Assert.assertNotEquals(uuidNew1, record2.get(CommonConstants.UUID_FIELD));
        Assert.assertNotNull(processedConfiguration.get(CommonConstants.UUID_FIELD));
    }

    @Test
    public void testValidationOfComplexTypes() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/simpleComplexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema.Parser schemaParser = new Schema.Parser();
        Schema avroSchema = schemaParser.parse(configuraionSchema.getRawSchema());

        GenericRecord recordNew1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew1.put("intField", 4);
        GenericFixed uuidNew1 = AvroUtils.generateUuidObject();
        recordNew1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord rootNew = new GenericData.Record(avroSchema);
        rootNew.put("recordField", recordNew1);

        GenericRecord recordOld1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordOld1.put("intField", 6);
        recordOld1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord rootOld = new GenericData.Record(avroSchema);
        rootOld.put("recordField", recordOld1);
        rootOld.put(CommonConstants.UUID_FIELD, AvroUtils.generateUuidObject());

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBodyNew = converter.encodeToJson(rootNew);
        String configurationBodyOld = converter.encodeToJson(rootOld);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(rootNew, rootOld);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        GenericRecord processedRecord = (GenericRecord) processedConfiguration.get("recordField");
        Assert.assertEquals(uuidNew1, processedRecord.get(CommonConstants.UUID_FIELD));
        Assert.assertNotNull(processedConfiguration.get(CommonConstants.UUID_FIELD));
    }

    @Test
    public void testValidationOfComplexWithoutOldConfiguration() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/simpleComplexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));
        Schema.Parser schemaParser = new Schema.Parser();
        Schema avroSchema = schemaParser.parse(configuraionSchema.getRawSchema());

        GenericRecord recordNew1 = new GenericData.Record(schemaParser.getTypes().get("org.kaaproject.recordT"));
        recordNew1.put("intField", 4);
        GenericFixed uuidNew1 = AvroUtils.generateUuidObject();
        recordNew1.put(CommonConstants.UUID_FIELD, uuidNew1);

        GenericRecord rootNew = new GenericData.Record(avroSchema);
        rootNew.put("recordField", recordNew1);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(avroSchema);
        String configurationBodyNew = converter.encodeToJson(rootNew);

        DefaultUuidValidator uuidGenerator = new DefaultUuidValidator(configuraionSchema, new BaseDataFactory());
        KaaData processedConfigurationBody = uuidGenerator.validateUuidFields(rootNew, null);

        GenericRecord processedConfiguration = converter.decodeJson(processedConfigurationBody.getRawData());
        GenericRecord processedRecord = (GenericRecord) processedConfiguration.get("recordField");
        Assert.assertNotEquals(uuidNew1, processedRecord.get(CommonConstants.UUID_FIELD));
        Assert.assertNotNull(processedConfiguration.get(CommonConstants.UUID_FIELD));
    }

}


