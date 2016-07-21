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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

/**
 * The Class ControlServerConfigurationSchemaIT.
 */
public class ControlServerConfigurationSchemaIT extends AbstractTestControlServer {

    /**
     * Test create configuration schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateConfigurationSchema() throws Exception {
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema();
        Assert.assertFalse(strIsEmpty(configurationSchema.getId()));
        Assert.assertFalse(configurationSchema.getProtocolSchema().isEmpty());
    }

    /**
     * Test get configuration schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetConfigurationSchema() throws Exception {
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema();

        ConfigurationSchemaDto storedConfigurationSchema = client.getConfigurationSchema(configurationSchema.getId());

        Assert.assertNotNull(storedConfigurationSchema);
        assertConfigurationSchemasEquals(configurationSchema, storedConfigurationSchema);
    }

    /**
     * Test get configuration schemas by application token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetConfigurationSchemasByApplicationToken() throws Exception {

        List<ConfigurationSchemaDto> configurationSchemas  = new ArrayList<ConfigurationSchemaDto>(11);
        ApplicationDto application = createApplication(tenantAdminDto);

        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        List<ConfigurationSchemaDto> defaultConfigurationSchemas = client.getConfigurationSchemasByAppToken(application.getApplicationToken());
        configurationSchemas.addAll(defaultConfigurationSchemas);

        for (int i=0;i<10;i++) {
            ConfigurationSchemaDto configurationSchema = createConfigurationSchema(application.getId(), null);
            configurationSchemas.add(configurationSchema);
        }

        Collections.sort(configurationSchemas, new IdComparator());

        List<ConfigurationSchemaDto> storedConfigurationSchemas = client.getConfigurationSchemasByAppToken(application.getApplicationToken());

        Collections.sort(storedConfigurationSchemas, new IdComparator());

        Assert.assertEquals(configurationSchemas.size(), storedConfigurationSchemas.size());
        for (int i=0;i<configurationSchemas.size();i++) {
            ConfigurationSchemaDto configurationSchema = configurationSchemas.get(i);
            ConfigurationSchemaDto storedConfigurationSchema = storedConfigurationSchemas.get(i);
            Assert.assertEquals(configurationSchema.getId(), storedConfigurationSchema.getId());
            Assert.assertEquals(configurationSchema.getApplicationId(), storedConfigurationSchema.getApplicationId());
            Assert.assertEquals(configurationSchema.getStatus(), storedConfigurationSchema.getStatus());
        }
    }

    /**
     * Test get configuration schema versions by application Token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetConfigurationSchemaVersionsByApplicationToken() throws Exception {

        List<ConfigurationSchemaDto> configurationSchemas  = new ArrayList<ConfigurationSchemaDto>(11);
        ApplicationDto application = createApplication(tenantAdminDto);

        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        List<ConfigurationSchemaDto> defaultConfigurationSchemas = client.getConfigurationSchemasByAppToken(application.getApplicationToken());
        configurationSchemas.addAll(defaultConfigurationSchemas);

        for (int i=0;i<10;i++) {
            ConfigurationSchemaDto configurationSchema = createConfigurationSchema(application.getId(), null);
            configurationSchemas.add(configurationSchema);
        }

        Collections.sort(configurationSchemas, new IdComparator());

        SchemaVersions schemaVersions = client.getSchemaVersionsByApplicationToken(application.getApplicationToken());

        List<VersionDto> storedConfigurationSchemas = schemaVersions.getConfigurationSchemaVersions();

        Collections.sort(storedConfigurationSchemas, new IdComparator());

        Assert.assertEquals(configurationSchemas.size(), storedConfigurationSchemas.size());
        for (int i=0;i<configurationSchemas.size();i++) {
            ConfigurationSchemaDto configurationSchema = configurationSchemas.get(i);
            VersionDto storedConfigurationSchema = storedConfigurationSchemas.get(i);
            assertSchemasEquals(configurationSchema, storedConfigurationSchema);
        }
    }

    /**
     * Test update configuration schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateConfigurationSchema() throws Exception {
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema();

        configurationSchema.setName(generateString("Test Schema 2"));
        configurationSchema.setDescription(generateString("Test Desc 2"));

        ConfigurationSchemaDto updatedConfigurationSchema = client
                .saveConfigurationSchema(configurationSchema);

        Assert.assertTrue(updatedConfigurationSchema.equals(configurationSchema));
    }

    /**
     * Assert configuration schemas equals.
     *
     * @param configurationSchema the configuration schema
     * @param storedConfigurationSchema the stored configuration schema
     */
    private void assertConfigurationSchemasEquals(ConfigurationSchemaDto configurationSchema, ConfigurationSchemaDto storedConfigurationSchema) {
        Assert.assertEquals(configurationSchema.getId(), storedConfigurationSchema.getId());
        Assert.assertEquals(configurationSchema.getApplicationId(), storedConfigurationSchema.getApplicationId());
        Assert.assertEquals(configurationSchema.getCtlSchemaId(), storedConfigurationSchema.getCtlSchemaId());
        Assert.assertEquals(configurationSchema.getProtocolSchema(), storedConfigurationSchema.getProtocolSchema());
        Assert.assertEquals(configurationSchema.getStatus(), storedConfigurationSchema.getStatus());
    }

}
