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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;

/**
 * The Class ControlServerProfileSchemaIT.
 */
public class ControlServerProfileSchemaIT extends AbstractTestControlServer {

    /**
     * Test create profile schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateProfileSchema() throws Exception {
        ProfileSchemaDto profileSchema = createProfileSchema();
        Assert.assertFalse(strIsEmpty(profileSchema.getId()));
    }
    
    /**
     * Test get profile schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileSchema() throws Exception {
        ProfileSchemaDto profileSchema = createProfileSchema();
        
        ProfileSchemaDto storedProfileSchema = client.getProfileSchema(profileSchema.getId());
        
        Assert.assertNotNull(storedProfileSchema);
        assertProfileSchemasEquals(profileSchema, storedProfileSchema);
    }
    
    /**
     * Test get profile schemas by application id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileSchemasByApplicationId() throws Exception {
        
        List<ProfileSchemaDto> profileSchemas  = new ArrayList<>(11);
        ApplicationDto application = createApplication(tenantAdminDto);
        
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        
        List<ProfileSchemaDto> defaultProfileSchemas = client.getProfileSchemas(application.getId());
        profileSchemas.addAll(defaultProfileSchemas);

        for (int i=0;i<10;i++) {
            ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
            profileSchemas.add(profileSchema);
        }
        
        Collections.sort(profileSchemas, new IdComparator());
        
        List<ProfileSchemaDto> storedProfileSchemas = client.getProfileSchemas(application.getId());

        Collections.sort(storedProfileSchemas, new IdComparator());
        
        Assert.assertEquals(profileSchemas.size(), storedProfileSchemas.size());
        for (int i=0;i<profileSchemas.size();i++) {
            ProfileSchemaDto profileSchema = profileSchemas.get(i);
            ProfileSchemaDto storedProfileSchema = storedProfileSchemas.get(i);
            assertProfileSchemasEquals(profileSchema, storedProfileSchema);
        }
    }
    
    /**
     * Test get profile schema versions by application id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileSchemaVersionsByApplicationId() throws Exception {
        
        List<ProfileSchemaDto> profileSchemas  = new ArrayList<>(11);
        ApplicationDto application = createApplication(tenantAdminDto);
        
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        
        List<ProfileSchemaDto> defaultProfileSchemas = client.getProfileSchemas(application.getId());
        profileSchemas.addAll(defaultProfileSchemas);

        for (int i=0;i<10;i++) {
            ProfileSchemaDto profileSchema = createProfileSchema(application.getId());
            profileSchemas.add(profileSchema);
        }
        
        Collections.sort(profileSchemas, new IdComparator());
        
        SchemaVersions schemaVersions = client.getSchemaVersionsByApplicationId(application.getId());
        
        List<SchemaDto> storedProfileSchemas = schemaVersions.getProfileSchemaVersions();

        Collections.sort(storedProfileSchemas, new IdComparator());
        
        Assert.assertEquals(profileSchemas.size(), storedProfileSchemas.size());
        for (int i=0;i<profileSchemas.size();i++) {
            ProfileSchemaDto profileSchema = profileSchemas.get(i);
            SchemaDto storedProfileSchema = storedProfileSchemas.get(i);
            assertSchemasEquals(profileSchema, storedProfileSchema);
        }
    }
    
    /**
     * Test update profile schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateProfileSchema() throws Exception {
        ProfileSchemaDto profileSchema = createProfileSchema();
        
        profileSchema.setName("Test Schema 2");
        profileSchema.setDescription("Test Desc 2");
        
        ProfileSchemaDto updatedProfileSchema = client
                .editProfileSchema(profileSchema);

        Assert.assertEquals(profileSchema.getApplicationId(), updatedProfileSchema.getApplicationId());
        Assert.assertEquals(profileSchema.getName(), updatedProfileSchema.getName());
        Assert.assertEquals(profileSchema.getDescription(), updatedProfileSchema.getDescription());
        Assert.assertEquals(profileSchema.getCreatedTime(), updatedProfileSchema.getCreatedTime());
        Assert.assertEquals(profileSchema.getSchema(), updatedProfileSchema.getSchema());
    }
    
    /**
     * Assert profile schemas equals.
     *
     * @param profileSchema the profile schema
     * @param storedProfileSchema the stored profile schema
     */
    private void assertProfileSchemasEquals(ProfileSchemaDto profileSchema, ProfileSchemaDto storedProfileSchema) {
        Assert.assertEquals(profileSchema.getId(), storedProfileSchema.getId());
        Assert.assertEquals(profileSchema.getApplicationId(), storedProfileSchema.getApplicationId());
        Assert.assertEquals(profileSchema.getSchema(), storedProfileSchema.getSchema());
    }
 
}
