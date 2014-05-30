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

package org.kaaproject.kaa.server.operations.service.delta.merge;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.server.common.dao.schema.BaseDataSchemaStrategy;
import org.kaaproject.kaa.server.common.dao.schema.OverrideDataSchemaStrategy;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreator;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreatorImpl;
import org.kaaproject.kaa.server.operations.service.delta.merge.ConfigurationMerger;
import org.kaaproject.kaa.server.operations.service.delta.merge.DefaultConfigurationMerger;
import org.kaaproject.kaa.server.operations.service.delta.merge.MergeException;

public class DefaultConfigurationMergerTest {

    @Test
    public void testMergeOf3Configurations() throws Exception {
        // Create Configuration Schema
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));
        String overrideSchemaString = overrideSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(baseSchemaString);
        configurationSchema.setOverrideSchema(overrideSchemaString);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(baseSchemaString);

        // Create Endpoint Group weight 0
        EndpointGroupDto endpointGroupWeight0 = new EndpointGroupDto();
        endpointGroupWeight0.setId("group_id_0");
        endpointGroupWeight0.setWeight(0);

        // Create Configuration weight 0
        Path configuraionWeight0Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_0.json").toURI());

        ConfigurationDto configurationWeight0 = new ConfigurationDto();
        configurationWeight0.setEndpointGroupId(endpointGroupWeight0.getId());
        configurationWeight0.setSchemaId(configurationSchema.getId());
        configurationWeight0.setBinaryBody(Files.readAllBytes(configuraionWeight0Path));

        // Create Endpoint Group weight 1
        EndpointGroupDto endpointGroupWeight1 = new EndpointGroupDto();
        endpointGroupWeight1.setId("group_id_1");
        endpointGroupWeight1.setWeight(1);

        // Create Configuration weight 1
        Path configuraionWeight1Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_1.json").toURI());

        ConfigurationDto configurationWeight1 = new ConfigurationDto();
        configurationWeight1.setEndpointGroupId(endpointGroupWeight1.getId());
        configurationWeight1.setSchemaId(configurationSchema.getId());
        configurationWeight1.setBinaryBody(Files.readAllBytes(configuraionWeight1Path));

        // Create Endpoint Group weight 2
        EndpointGroupDto endpointGroupWeight2 = new EndpointGroupDto();
        endpointGroupWeight2.setId("group_id_2");
        endpointGroupWeight2.setWeight(2);

        // Create Configuration weight 2
        Path configuraionWeight2Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_2.json").toURI());

        ConfigurationDto configurationWeight2 = new ConfigurationDto();
        configurationWeight2.setEndpointGroupId(endpointGroupWeight2.getId());
        configurationWeight2.setSchemaId(configurationSchema.getId());
        configurationWeight2.setBinaryBody(Files.readAllBytes(configuraionWeight2Path));

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();
        endpointGroups.add(endpointGroupWeight0);
        endpointGroups.add(endpointGroupWeight1);
        endpointGroups.add(endpointGroupWeight2);

        List<ConfigurationDto> configurations = new ArrayList<>();
        configurations.add(configurationWeight0);
        configurations.add(configurationWeight1);
        configurations.add(configurationWeight2);

        // Create Configuration weight 2
        Path mergedConfiguraionPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/merged_config.json").toURI());
        String mergedConfiguraionBody = new String(Files.readAllBytes(mergedConfiguraionPath));

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        byte[] mergeResult = merger.merge(endpointGroups, configurations, configurationSchema);

        GenericRecord mergeResultGenericRecord = converter.decodeJson(mergeResult);
        GenericRecord expectedMergedConfiguraionGenericRecord = converter.decodeJson(mergedConfiguraionBody);

        Assert.assertTrue(mergeResultGenericRecord.equals(expectedMergedConfiguraionGenericRecord));
    }

    @Test
    public void testMergeWhenEndpointGroupsIsEmpty() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));
        String overrideSchemaString = overrideSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(baseSchemaString);
        configurationSchema.setOverrideSchema(overrideSchemaString);

        // Create Endpoint Group weight 0
        EndpointGroupDto endpointGroupWeight0 = new EndpointGroupDto();
        endpointGroupWeight0.setId("group_id_0");
        endpointGroupWeight0.setWeight(0);

        // Create Configuration weight 0
        Path configuraionWeight0Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_0.json").toURI());

        ConfigurationDto configurationWeight0 = new ConfigurationDto();
        configurationWeight0.setEndpointGroupId(endpointGroupWeight0.getId());
        configurationWeight0.setSchemaId(configurationSchema.getId());
        configurationWeight0.setBinaryBody(Files.readAllBytes(configuraionWeight0Path));

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();

        List<ConfigurationDto> configurations = new ArrayList<>();
        configurations.add(configurationWeight0);

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        byte[] mergeResult = merger.merge(endpointGroups, configurations, configurationSchema);

        Assert.assertNull(mergeResult);
    }

    @Test
    public void testMergeWhenConfigurationsIsEmpty() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));
        String overrideSchemaString = overrideSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(baseSchemaString);
        configurationSchema.setOverrideSchema(overrideSchemaString);

        // Create Endpoint Group weight 0
        EndpointGroupDto endpointGroupWeight0 = new EndpointGroupDto();
        endpointGroupWeight0.setId("group_id_0");
        endpointGroupWeight0.setWeight(0);

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();
        endpointGroups.add(endpointGroupWeight0);

        List<ConfigurationDto> configurations = new ArrayList<>();

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        byte[] mergeResult = merger.merge(endpointGroups, configurations, configurationSchema);

        Assert.assertNull(mergeResult);
    }

    @Test
    public void testMergeWhenEndpointGroupsAndConfigurationsAreEmpty() throws Exception {
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));
        String overrideSchemaString = overrideSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(baseSchemaString);
        configurationSchema.setOverrideSchema(overrideSchemaString);

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();

        List<ConfigurationDto> configurations = new ArrayList<>();

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        byte[] mergeResult = merger.merge(endpointGroups, configurations, configurationSchema);

        Assert.assertNull(mergeResult);
    }

    @Test(expected = MergeException.class)
    public void testMergeWhenEndpointGroupsIsNotFoundForConfiguration() throws Exception {
        // Create Configuration Schema
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));
        String overrideSchemaString = overrideSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(baseSchemaString);
        configurationSchema.setOverrideSchema(overrideSchemaString);

        // Create Endpoint Group weight 0
        EndpointGroupDto endpointGroupWeight0 = new EndpointGroupDto();
        endpointGroupWeight0.setId("group_id_0");
        endpointGroupWeight0.setWeight(0);

        // Create Configuration weight 0
        Path configuraionWeight0Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_0.json").toURI());

        ConfigurationDto configurationWeight0 = new ConfigurationDto();
        configurationWeight0.setEndpointGroupId(endpointGroupWeight0.getId());
        configurationWeight0.setSchemaId(configurationSchema.getId());
        configurationWeight0.setBinaryBody(Files.readAllBytes(configuraionWeight0Path));

        // Create Endpoint Group weight 1
        EndpointGroupDto endpointGroupWeight1 = new EndpointGroupDto();
        endpointGroupWeight1.setId("group_id_1");
        endpointGroupWeight1.setWeight(1);

        // Create Configuration weight 1
        Path configuraionWeight1Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_1.json").toURI());

        ConfigurationDto configurationWeight1 = new ConfigurationDto();
        configurationWeight1.setEndpointGroupId(endpointGroupWeight1.getId());
        configurationWeight1.setSchemaId(configurationSchema.getId());
        configurationWeight1.setBinaryBody(Files.readAllBytes(configuraionWeight1Path));

        // Create Endpoint Group weight 2
        EndpointGroupDto endpointGroupWeight2 = new EndpointGroupDto();
        endpointGroupWeight2.setId("group_id_2");
        endpointGroupWeight2.setWeight(2);

        // Create Configuration weight 2
        Path configuraionWeight2Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_2.json").toURI());

        ConfigurationDto configurationWeight2 = new ConfigurationDto();
        configurationWeight2.setEndpointGroupId(endpointGroupWeight2.getId());
        configurationWeight2.setSchemaId(configurationSchema.getId());
        configurationWeight2.setBinaryBody(Files.readAllBytes(configuraionWeight2Path));

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();
        endpointGroups.add(endpointGroupWeight0);
        endpointGroups.add(endpointGroupWeight1);

        List<ConfigurationDto> configurations = new ArrayList<>();
        configurations.add(configurationWeight0);
        configurations.add(configurationWeight1);
        configurations.add(configurationWeight2);

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        merger.merge(endpointGroups, configurations, configurationSchema);
    }

    @Test(expected = MergeException.class)
    public void testMergeFailsWhenBaseSchemaIsCorrupted() throws Exception {
        // Create Configuration Schema
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/corruptedSchema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(configuraionSchema);
        configurationSchema.setOverrideSchema(configuraionSchema);

        // Create Endpoint Group weight 0
        EndpointGroupDto endpointGroupWeight0 = new EndpointGroupDto();
        endpointGroupWeight0.setId("group_id_0");
        endpointGroupWeight0.setWeight(0);

        // Create Configuration weight 0
        Path configuraionWeight0Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_0.json").toURI());

        ConfigurationDto configurationWeight0 = new ConfigurationDto();
        configurationWeight0.setEndpointGroupId(endpointGroupWeight0.getId());
        configurationWeight0.setSchemaId(configurationSchema.getId());
        configurationWeight0.setBinaryBody(Files.readAllBytes(configuraionWeight0Path));

        // Create Endpoint Group weight 1
        EndpointGroupDto endpointGroupWeight1 = new EndpointGroupDto();
        endpointGroupWeight1.setId("group_id_1");
        endpointGroupWeight1.setWeight(1);

        // Create Configuration weight 1
        Path configuraionWeight1Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_1.json").toURI());

        ConfigurationDto configurationWeight1 = new ConfigurationDto();
        configurationWeight1.setEndpointGroupId(endpointGroupWeight1.getId());
        configurationWeight1.setSchemaId(configurationSchema.getId());
        configurationWeight1.setBinaryBody(Files.readAllBytes(configuraionWeight1Path));

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();
        endpointGroups.add(endpointGroupWeight0);
        endpointGroups.add(endpointGroupWeight1);

        List<ConfigurationDto> configurations = new ArrayList<>();
        configurations.add(configurationWeight0);
        configurations.add(configurationWeight1);

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        merger.merge(endpointGroups, configurations, configurationSchema);
    }

    @Test
    public void testMergeSuccessWhenEndpointGroupHasNoConfiguration() throws Exception {
        // Create Configuration Schema
        Path schemaUrl = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/schema.json").toURI());
        String configuraionSchema = new String(Files.readAllBytes(schemaUrl));

        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());

        String baseSchemaString = baseSchemaCreator.createSchema(new StringReader(configuraionSchema));
        String overrideSchemaString = overrideSchemaCreator.createSchema(new StringReader(configuraionSchema));

        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setId("configuration_schema_0");
        configurationSchema.setSchema(configuraionSchema);
        configurationSchema.setBaseSchema(baseSchemaString);
        configurationSchema.setOverrideSchema(overrideSchemaString);

        GenericAvroConverter<GenericRecord> converter = new GenericAvroConverter<>(baseSchemaString);

        // Create Endpoint Group weight 0
        EndpointGroupDto endpointGroupWeight0 = new EndpointGroupDto();
        endpointGroupWeight0.setId("group_id_0");
        endpointGroupWeight0.setWeight(0);

        // Create Configuration weight 0
        Path configuraionWeight0Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_0.json").toURI());

        ConfigurationDto configurationWeight0 = new ConfigurationDto();
        configurationWeight0.setEndpointGroupId(endpointGroupWeight0.getId());
        configurationWeight0.setSchemaId(configurationSchema.getId());
        configurationWeight0.setBinaryBody(Files.readAllBytes(configuraionWeight0Path));

        // Create Endpoint Group weight 1
        EndpointGroupDto endpointGroupWeight1 = new EndpointGroupDto();
        endpointGroupWeight1.setId("group_id_1");
        endpointGroupWeight1.setWeight(1);

        // Create Configuration weight 1
        Path configuraionWeight1Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_1.json").toURI());

        ConfigurationDto configurationWeight1 = new ConfigurationDto();
        configurationWeight1.setEndpointGroupId(endpointGroupWeight1.getId());
        configurationWeight1.setSchemaId(configurationSchema.getId());
        configurationWeight1.setBinaryBody(Files.readAllBytes(configuraionWeight1Path));

        // Create Endpoint Group weight 2
        EndpointGroupDto endpointGroupWeight2 = new EndpointGroupDto();
        endpointGroupWeight2.setId("group_id_2");
        endpointGroupWeight2.setWeight(2);

        // Create Configuration weight 2
        Path configuraionWeight2Path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/config_weight_2.json").toURI());

        ConfigurationDto configurationWeight2 = new ConfigurationDto();
        configurationWeight2.setEndpointGroupId(endpointGroupWeight2.getId());
        configurationWeight2.setSchemaId(configurationSchema.getId());
        configurationWeight2.setBinaryBody(Files.readAllBytes(configuraionWeight2Path));

        // Create Endpoint Group weight 3
        EndpointGroupDto endpointGroupWeight3 = new EndpointGroupDto();
        endpointGroupWeight3.setId("group_id_3");
        endpointGroupWeight3.setWeight(3);

        List<EndpointGroupDto> endpointGroups = new ArrayList<>();
        endpointGroups.add(endpointGroupWeight0);
        endpointGroups.add(endpointGroupWeight1);
        endpointGroups.add(endpointGroupWeight2);
        endpointGroups.add(endpointGroupWeight3);

        List<ConfigurationDto> configurations = new ArrayList<>();
        configurations.add(configurationWeight0);
        configurations.add(configurationWeight1);
        configurations.add(configurationWeight2);

        // Create Configuration weight 2
        Path mergedConfiguraionPath = Paths.get(Thread.currentThread().getContextClassLoader().getResource("service/delta/merge/merged_config.json").toURI());
        String mergedConfiguraionBody = new String(Files.readAllBytes(mergedConfiguraionPath));

        ConfigurationMerger merger = new DefaultConfigurationMerger();
        byte[] mergeResult = merger.merge(endpointGroups, configurations, configurationSchema);

        GenericRecord mergeResultGenericRecord = converter.decodeJson(mergeResult);
        GenericRecord expectedMergedConfiguraionGenericRecord = converter.decodeJson(mergedConfiguraionBody);

        Assert.assertTrue(mergeResultGenericRecord.equals(expectedMergedConfiguraionGenericRecord));
    }
}
