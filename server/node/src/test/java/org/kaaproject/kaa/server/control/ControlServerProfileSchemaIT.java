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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.admin.SchemaVersions;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

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
        EndpointProfileSchemaDto profileSchema = createProfileSchema();
        Assert.assertFalse(strIsEmpty(profileSchema.getId()));
    }

    /**
     * Test get profile schema.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileSchema() throws Exception {
        EndpointProfileSchemaDto profileSchema = createProfileSchema();

        EndpointProfileSchemaDto storedProfileSchema = client.getProfileSchema(profileSchema.getId());

        Assert.assertNotNull(storedProfileSchema);
        assertProfileSchemasEquals(profileSchema, storedProfileSchema);
    }

    /**
     * Test get profile schemas by application id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileSchemasByApplicationToken() throws Exception {

        List<EndpointProfileSchemaDto> profileSchemas  = new ArrayList<>(11);
        ApplicationDto application = createApplication(tenantAdminDto);

        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null, null, null);

        List<EndpointProfileSchemaDto> defaultProfileSchemas = client.getProfileSchemas(application.getApplicationToken());
        profileSchemas.addAll(defaultProfileSchemas);

        for (int i=0;i<10;i++) {
            EndpointProfileSchemaDto profileSchema = createEndpointProfileSchema(application.getId(), ctlSchema.getId());
            profileSchemas.add(profileSchema);
        }

        Collections.sort(profileSchemas, new IdComparator());

        List<EndpointProfileSchemaDto> storedProfileSchemas = client.getProfileSchemas(application.getApplicationToken());

        Collections.sort(storedProfileSchemas, new IdComparator());

        Assert.assertEquals(profileSchemas.size(), storedProfileSchemas.size());
        for (int i=0;i<profileSchemas.size();i++) {
            EndpointProfileSchemaDto profileSchema = profileSchemas.get(i);
            EndpointProfileSchemaDto storedProfileSchema = storedProfileSchemas.get(i);
            assertProfileSchemasEquals(profileSchema, storedProfileSchema);
        }
    }

    /**
     * Test get profile schema versions by application token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileSchemaVersionsByApplicationToken() throws Exception {

        List<EndpointProfileSchemaDto> profileSchemas  = new ArrayList<>(11);
        ApplicationDto application = createApplication(tenantAdminDto);

        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        List<EndpointProfileSchemaDto> defaultProfileSchemas = client.getProfileSchemas(application.getApplicationToken());
        profileSchemas.addAll(defaultProfileSchemas);

        CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(), null, null, null);

        for (int i=0;i<10;i++) {
            EndpointProfileSchemaDto profileSchema = createEndpointProfileSchema(application.getId(), ctlSchema.getId());
            profileSchemas.add(profileSchema);
        }

        Collections.sort(profileSchemas, new IdComparator());

        SchemaVersions schemaVersions = client.getSchemaVersionsByApplicationToken(application.getApplicationToken());

        List<VersionDto> storedProfileSchemas = schemaVersions.getProfileSchemaVersions();

        Collections.sort(storedProfileSchemas, new IdComparator());

        Assert.assertEquals(profileSchemas.size(), storedProfileSchemas.size());
        for (int i=0;i<profileSchemas.size();i++) {
            EndpointProfileSchemaDto profileSchema = profileSchemas.get(i);
            VersionDto storedProfileSchema = storedProfileSchemas.get(i);
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
        EndpointProfileSchemaDto profileSchema = createProfileSchema();

        profileSchema.setName("Test Schema 2");
        profileSchema.setDescription("Test Desc 2");

        EndpointProfileSchemaDto updatedProfileSchema = client
                .saveProfileSchema(profileSchema);

        Assert.assertEquals(profileSchema.getApplicationId(), updatedProfileSchema.getApplicationId());
        Assert.assertEquals(profileSchema.getName(), updatedProfileSchema.getName());
        Assert.assertEquals(profileSchema.getDescription(), updatedProfileSchema.getDescription());
        Assert.assertEquals(profileSchema.getCreatedTime(), updatedProfileSchema.getCreatedTime());
        Assert.assertEquals(profileSchema.getCtlSchemaId(), updatedProfileSchema.getCtlSchemaId());
    }

    /**
     * Assert profile schemas equals.
     *
     * @param profileSchema the profile schema
     * @param storedProfileSchema the stored profile schema
     */
    private void assertProfileSchemasEquals(EndpointProfileSchemaDto profileSchema, EndpointProfileSchemaDto storedProfileSchema) {
        Assert.assertEquals(profileSchema.getId(), storedProfileSchema.getId());
        Assert.assertEquals(profileSchema.getApplicationId(), storedProfileSchema.getApplicationId());
        Assert.assertEquals(profileSchema.getCtlSchemaId(), storedProfileSchema.getCtlSchemaId());
    }

}
