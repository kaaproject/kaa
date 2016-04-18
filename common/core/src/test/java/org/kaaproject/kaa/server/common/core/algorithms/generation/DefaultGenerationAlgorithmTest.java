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

package org.kaaproject.kaa.server.common.core.algorithms.generation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroDataCanonizationUtils;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.server.common.core.configuration.BaseDataFactory;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;
import org.kaaproject.kaa.server.common.core.schema.BaseSchema;

/**
 * Class for testing
 * {@link org.kaaproject.kaa.server.common.dao.configuration.DefaultConfigurationProcessor}
 */
public class DefaultGenerationAlgorithmTest {

    /**
     * Checks generation of a complex default configuration.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateComplexDefaultConfigurationSuccess() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/complexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        KaaData generatedConfiguration = configurationProcessor.getRootData();

        // Read expected generated Configuration
        Path expectedGeneratedConfigurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/expectedDefaultConfigurationForComplexSchema.json").toURI());
        String expectedGeneratedConfiguration = new String(Files.readAllBytes(expectedGeneratedConfigurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema.getRawSchema());

        GenericRecord generatedConfigurationGenericRecord = converter.decodeJson(generatedConfiguration.getRawData());
        GenericRecord expectedGeneratedConfigurationGenericRecord = converter.decodeJson(expectedGeneratedConfiguration);

        AvroDataCanonizationUtils.removeUuid(generatedConfigurationGenericRecord);
        AvroDataCanonizationUtils.removeUuid(expectedGeneratedConfigurationGenericRecord);

        Assert.assertTrue(generatedConfigurationGenericRecord.equals(expectedGeneratedConfigurationGenericRecord));
    }

    /**
     * Verifies that configuration generation fails when schema contains field
     * of type map.
     *
     * @throws Exception
     */
    @Test(expected = ConfigurationGenerationException.class)
    public void testConfigurationGenerationFailedForSchemaWithMapField() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/schemaWithMapType.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        KaaData generatedConfiguration = configurationProcessor.getRootData();
    }

    /**
     * Verifies that configuration generation fails when schema contains "by_default" field
     * doesn't match to the actual field type.
     *
     * @throws Exception
     */
    @Test(expected = ConfigurationGenerationException.class)
    public void testConfigurationGenerationFailedForSchemaWithUnsuitableDefault() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/schemaWithUnsuitableDefault.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        KaaData generatedConfiguration = configurationProcessor.getRootData();
    }

    /**
     * Checks generation of a default configuration for a schema with fixed
     * type.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateDefaultConfigurationForSchemaWithFixedType() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/schemaWithFixedType.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        KaaData generatedConfiguration = configurationProcessor.getRootData();

        // Read expected generated Configuration
        Path expectedGeneratedConfigurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/expectedDefaultConfigurationWithFixedType.json").toURI());
        String expectedGeneratedConfiguration = new String(Files.readAllBytes(expectedGeneratedConfigurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema.getRawSchema());

        GenericRecord generatedConfigurationGenericRecord = converter.decodeJson(generatedConfiguration.getRawData());
        GenericRecord expectedGeneratedConfigurationGenericRecord = converter.decodeJson(expectedGeneratedConfiguration);

        AvroDataCanonizationUtils.removeUuid(generatedConfigurationGenericRecord);
        AvroDataCanonizationUtils.removeUuid(expectedGeneratedConfigurationGenericRecord);

        Assert.assertTrue(generatedConfigurationGenericRecord.equals(expectedGeneratedConfigurationGenericRecord));
    }

    /**
     * Checks generation of a default configuration for a schema with reused
     * type.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateDefaultConfigurationForSchemaWithReusedType() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/schemaWithReusedType.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        KaaData generatedConfiguration = configurationProcessor.getRootData();

        // Read expected generated Configuration
        Path expectedGeneratedConfigurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/expectedDefaultConfigurationForSchemaWithReusedType.json").toURI());
        String expectedGeneratedConfiguration = new String(Files.readAllBytes(expectedGeneratedConfigurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema.getRawSchema());

        GenericRecord generatedConfigurationGenericRecord = converter.decodeJson(generatedConfiguration.getRawData());
        GenericRecord expectedGeneratedConfigurationGenericRecord = converter.decodeJson(expectedGeneratedConfiguration);

        AvroDataCanonizationUtils.removeUuid(generatedConfigurationGenericRecord);
        AvroDataCanonizationUtils.removeUuid(expectedGeneratedConfigurationGenericRecord);

        Assert.assertTrue(generatedConfigurationGenericRecord.equals(expectedGeneratedConfigurationGenericRecord));
    }

    @Test
    public void testGetRootBinaryConfigurationFailsWhenByDefauleIsMissed() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/schemaWithMissedByDefault.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        GenericRecord generatedConfiguration = configurationProcessor.getRootConfiguration();
        Assert.assertEquals(0, generatedConfiguration.get("testField5"));
    }

    @Test
    public void testGetConfigurationByNameForNullNameParameter() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/complexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName(null, "org.kaa.config");

        Assert.assertNull(generatedConfiguration);
    }

    @Test
    public void testGetConfigurationByNameForNullNamespaceParameter() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/complexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName("testT", null);

        Assert.assertNull(generatedConfiguration);
    }

    @Test
    public void testGetConfigurationByNameForMissedSchema() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/complexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName("missed", "org.kaa.config");

        Assert.assertNull(generatedConfiguration);
    }

    @Test
    public void testGetConfigurationByNameForNestedType() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation/complexSchema.json").toURI());
        BaseSchema configuraionSchema = new BaseSchema(new String(Files.readAllBytes(schemaPath)));

        // generated default configuration
        DefaultRecordGenerationAlgorithm configurationProcessor = new DefaultRecordGenerationAlgorithmImpl(configuraionSchema, new BaseDataFactory());
        configurationProcessor.getConfigurationByName("testRecordItemT", "org.kaa.config");
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName("testRecordItemT", "org.kaa.config");

        Assert.assertNotNull(generatedConfiguration);
        Assert.assertEquals(4, generatedConfiguration.get("testField4"));
    }

}
