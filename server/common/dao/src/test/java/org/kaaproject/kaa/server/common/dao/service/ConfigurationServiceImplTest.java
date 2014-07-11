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

package org.kaaproject.kaa.server.common.dao.service;

import java.io.IOException;
import java.util.List;

import org.apache.avro.generic.GenericContainer;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.avro.GenericAvroConverter;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ChangeConfigurationNotification;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.core.algorithms.generation.ConfigurationGenerationException;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaCreationException;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithm;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactory;
import org.kaaproject.kaa.server.common.core.algorithms.schema.SchemaGenerationAlgorithmFactoryImpl;
import org.kaaproject.kaa.server.common.core.schema.DataSchema;
import org.kaaproject.kaa.server.common.core.schema.KaaSchema;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.impl.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.impl.mongo.MongoDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/common-dao-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ConfigurationServiceImplTest extends AbstractTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImplTest.class);

    private static final String INCORRECT_SQL_ID = "incorrect id";

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveConfigurationWithIncorrectIdTestFail() throws SchemaCreationException {
        ConfigurationDto configurationDto = new ConfigurationDto();
        configurationDto.setId(INCORRECT_SQL_ID);
        configurationService.saveConfiguration(configurationDto);
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void saveConfigurationWithIncorrectStatusTestFail() throws SchemaCreationException {
        List<ConfigurationDto> configurations = generateConfiguration(null, null, 1, true, false);
        ConfigurationDto configurationDto = configurationService.findConfigurationById(configurations.get(0).getId());
        configurationService.saveConfiguration(configurationDto);
    }

    @Test
    public void saveConfigurationObjectWithIdTest() throws SchemaCreationException, IOException, ConfigurationGenerationException {
        List<ConfigurationDto> configs = generateConfiguration(null, null, 1, false, false);
        ConfigurationDto saved = configurationService.findConfigurationById(configs.get(0).getId());
        ConfigurationDto updated = configurationService.saveConfiguration(saved);
        Assert.assertNotNull(saved);
        Assert.assertEquals(updated.getStatus(), UpdateStatus.INACTIVE);
    }

    @Test
    public void saveConfigurationObjectWithoutIdTest() throws SchemaCreationException {
        List<ConfigurationDto> configs = generateConfiguration(null, null, 1, false, false);
        ConfigurationDto saved = configurationService.findConfigurationById(configs.get(0).getId());
        String inactiveId = saved.getId();
        saved.setId(null);
        ConfigurationDto updated = configurationService.saveConfiguration(saved);
        Assert.assertNotNull(saved);
        Assert.assertEquals(saved.getStatus(), UpdateStatus.INACTIVE);
        Assert.assertEquals(inactiveId, updated.getId());
    }

    @Test
    public void saveConfigurationObjectWithoutInactiveConfigurationTest() throws SchemaCreationException {
        List<ConfigurationDto> configs = generateConfiguration(null, null, 3, true, false);
        ConfigurationDto saved = configurationService.findConfigurationById(configs.get(2).getId());
        String id = saved.getId();
        saved.setId(null);
        ConfigurationDto updated = configurationService.saveConfiguration(saved);
        Assert.assertNotNull(updated);
        Assert.assertEquals(updated.getStatus(), UpdateStatus.INACTIVE);
        Assert.assertEquals(saved.getSequenceNumber(), updated.getSequenceNumber());
        Assert.assertNotEquals(id, updated.getId());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveConfigurationObjectWithIncorrectSchemaIdTest() throws SchemaCreationException {
        List<ConfigurationDto> configurations = generateConfiguration(null, null, 1, false, false);
        ConfigurationDto configurationDto = configurationService.findConfigurationById(configurations.get(0).getId());
        configurationDto.setId(null);
        configurationDto.setSchemaId(configurationDto.getApplicationId());
        configurationService.saveConfiguration(configurationDto);
    }

    @Test
    public void findConfSchemaByIdTest() {
        List<ConfigurationSchemaDto> schemas = generateConfSchema(null, 1);
        ConfigurationSchemaDto schema = schemas.get(0);
        ConfigurationSchemaDto foundSchema = configurationService.findConfSchemaById(schema.getId());
        Assert.assertNotNull(foundSchema);
        Assert.assertEquals(schema, foundSchema);
    }

    @Test(expected = IncorrectParameterException.class)
    public void findConfSchemaByIdTestFail() {
        configurationService.findConfSchemaById(INCORRECT_SQL_ID);
    }

    @Test
    public void findLatestConfigurationByAppIdTest() {
        List<ConfigurationDto> configurations = generateConfiguration(null, null, 1, false, false);
        ConfigurationDto expected = configurations.get(0);
        ConfigurationDto found = configurationService.findConfigurationByAppIdAndVersion(expected.getApplicationId(), 1);
        Assert.assertNotNull(found);
    }

    @Test(expected = IncorrectParameterException.class)
    public void findLatestConfigurationByAppIdTestFail() {
        configurationService.findConfigurationByAppIdAndVersion(INCORRECT_SQL_ID, 1);
    }

    @Test
    public void findConfigurationByIdTest() {
        List<ConfigurationDto> configurations = generateConfiguration(null, null, 1, false, false);
        ConfigurationDto configuration = configurations.get(0);
        ConfigurationDto foundConfiguration = configurationService.findConfigurationById(configuration.getId());
        Assert.assertNotNull(foundConfiguration);
        Assert.assertEquals(configuration, foundConfiguration);
    }

    @Test(expected = IncorrectParameterException.class)
    public void findConfigurationByIdTestFail() {
        configurationService.findConfigurationById(INCORRECT_SQL_ID);
    }

    @Test
    public void activateConfiguration() {
        List<ConfigurationDto> configurations = generateConfiguration(null, null, 1, false, false);
        String configId = configurations.get(0).getId();
        ConfigurationDto found = configurationService.findConfigurationById(configId);
        ChangeConfigurationNotification notification = configurationService.activateConfiguration(configId, null);
        Assert.assertNotNull(notification);
        ConfigurationDto dto = notification.getConfigurationDto();
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getStatus(), UpdateStatus.ACTIVE);
        Assert.assertEquals(dto.getId(), configId);
        Assert.assertNotEquals(dto.getSequenceNumber(), found.getSequenceNumber());
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void activateConfigurationTestFail() {
        List<ConfigurationDto> configurations = generateConfiguration(null, null, 1, true, false);
        ConfigurationDto configuration = configurations.get(0);
        configurationService.activateConfiguration(configuration.getId(), null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void activateConfigurationWithIncorrectIdTestFail() {
        configurationService.activateConfiguration(INCORRECT_SQL_ID, null);
    }

    @Test
    public void findConfigurationsByEndpointGroupIdTest() {
        EndpointGroupDto group = generateEndpointGroup(null);
        List<ConfigurationDto> configurations = generateConfiguration(null, group.getId(), 1, true, false);
        List<ConfigurationDto> dtoList = configurationService.findConfigurationsByEndpointGroupId(group.getId());
        Assert.assertNotNull(dtoList);
        Assert.assertEquals(configurations, dtoList);
    }

    @Test
    public void findConfSchemaByAppIdAndVersionTest() {
        String appId = generateApplication().getId();
        ConfigurationSchemaDto dto = configurationService.findConfSchemaByAppIdAndVersion(appId, 1);
        Assert.assertNotNull(dto);
    }

    @Test
    public void saveConfSchemaTest() throws SchemaCreationException, IOException {
        String id = generateConfSchema(null, 1).get(0).getId();
        ConfigurationSchemaDto schema = configurationService.findConfSchemaById(id);
        Assert.assertNotNull(schema);
        schema.setId(null);
        schema.setSchema(new DataSchema(readSchemaFileAsString("dao/configuration/default_schema.json")).getRawSchema());
        ConfigurationSchemaDto saved = configurationService.saveConfSchema(schema);
        Assert.assertNotNull(saved);
        Assert.assertNotEquals(schema.getId(), saved.getId());
    }

    @Test
    public void removeConfSchemasByAppIdTest() {
        ApplicationDto application = generateApplication();
        String appId = application.getId();
        List<ConfigurationSchemaDto> dtoList = configurationService.findConfSchemasByAppId(appId);
        Assert.assertNotNull(dtoList);
        Assert.assertFalse(dtoList.isEmpty());
        configurationService.removeConfSchemasByAppId(appId);
        dtoList = configurationService.findConfSchemasByAppId(appId);
        Assert.assertNotNull(dtoList);
        Assert.assertTrue(dtoList.isEmpty());
    }

    @Test
    public void createApplicationTest() throws IOException {
        String schema = readSchemaFileAsString("dao/schema/testOverrideSchema.json");
        String config = readSchemaFileAsString("dao/schema/testOverrideData.json");
        GenericAvroConverter<GenericContainer> converter = new GenericAvroConverter<GenericContainer>(schema);
        GenericContainer container = converter.decodeJson(config);
        LOGGER.debug("JSON {}", container);
        LOGGER.debug("Converted JSON {} ", new String(converter.encodeToJsonBytes(container)));
        Assert.assertEquals(converter.endcodeToJson(container), new String(converter.encodeToJsonBytes(container)));
    }

    @Test
    public void createSchemaTest() throws Exception {
        DataSchema schema = new DataSchema(readSchemaFileAsString("dao/schema/dataSchema.json"));
        SchemaGenerationAlgorithmFactory factory = new SchemaGenerationAlgorithmFactoryImpl();
        SchemaGenerationAlgorithm generator = factory.createSchemaGenerator(schema);
        KaaSchema protocolSchema = generator.getProtocolSchema();
        KaaSchema baseSchema = generator.getBaseSchema();
        KaaSchema overrideSchema = generator.getOverrideSchema();
        LOGGER.debug("Created Override schema JSON {} ", overrideSchema.getRawSchema());
        LOGGER.debug("Created Base schema JSON {} ", baseSchema.getRawSchema());
        LOGGER.debug("Created Protocol schema JSON {} ", protocolSchema.getRawSchema());
    }

    @Test
    public void createDefaultSchemaTest() {
        String id = generateApplication().getId();
        ConfigurationSchemaDto schema = generateConfSchema(id, 1).get(0);
        ConfigurationDto config = configurationService.findConfigurationByAppIdAndVersion(id, schema.getMajorVersion());
        Assert.assertEquals(config.getStatus(), UpdateStatus.ACTIVE);
    }

    @Test
    public void findDefaultConfigurationBySchemaIdTest() {
        ConfigurationSchemaDto schema = generateConfSchema(null, 1).get(0);
        ConfigurationDto configuration = configurationService.findDefaultConfigurationBySchemaId(schema.getId());
        Assert.assertNotNull(configuration);
        Assert.assertEquals(UpdateStatus.ACTIVE, configuration.getStatus());
        Assert.assertEquals(schema.getId(), configuration.getSchemaId());
    }

    @Test
    public void findConfigurationByEndpointGroupIdAndVersionTest() {
        ConfigurationSchemaDto schema = generateConfSchema(null, 1).get(0);
        String groupId = generateEndpointGroup(schema.getApplicationId()).getId();
        ConfigurationDto config = generateConfiguration(schema.getId(), groupId, 1, true, false).get(0);
        ConfigurationDto configuration = configurationService.findConfigurationByEndpointGroupIdAndVersion(groupId, schema.getMajorVersion());
        Assert.assertNotNull(configuration);
        Assert.assertEquals(config, configuration);
    }

    @Test(expected = IncorrectParameterException.class)
    public void deactivateInactiveConfigurationTest() {
        ConfigurationDto config = generateConfiguration(null, null, 1, false, false).get(0);
        configurationService.deactivateConfiguration(config.getId(), null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void deactivateIncorrectConfigurationTest() {
        configurationService.deactivateConfiguration(INCORRECT_SQL_ID, null);
    }

    @Test
    public void deactivateConfigurationTest() {
        ConfigurationDto config = generateConfiguration(null, null, 1, true, false).get(0);
        configurationService.deactivateConfiguration(config.getId(), null);
        config = configurationService.findConfigurationById(config.getId());
        Assert.assertNotNull(config);
        Assert.assertEquals(UpdateStatus.DEPRECATED, config.getStatus());
    }

    @Test
    public void deleteConfigurationRecordTest() {
        ConfigurationSchemaDto schemaDto = generateConfSchema(null, 1).get(0);
        EndpointGroupDto group = generateEndpointGroup(schemaDto.getApplicationId());
        generateConfiguration(schemaDto.getId(), group.getId(), 1, true, false);
        ChangeConfigurationNotification notification = configurationService.deleteConfigurationRecord(schemaDto.getId(), group.getId(), null);
        Assert.assertNotNull(notification);
        ConfigurationDto configurationDto = notification.getConfigurationDto();
        Assert.assertEquals(UpdateStatus.DEPRECATED, configurationDto.getStatus());
        StructureRecordDto<ConfigurationDto> records = configurationService.findConfigurationRecordBySchemaIdAndEndpointGroupId(schemaDto.getId(), group.getId());
        Assert.assertNull(records.getInactiveStructureDto());
        Assert.assertEquals(UpdateStatus.DEPRECATED, records.getActiveStructureDto().getStatus());
    }

    @Test
    public void findAllConfigurationRecordsByEndpointGroupIdTest() {
        String id = generateApplication().getId();
        ConfigurationSchemaDto schema = generateConfSchema(id, 1).get(0);
        EndpointGroupDto group = generateEndpointGroup(id);
        generateConfiguration(schema.getId(), group.getId(), 1, true, false);
        List<StructureRecordDto<ConfigurationDto>> records = (List<StructureRecordDto<ConfigurationDto>>) configurationService.findAllConfigurationRecordsByEndpointGroupId(group.getId(), false);
        Assert.assertNotNull(records);
        Assert.assertEquals(1, records.size());
        ConfigurationDto activeConfiguration = records.get(0).getActiveStructureDto();
        Assert.assertEquals(UpdateStatus.ACTIVE, activeConfiguration.getStatus());
        ConfigurationDto inactiveConfiguration = records.get(0).getInactiveStructureDto();
        Assert.assertNull(inactiveConfiguration);
    }

    @Test
    public void findConfigurationRecordBySchemaIdAndEndpointGroupIdTest() {
        ConfigurationSchemaDto schema = generateConfSchema(null, 1).get(0);
        EndpointGroupDto group = generateEndpointGroup(schema.getApplicationId());
        ConfigurationDto activeConfig = generateConfiguration(schema.getId(), group.getId(), 1, true, false).get(0);
        ConfigurationDto inactiveConfig = generateConfiguration(schema.getId(), group.getId(), 1, false, false).get(0);
        StructureRecordDto<ConfigurationDto> record = configurationService.findConfigurationRecordBySchemaIdAndEndpointGroupId(schema.getId(), group.getId());
        Assert.assertEquals(activeConfig, record.getActiveStructureDto());
        Assert.assertEquals(inactiveConfig, record.getInactiveStructureDto());
    }

    @Test
    public void findVacantSchemasByEndpointGroupIdTest() {
        ApplicationDto application = generateApplication();
        List<ConfigurationSchemaDto> schemas = generateConfSchema(application.getId(), 4);
        EndpointGroupDto groupOne = generateEndpointGroup(application.getId());
        ConfigurationSchemaDto schemaOne = schemas.get(0);
        generateConfiguration(schemaOne.getId(), groupOne.getId(), 1, true, false);
        EndpointGroupDto groupTwo = generateEndpointGroup(application.getId());

        List<SchemaDto> schemasOne = configurationService.findVacantSchemasByEndpointGroupId(groupOne.getId());
        Assert.assertFalse(schemasOne.isEmpty());
        Assert.assertEquals(4, schemasOne.size());

        List<SchemaDto> schemasTwo = configurationService.findVacantSchemasByEndpointGroupId(groupTwo.getId());
        Assert.assertFalse(schemasTwo.isEmpty());
        Assert.assertEquals(5, schemasTwo.size());
    }

    @Test
    public void findConfigurationSchemaVersionsByAppIdTest() {
        ConfigurationSchemaDto schemaDto = generateConfSchema(null, 1).get(0);
        List<SchemaDto> versions = configurationService.findConfigurationSchemaVersionsByAppId(schemaDto.getApplicationId());
        Assert.assertFalse(versions.isEmpty());
        Assert.assertEquals(2, versions.size());
        Assert.assertEquals(versions.get(0).getMajorVersion(), 1);
        Assert.assertEquals(versions.get(1).getMajorVersion(), 2);
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateConfigurationWithoutGroupIdTest() {
        ConfigurationDto configuration = new ConfigurationDto();
        configuration.setSchemaId(new ObjectId().toString());
        configurationService.saveConfiguration(configuration);
    }

    @Test(expected = IncorrectParameterException.class)
    public void validateConfigurationWithoutSchemaIdTest() {
        ConfigurationDto configuration = new ConfigurationDto();
        configurationService.saveConfiguration(configuration);
    }
}
