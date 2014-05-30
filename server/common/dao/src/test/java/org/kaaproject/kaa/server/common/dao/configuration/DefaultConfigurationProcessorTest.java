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

package org.kaaproject.kaa.server.common.dao.configuration;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

import org.apache.avro.generic.GenericRecord;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;

/**
 * Class for testing
 * {@link org.kaaproject.kaa.server.common.dao.configuration.DefaultConfigurationProcessor}
 */
public class DefaultConfigurationProcessorTest {

    /**
     * Checks generation of a complex default configuration.
     *
     * @throws Exception
     */
    @Test
    public void testGenerateComplexDefaultConfigurationSuccess() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/complexSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        String generatedConfiguration = configurationProcessor.getRootJsonConfiguration();

        // Read expected generated Configuration
        Path expectedGeneratedConfigurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/expectedDefaultConfigurationForComplexSchema.json").toURI());
        String expectedGeneratedConfiguration = new String(Files.readAllBytes(expectedGeneratedConfigurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema);

        GenericRecord generatedConfigurationGenericRecord = converter.decodeJson(generatedConfiguration);
        GenericRecord expectedGeneratedConfigurationGenericRecord = converter.decodeJson(expectedGeneratedConfiguration);

        Assert.assertTrue(generatedConfigurationGenericRecord.equals(expectedGeneratedConfigurationGenericRecord));
    }

    /**
     * Verifies that configuration generation fails when schema contains field
     * of type map.
     *
     * @throws Exception
     */
    @Test(expected = ConfigurationProcessingException.class)
    public void testConfigurationGenerationFailedForSchemaWithMapField() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/schemaWithMapType.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        configurationProcessor.getRootJsonConfiguration();
    }

    /**
     * Verifies that configuration generation fails when schema is corrupted.
     *
     * @throws Exception
     */
    @Test(expected = ConfigurationProcessingException.class)
    public void testConfigurationGenerationFailedForCorruptedSchema() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/corruptedSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        configurationProcessor.getRootJsonConfiguration();
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
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/schemaWithFixedType.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        String generatedConfiguration = configurationProcessor.getRootJsonConfiguration();

        // Read expected generated Configuration
        Path expectedGeneratedConfigurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/expectedDefaultConfigurationWithFixedType.json").toURI());
        String expectedGeneratedConfiguration = new String(Files.readAllBytes(expectedGeneratedConfigurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema);

        GenericRecord generatedConfigurationGenericRecord = converter.decodeJson(generatedConfiguration);
        GenericRecord expectedGeneratedConfigurationGenericRecord = converter.decodeJson(expectedGeneratedConfiguration);

        Assert.assertTrue(generatedConfigurationGenericRecord.equals(expectedGeneratedConfigurationGenericRecord));
    }

    /**
     * Checks generation fails for schema with missed 'by_default' option.
     *
     * @throws Exception
     */
    @Test(expected = ConfigurationProcessingException.class)
    public void testGenerateDefaultConfigurationFailsForSchemaWithMissedByDefault() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/schemaWithMissedByDefault.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        configurationProcessor.getRootJsonConfiguration();
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
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/schemaWithReusedType.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        String generatedConfiguration = configurationProcessor.getRootJsonConfiguration();

        // Read expected generated Configuration
        Path expectedGeneratedConfigurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/expectedDefaultConfigurationForSchemaWithReusedType.json").toURI());
        String expectedGeneratedConfiguration = new String(Files.readAllBytes(expectedGeneratedConfigurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema);

        GenericRecord generatedConfigurationGenericRecord = converter.decodeJson(generatedConfiguration);
        GenericRecord expectedGeneratedConfigurationGenericRecord = converter.decodeJson(expectedGeneratedConfiguration);

        Assert.assertTrue(generatedConfigurationGenericRecord.equals(expectedGeneratedConfigurationGenericRecord));
    }

    @Test(expected = ConfigurationProcessingException.class)
    public void testGetRootJsonBytesConfigurationFailsWhenByDefauleIsMissed() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/schemaWithMissedByDefault.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        configurationProcessor.getRootJsonBytesConfiguration();
    }

    @Test(expected = ConfigurationProcessingException.class)
    public void testGetRootBinaryConfigurationFailsWhenByDefauleIsMissed() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/schemaWithMissedByDefault.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        configurationProcessor.getRootBinaryConfiguration();
    }

    @Test
    public void testGenerateUuidFields() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/simpleSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        Path configurationPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/simpleConfiguration.json").toURI());
        String configuraion = new String(Files.readAllBytes(configurationPath));

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(configuraionSchema);
        byte[] configurationBody = converter.encode(converter.decodeJson(configuraion));

        // generated default configuration
        DefaultConfigurationProcessorStub configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        byte[] processedConfigurationBody = configurationProcessor.generateUuidFields(configurationBody);

        String processedConfigurationJson = converter.endcodeToJson(converter.decodeBinary(processedConfigurationBody));
        ObjectMapper jsonMapper = new ObjectMapper();
        Map<String, Object> processedConfigurationMap = jsonMapper.readValue(processedConfigurationJson, Map.class);

        Object uuid = processedConfigurationMap.get("__uuid");
        Assert.assertNotNull(uuid);
        Assert.assertTrue(uuid instanceof Map);

        Object uuidValue = ((Map) uuid).get("org.kaaproject.configuration.uuidT");
        Assert.assertNotNull(uuidValue);
        byte[] uuidValueBytes = uuidValue.toString().getBytes(Charset.forName("UTF-8"));

        Assert.assertTrue(Arrays.equals(uuidValueBytes, configurationProcessor.generateUUIDBytes()));
    }

    @Test
    public void testGetConfigurationByNameForNullNameParameter() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/complexSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName(null, "org.kaa.config");

        Assert.assertNull(generatedConfiguration);
    }

    @Test
    public void testGetConfigurationByNameForNullNamespaceParameter() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/complexSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName("testT", null);

        Assert.assertNull(generatedConfiguration);
    }

    @Test
    public void testGetConfigurationByNameForMissedSchema() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/complexSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName("missed", "org.kaa.config");

        Assert.assertNull(generatedConfiguration);
    }

    @Test
    public void testGetConfigurationByNameForNestedType() throws Exception {
        // Read Configuration Schema
        Path schemaPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("dao/configuration/complexSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaPath));

        // generated default configuration
        ConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessorStub(configuraionSchema);
        configurationProcessor.getConfigurationByName("testT", "org.kaa.config");
        GenericRecord generatedConfiguration = configurationProcessor.getConfigurationByName("testRecordT", "org.kaa.config");

        Assert.assertNotNull(generatedConfiguration);
    }

    /**
     * Stub for DefaultConfigurationProcessor class to override
     * 'generateUUIDBytes' method because default implementation generates
     * random UUID but for testing purposes we need constant UUID value.
     */
    private static class DefaultConfigurationProcessorStub extends DefaultConfigurationProcessor {

        public DefaultConfigurationProcessorStub(String baseSchema)
                throws ConfigurationProcessingException {
            super(baseSchema);
        }

        @Override
        protected byte[] generateUUIDBytes() {
            byte[] uuidBytes = new byte[16];
            for (int i = 0; i < 16; i++) {
                uuidBytes[i] = ((byte) (i + 1));
            }
            return uuidBytes;
        }
    }
}
