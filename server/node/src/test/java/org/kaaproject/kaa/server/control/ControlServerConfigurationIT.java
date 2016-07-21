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

package org.kaaproject.kaa.server.control;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;

/**
 * The Class ControlServerConfigurationIT.
 */
public class ControlServerConfigurationIT extends AbstractTestControlServer {

    private static final Charset DECODING_CHARSET = Charset.forName("ISO-8859-1");
    
    /**
     * Test create configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateConfiguration() throws Exception {
        ConfigurationDto configuration = createConfiguration();
        Assert.assertFalse(strIsEmpty(configuration.getId()));
        Assert.assertFalse(strIsEmpty(configuration.getApplicationId()));
        Assert.assertFalse(configuration.getProtocolSchema().isEmpty());
    }

    /**
     * Test get configuration record.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetConfigurationRecord() throws Exception {
        ConfigurationDto configuration = createConfiguration();

        ConfigurationRecordDto configurationRecord = client.getConfigurationRecord(configuration.getSchemaId(), configuration.getEndpointGroupId());

        Assert.assertNotNull(configurationRecord);
        Assert.assertNotNull(configurationRecord.getInactiveStructureDto());
        assertConfigurationsEquals(configuration, configurationRecord.getInactiveStructureDto());
    }
    
    /**
     * Test get configuration record body.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetConfigurationRecordBody() throws Exception {
        ConfigurationDto configuration = createConfiguration();
        ConfigurationDto activatedConfiguration = client.activateConfiguration(configuration.getId());
        ConfigurationRecordDto configurationRecord = client.getConfigurationRecord(activatedConfiguration.getSchemaId(), configuration.getEndpointGroupId());
        
        String configurationRecordBody = client.getConfigurationRecordBody(activatedConfiguration.getSchemaId(), activatedConfiguration.getEndpointGroupId());
        
        Assert.assertNotNull(configurationRecordBody);
        String expectedConfigurationRecordBody = new String(configurationRecord.getActiveStructureDto().getBody().getBytes(), DECODING_CHARSET);
        Assert.assertEquals(expectedConfigurationRecordBody, configurationRecordBody);
    }

    /**
     * Test get configuration records by endpoint group id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetConfigurationRecordsByEndpointGroupId() throws Exception {

        ApplicationDto application = createApplication(tenantAdminDto);

        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        
        EndpointGroupDto endpointGroup = createEndpointGroup(application.getId());

        ConfigurationDto configuration1 = createConfiguration(null, endpointGroup.getId(), application.getId());
        ConfigurationDto configuration2 = createConfiguration(null, endpointGroup.getId(), application.getId());

        List<ConfigurationRecordDto> configurationRecords = client.getConfigurationRecords(endpointGroup.getId(), false);

        Assert.assertNotNull(configurationRecords);
        Assert.assertEquals(2, configurationRecords.size());
        assertConfigurationsEquals(configuration1, configurationRecords.get(0).getInactiveStructureDto());
        assertConfigurationsEquals(configuration2, configurationRecords.get(1).getInactiveStructureDto());
    }

    /**
     * Test delete configuration record.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteConfigurationRecord() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();

        ConfigurationDto configuration1 = createConfiguration(null, endpointGroup.getId());
        ConfigurationDto configuration2 = createConfiguration(null, endpointGroup.getId());

        client.activateConfiguration(configuration2.getId());

        client.deleteConfigurationRecord(configuration2.getSchemaId(), endpointGroup.getId());

        List<ConfigurationRecordDto> configurationRecords = client.getConfigurationRecords(endpointGroup.getId(), false);

        Assert.assertNotNull(configurationRecords);
        Assert.assertEquals(1, configurationRecords.size());
        assertConfigurationsEquals(configuration1, configurationRecords.get(0).getInactiveStructureDto());

        client.deleteConfigurationRecord(configuration1.getSchemaId(), endpointGroup.getId());
        configurationRecords = client.getConfigurationRecords(endpointGroup.getId(), false);
        Assert.assertNotNull(configurationRecords);
        Assert.assertEquals(0, configurationRecords.size());

    }

    /**
     * Test get vacant schemas by endpoint group id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetVacantSchemasByEndpointGroupId() throws Exception {

        EndpointGroupDto endpointGroup = createEndpointGroup();
        ConfigurationSchemaDto configurationSchema1 = createConfigurationSchema(endpointGroup.getApplicationId(), null);
        ConfigurationSchemaDto configurationSchema2 = createConfigurationSchema(endpointGroup.getApplicationId(), null);
        ConfigurationSchemaDto configurationSchema3 = createConfigurationSchema(endpointGroup.getApplicationId(), null);

        createConfiguration(configurationSchema1.getId(), endpointGroup.getId());
        createConfiguration(configurationSchema2.getId(), endpointGroup.getId());

        List<VersionDto> schemas = client.getVacantConfigurationSchemasByEndpointGroupId(endpointGroup.getId());

        Assert.assertNotNull(schemas);
        Assert.assertEquals(2, schemas.size());
        Collections.sort(schemas);
        VersionDto schema = schemas.get(1);
        Assert.assertNotNull(schema);
        Assert.assertEquals(configurationSchema3.getId(), schema.getId());
        Assert.assertEquals(configurationSchema3.getVersion(), schema.getVersion());
    }

    /**
     * Test update configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateConfiguration() throws Exception {
        ConfigurationDto configuration = createConfiguration();

        String configUpdated = getResourceAsString(TEST_CONFIGURATION_UPDATED);

        configuration.setBody(configUpdated);

        ConfigurationDto updatedConfiguration = client
                .editConfiguration(configuration);

        Assert.assertNotEquals(updatedConfiguration, configuration);
    }

    /**
     * Test activate configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testActivateConfiguration() throws Exception {
        ConfigurationDto configuration = createConfiguration();
        ConfigurationDto activatedConfiguration = client.activateConfiguration(configuration.getId());

        Assert.assertEquals(configuration.getId(), activatedConfiguration.getId());
        Assert.assertEquals(configuration.getSchemaId(), activatedConfiguration.getSchemaId());
        Assert.assertEquals(configuration.getEndpointGroupId(), activatedConfiguration.getEndpointGroupId());
        Assert.assertEquals(configuration.getBody(), activatedConfiguration.getBody());
        Assert.assertEquals(configuration.getProtocolSchema(), activatedConfiguration.getProtocolSchema());
        Assert.assertEquals(configuration.getApplicationId(), activatedConfiguration.getApplicationId());
        Assert.assertEquals(activatedConfiguration.getStatus(), UpdateStatus.ACTIVE);
    }

    /**
     * Test deactivate configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeactivateConfiguration() throws Exception {
        ConfigurationDto configuration = createConfiguration();
        client.activateConfiguration(configuration.getId());
        ConfigurationDto deactivatedConfiguration = client.deactivateConfiguration(configuration.getId());

        Assert.assertEquals(configuration.getId(), deactivatedConfiguration.getId());
        Assert.assertEquals(configuration.getSchemaId(), deactivatedConfiguration.getSchemaId());
        Assert.assertEquals(configuration.getEndpointGroupId(), deactivatedConfiguration.getEndpointGroupId());
        Assert.assertEquals(configuration.getBody(), deactivatedConfiguration.getBody());
        Assert.assertEquals(configuration.getProtocolSchema(), deactivatedConfiguration.getProtocolSchema());
        Assert.assertEquals(configuration.getApplicationId(), deactivatedConfiguration.getApplicationId());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivatedConfiguration.getStatus());
    }

    /**
     * Assert configurations equals.
     *
     * @param configuration the configuration
     * @param storedConfiguration the stored configuration
     */
    private void assertConfigurationsEquals(ConfigurationDto configuration, ConfigurationDto storedConfiguration) {
        Assert.assertEquals(configuration.getId(), storedConfiguration.getId());
        Assert.assertEquals(configuration.getSchemaId(), storedConfiguration.getSchemaId());
        Assert.assertEquals(configuration.getEndpointGroupId(), storedConfiguration.getEndpointGroupId());
        Assert.assertEquals(configuration.getBody(), storedConfiguration.getBody());
        Assert.assertEquals(configuration.getProtocolSchema(), storedConfiguration.getProtocolSchema());
        Assert.assertEquals(configuration.getApplicationId(), storedConfiguration.getApplicationId());
        Assert.assertEquals(configuration.getStatus(), storedConfiguration.getStatus());
    }

}
