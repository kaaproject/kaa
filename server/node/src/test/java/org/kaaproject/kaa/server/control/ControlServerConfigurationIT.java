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

package org.kaaproject.kaa.server.control;

import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDataStruct;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toDtoList;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDto;
import static org.kaaproject.kaa.server.common.thrift.util.ThriftDtoConverter.toGenericDtoList;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationDto;
import org.kaaproject.kaa.common.dto.ConfigurationRecordDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerConfigurationIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerConfigurationIT.class);

    /**
     * Test create configuration.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetConfigurationRecord() throws Exception {
        ConfigurationDto configuration = createConfiguration();

        ConfigurationRecordDto configurationRecord = client.getConfigurationRecord(configuration.getSchemaId(), configuration.getEndpointGroupId());

        Assert.assertNotNull(configurationRecord);
        Assert.assertNotNull(configurationRecord.getInactiveConfiguration());
        assertConfigurationsEquals(configuration, configurationRecord.getInactiveConfiguration());
    }

    /**
     * Test get configuration records by endpoint group id.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
        assertConfigurationsEquals(configuration1, configurationRecords.get(0).getInactiveConfiguration());
        assertConfigurationsEquals(configuration2, configurationRecords.get(1).getInactiveConfiguration());
    }

    /**
     * Test delete configuration record
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
        assertConfigurationsEquals(configuration1, configurationRecords.get(0).getInactiveConfiguration());

        client.deleteConfigurationRecord(configuration1.getSchemaId(), endpointGroup.getId());
        configurationRecords = client.getConfigurationRecords(endpointGroup.getId(), false);
        Assert.assertNotNull(configurationRecords);
        Assert.assertEquals(0, configurationRecords.size());

    }

    /**
     * Test get vacant schemas by endpoint group id.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetVacantSchemasByEndpointGroupId() throws Exception {

        EndpointGroupDto endpointGroup = createEndpointGroup();
        ConfigurationSchemaDto configurationSchema1 = createConfigurationSchema(endpointGroup.getApplicationId());
        ConfigurationSchemaDto configurationSchema2 = createConfigurationSchema(endpointGroup.getApplicationId());
        ConfigurationSchemaDto configurationSchema3 = createConfigurationSchema(endpointGroup.getApplicationId());

        createConfiguration(configurationSchema1.getId(), endpointGroup.getId());
        createConfiguration(configurationSchema2.getId(), endpointGroup.getId());

        List<SchemaDto> schemas = client.getVacantConfigurationSchemasByEndpointGroupId(endpointGroup.getId());

        Assert.assertNotNull(schemas);
        Assert.assertEquals(2, schemas.size());
        Collections.sort(schemas, new Comparator<SchemaDto>() {
            @Override
            public int compare(SchemaDto o1, SchemaDto o2) {
                int result = o1.getMajorVersion() - o2.getMajorVersion();
                if (result == 0) {
                    result = o1.getMinorVersion() - o2.getMinorVersion();
                }
                return result;
            }
        });
        SchemaDto schema = schemas.get(1);
        Assert.assertNotNull(schema);
        Assert.assertEquals(configurationSchema3.getId(), schema.getId());
        Assert.assertEquals(configurationSchema3.getMajorVersion(), schema.getMajorVersion());
        Assert.assertEquals(configurationSchema3.getMinorVersion(), schema.getMinorVersion());
    }

    /**
     * Test update configuration.
     *
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
