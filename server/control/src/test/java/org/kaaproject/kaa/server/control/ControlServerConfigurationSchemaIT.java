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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ConfigurationSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.server.common.thrift.gen.control.ControlThriftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerConfigurationSchemaIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerConfigurationSchemaIT.class);
    
    /**
     * Test create configuration schema.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateConfigurationSchema() throws TException, IOException {
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema();
        Assert.assertFalse(strIsEmpty(configurationSchema.getId()));
        Assert.assertFalse(strIsEmpty(configurationSchema.getProtocolSchema()));
    }
    
    /**
     * Test create invalid configuration schema.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test(expected = ControlThriftException.class)
    public void testCreateInvalidConfigurationSchema() throws TException, IOException {
        ConfigurationSchemaDto configurationSchema = new ConfigurationSchemaDto();
        configurationSchema.setStatus(UpdateStatus.ACTIVE);
        String schema = getResourceAsString(TEST_INVALID_CONFIG_SCHEMA);
        configurationSchema.setSchema(schema);
        ApplicationDto application = createApplication();
        configurationSchema.setApplicationId(application.getId());
        client.editConfigurationSchema(toDataStruct(configurationSchema));
    }
    
    /**
     * Test get configuration schema.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetConfigurationSchema() throws TException, IOException {
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema();
        
        ConfigurationSchemaDto storedConfigurationSchema = toDto(client.getConfigurationSchema(configurationSchema.getId()));
        
        Assert.assertNotNull(storedConfigurationSchema);
        assertConfigurationSchemasEquals(configurationSchema, storedConfigurationSchema);
    }
    
    /**
     * Test get configuration schemas by application id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetConfigurationSchemasByApplicationId() throws TException, IOException {
        
        List<ConfigurationSchemaDto> configurationSchemas  = new ArrayList<ConfigurationSchemaDto>(11);
        ApplicationDto application = createApplication();

        List<ConfigurationSchemaDto> defaultConfigurationSchemas = toDtoList(client.getConfigurationSchemasByApplicationId(application.getId()));
        configurationSchemas.addAll(defaultConfigurationSchemas);
        
        for (int i=0;i<10;i++) {
            ConfigurationSchemaDto configurationSchema = createConfigurationSchema(application.getId());
            configurationSchemas.add(configurationSchema);
        }
        
        Collections.sort(configurationSchemas, new IdComparator());
        
        List<ConfigurationSchemaDto> storedConfigurationSchemas = toDtoList(client.getConfigurationSchemasByApplicationId(application.getId()));

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
     * Test get configuration schema versions by application id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetConfigurationSchemaVersionsByApplicationId() throws TException, IOException {
        
        List<ConfigurationSchemaDto> configurationSchemas  = new ArrayList<ConfigurationSchemaDto>(11);
        ApplicationDto application = createApplication();

        List<ConfigurationSchemaDto> defaultConfigurationSchemas = toDtoList(client.getConfigurationSchemasByApplicationId(application.getId()));
        configurationSchemas.addAll(defaultConfigurationSchemas);
        
        for (int i=0;i<10;i++) {
            ConfigurationSchemaDto configurationSchema = createConfigurationSchema(application.getId());
            configurationSchemas.add(configurationSchema);
        }
        
        Collections.sort(configurationSchemas, new IdComparator());
        
        List<SchemaDto> storedConfigurationSchemas = toDtoList(client.getConfigurationSchemaVersionsByApplicationId(application.getId()));

        Collections.sort(storedConfigurationSchemas, new IdComparator());
        
        Assert.assertEquals(configurationSchemas.size(), storedConfigurationSchemas.size());
        for (int i=0;i<configurationSchemas.size();i++) {
            ConfigurationSchemaDto configurationSchema = configurationSchemas.get(i);
            SchemaDto storedConfigurationSchema = storedConfigurationSchemas.get(i);
            assertSchemasEquals(configurationSchema, storedConfigurationSchema);
        }
    }
    
    /**
     * Test update configuration schema.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testUpdateConfigurationSchema() throws TException, IOException {
        ConfigurationSchemaDto configurationSchema = createConfigurationSchema();
        
        configurationSchema.setName(generateString("Test Schema 2"));
        configurationSchema.setDescription(generateString("Test Desc 2"));
        
        ConfigurationSchemaDto updatedConfigurationSchema = toDto(client
                .editConfigurationSchema(toDataStruct(configurationSchema)));
        
        Assert.assertEquals(updatedConfigurationSchema.getId(), configurationSchema.getId());
        Assert.assertEquals(updatedConfigurationSchema.getApplicationId(), configurationSchema.getApplicationId());
        Assert.assertEquals(updatedConfigurationSchema.getSchema(), configurationSchema.getSchema());
        Assert.assertEquals(updatedConfigurationSchema.getName(), configurationSchema.getName());
        Assert.assertEquals(updatedConfigurationSchema.getDescription(), configurationSchema.getDescription());
        Assert.assertEquals(updatedConfigurationSchema.getCreatedTime(), configurationSchema.getCreatedTime());
        Assert.assertEquals(updatedConfigurationSchema.getProtocolSchema(), configurationSchema.getProtocolSchema());
        Assert.assertEquals(updatedConfigurationSchema.getStatus(), configurationSchema.getStatus());
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
        Assert.assertEquals(configurationSchema.getSchema(), storedConfigurationSchema.getSchema());
        Assert.assertEquals(configurationSchema.getProtocolSchema(), storedConfigurationSchema.getProtocolSchema());
        Assert.assertEquals(configurationSchema.getStatus(), storedConfigurationSchema.getStatus());
    }

}
