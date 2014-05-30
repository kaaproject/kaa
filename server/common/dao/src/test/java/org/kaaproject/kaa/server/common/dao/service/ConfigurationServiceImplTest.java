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
import java.io.StringReader;
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
import org.kaaproject.kaa.server.common.dao.configuration.ConfigurationProcessingException;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.exception.UpdateStatusConflictException;
import org.kaaproject.kaa.server.common.dao.mongo.model.Configuration;
import org.kaaproject.kaa.server.common.dao.schema.BaseDataSchemaStrategy;
import org.kaaproject.kaa.server.common.dao.schema.OverrideDataSchemaStrategy;
import org.kaaproject.kaa.server.common.dao.schema.ProtocolSchemaStrategy;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreationException;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreator;
import org.kaaproject.kaa.server.common.dao.schema.SchemaCreatorImpl;
import org.kaaproject.kaa.server.common.dao.mongo.AbstractTest;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDBTestRunner;
import org.kaaproject.kaa.server.common.dao.mongo.MongoDataLoader;
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

    private static final String INCORRECT_ID = "7";

    @BeforeClass
    public static void init() throws Exception {
        MongoDBTestRunner.setUp();
    }

    @AfterClass
    public static void after() throws Exception {
        MongoDBTestRunner.getDB().dropDatabase();
        MongoDBTestRunner.tearDown();
    }

    @Before
    public void beforeTest() throws IOException {
        MongoDataLoader.loadData();
    }

    @After
    public void afterTest() {
        MongoDBTestRunner.getDB().dropDatabase();
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveConfigurationWithIncorrectIdTestFail() throws SchemaCreationException {
        ConfigurationDto configurationDto = new ConfigurationDto();
        configurationDto.setId(INCORRECT_ID);
        configurationService.saveConfiguration(configurationDto);
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void saveConfigurationWithIncorrectStatusTestFail() throws SchemaCreationException {
        ConfigurationDto configurationDto = configurationService.findConfigurationById(configurations.get(6));
        configurationService.saveConfiguration(configurationDto);
    }

    @Test
    public void saveConfigurationObjectWithIdTest() throws SchemaCreationException, IOException, ConfigurationProcessingException {
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
        ConfigurationDto configurationDto = configurationService.findConfigurationById(configurations.get(0));
        configurationDto.setId(null);
        configurationDto.setSchemaId(configurationDto.getApplicationId());
        configurationService.saveConfiguration(configurationDto);
    }

    @Test
    public void findConfSchemaByIdTest() {
        String id = schemas.get(0);
        ConfigurationSchemaDto dto = configurationService.findConfSchemaById(id);
        Assert.assertNotNull(dto);
    }

    @Test(expected = IncorrectParameterException.class)
    public void findConfSchemaByIdTestFail() {
        configurationService.findConfSchemaById(INCORRECT_ID);
    }

    @Test
    public void findLatestConfigurationByAppIdTest() {
        String applicationId = apps.get(0);
        ConfigurationDto configurationDto = configurationService.findConfigurationByAppIdAndVersion(applicationId, 1);
        Assert.assertNotNull(configurationDto);
    }

    @Test(expected = IncorrectParameterException.class)
    public void findLatestConfigurationByAppIdTestFail() {
        configurationService.findConfigurationByAppIdAndVersion(INCORRECT_ID, 1);
    }

    @Test
    public void findConfigurationByIdTest() {
        String id = configurations.get(0);
        ConfigurationDto configurationDto = configurationService.findConfigurationById(id);
        Assert.assertNotNull(configurationDto);
    }

    @Test(expected = IncorrectParameterException.class)
    public void findConfigurationByIdTestFail() {
        configurationService.findConfigurationById(INCORRECT_ID);
    }

    @Test
    public void activateConfiguration() {
        String id = configurations.get(7);
        ConfigurationDto found = configurationService.findConfigurationById(id);
        ChangeConfigurationNotification notification = configurationService.activateConfiguration(id, null);
        Assert.assertNotNull(notification);
        ConfigurationDto dto = notification.getConfigurationDto();
        Assert.assertNotNull(dto);
        Assert.assertEquals(dto.getStatus(), UpdateStatus.ACTIVE);
        Assert.assertEquals(dto.getId(), id);
        Assert.assertNotEquals(dto.getSequenceNumber(), found.getSequenceNumber());
    }

    @Test(expected = UpdateStatusConflictException.class)
    public void activateConfigurationTestFail() {
        configurationService.activateConfiguration(configurations.get(6), null);
    }

    @Test(expected = IncorrectParameterException.class)
    public void activateConfigurationWithIncorrectIdTestFail() {
        configurationService.activateConfiguration(INCORRECT_ID, null);
    }

    @Test
    public void findConfigurationsByEndpointGroupIdTest() {
        List<ConfigurationDto> dtoList = configurationService.findConfigurationsByEndpointGroupId(endGroups.get(0));
        Assert.assertNotNull(dtoList);
        Assert.assertFalse(dtoList.isEmpty());
    }

    @Test
    public void findConfSchemaByAppIdAndVersionTest() {
        String appId = apps.get(0);
        ConfigurationSchemaDto dto = configurationService.findConfSchemaByAppIdAndVersion(appId, 1);
        Assert.assertNotNull(dto);
    }

    @Test
    public void saveConfSchemaTest() throws SchemaCreationException, IOException {
        String id = schemas.get(0);
        ConfigurationSchemaDto dto = configurationService.findConfSchemaById(id);
        Assert.assertNotNull(dto);
        dto.setId(null);
        dto.setSchema(readSchemaFileAsString("dao/configuration/default_schema.json"));
        ConfigurationSchemaDto saved = configurationService.saveConfSchema(dto);
        Assert.assertNotNull(saved);
        Assert.assertNotEquals(dto.getId(), saved.getId());
        Assert.assertEquals(UpdateStatus.ACTIVE, dto.getStatus());
    }

    @Test
    public void removeConfSchemasByAppIdTest() {
        String appId = apps.get(0);
        List<ConfigurationSchemaDto> dtoList = configurationService.findConfSchemasByAppId(appId);
        Assert.assertNotNull(dtoList);
        Assert.assertFalse(dtoList.isEmpty());
        configurationService.removeConfSchemasByAppId(appId);
        dtoList = configurationService.findConfSchemasByAppId(appId);
        Assert.assertNotNull(dtoList);
        Assert.assertTrue(dtoList.isEmpty());
    }

    @Test
    public void saveMoreConfigurations() throws SchemaCreationException, IOException, ConfigurationProcessingException {
        ConfigurationSchemaDto schema = generateConfSchema(null, 1).get(0);
        generateConfiguration(schema.getId(), null, 9, true, false);
        ConfigurationDto config = configurationService.findConfigurationByAppIdAndVersion(schema.getApplicationId(), schema.getMajorVersion());
        Assert.assertEquals(UpdateStatus.ACTIVE, config.getStatus());
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
        String schema = readSchemaFileAsString("dao/schema/dataSchema.json");
        SchemaCreator protocolSchemaCreator = new SchemaCreatorImpl(new ProtocolSchemaStrategy());
        SchemaCreator overrideSchemaCreator = new SchemaCreatorImpl(new OverrideDataSchemaStrategy());
        SchemaCreator baseSchemaCreator = new SchemaCreatorImpl(new BaseDataSchemaStrategy());
        String protocolSchema = protocolSchemaCreator.createSchema(new StringReader(schema));
        String baseSchema = baseSchemaCreator.createSchema(new StringReader(schema));
        String overrideSchema = overrideSchemaCreator.createSchema(new StringReader(schema));
        LOGGER.debug("Created Override schema JSON {} ", overrideSchema);
        LOGGER.debug("Created Base schema JSON {} ", baseSchema);
        LOGGER.debug("Created Protocol schema JSON {} ", protocolSchema);
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
        configurationService.deactivateConfiguration(new ObjectId().toString(), null);
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
        Configuration inactive = configurationDao.findInactiveBySchemaIdAndGroupId(schemaDto.getId(), group.getId());
        Assert.assertNull(inactive);
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
