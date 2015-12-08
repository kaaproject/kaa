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

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.VersionDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaInfoDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaScopeDto;

/**
 * The Class ControlServerProfileFilterIT.
 */
public class ControlServerProfileFilterIT extends AbstractTestControlServer {

    /**
     * Test create profile filter.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        Assert.assertFalse(strIsEmpty(profileFilter.getId()));
    }
    
    /**
     * Test get profile filter record.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileFilterRecord() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        
        ProfileFilterRecordDto profileFilterRecord = client.getProfileFilterRecord(profileFilter.getSchemaId(), profileFilter.getEndpointGroupId());
        
        Assert.assertNotNull(profileFilterRecord);
        Assert.assertNotNull(profileFilterRecord.getInactiveProfileFilter());
        assertProfileFiltersEquals(profileFilter, profileFilterRecord.getInactiveProfileFilter());
    }
    
    /**
     * Test get profile filter records by endpoint group id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetProfileFilterRecordsByEndpointGroupId() throws Exception {
        
        ApplicationDto application = createApplication(tenantAdminDto);
        
        loginTenantDeveloper(tenantDeveloperDto.getUsername());
        
        EndpointGroupDto endpointGroup = createEndpointGroup(application.getId());
        
        ProfileFilterDto profileFilter1 = createProfileFilter(null, endpointGroup.getId(), application.getId());
        ProfileFilterDto profileFilter2 = createProfileFilter(null, endpointGroup.getId(), application.getId());
        
        List<ProfileFilterRecordDto> profileFilterRecords = client.getProfileFilterRecords(endpointGroup.getId(), false);
        
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(2, profileFilterRecords.size());
        assertProfileFiltersEquals(profileFilter1, profileFilterRecords.get(0).getInactiveProfileFilter());
        assertProfileFiltersEquals(profileFilter2, profileFilterRecords.get(1).getInactiveProfileFilter());
    }
    
    /**
     * Test delete profile filter record.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteProfileFilterRecord() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();
        
        ProfileFilterDto profileFilter1 = createProfileFilter(null, endpointGroup.getId());
        ProfileFilterDto profileFilter2 = createProfileFilter(null, endpointGroup.getId());
        
        client.activateProfileFilter(profileFilter2.getId());
        
        client.deleteProfileFilterRecord(profileFilter2.getSchemaId(), endpointGroup.getId());
        
        List<ProfileFilterRecordDto> profileFilterRecords = client.getProfileFilterRecords(endpointGroup.getId(), false);
        
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(1, profileFilterRecords.size());
        assertProfileFiltersEquals(profileFilter1, profileFilterRecords.get(0).getInactiveProfileFilter());
        
        client.deleteProfileFilterRecord(profileFilter1.getSchemaId(), endpointGroup.getId());
        profileFilterRecords = client.getProfileFilterRecords(endpointGroup.getId(), false);
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(0, profileFilterRecords.size());
    }
    
    /**
     * Test get vacant schemas by endpoint group id.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetVacantSchemasByEndpointGroupId() throws Exception {
        
        EndpointGroupDto endpointGroup = createEndpointGroup();
        
        CTLSchemaInfoDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, CTLSchemaScopeDto.TENANT, null, null, null);
        
        ProfileSchemaDto profileSchema1 = createProfileSchema(endpointGroup.getApplicationId(), ctlSchema.getId());
        ProfileSchemaDto profileSchema2 = createProfileSchema(endpointGroup.getApplicationId(), ctlSchema.getId());
        ProfileSchemaDto profileSchema3 = createProfileSchema(endpointGroup.getApplicationId(), ctlSchema.getId());
        
        createProfileFilter(profileSchema1.getId(), endpointGroup.getId());
        createProfileFilter(profileSchema2.getId(), endpointGroup.getId());
        
        List<VersionDto> schemas = client.getVacantProfileSchemasByEndpointGroupId(endpointGroup.getId());
        
        Assert.assertNotNull(schemas);
        Assert.assertEquals(2, schemas.size());
        Collections.sort(schemas);
        VersionDto schema = schemas.get(1);
        Assert.assertNotNull(schema);
        Assert.assertEquals(profileSchema3.getId(), schema.getId());
        Assert.assertEquals(profileSchema3.getVersion(), schema.getVersion());
    }
    
    /**
     * Test update profile filter.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpdateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        
        String filterUpdated = getResourceAsString(TEST_PROFILE_FILTER_UPDATED);
        
        profileFilter.setBody(filterUpdated);
        
        ProfileFilterDto updatedProfileFilter = client
                .editProfileFilter(profileFilter);
        
        assertProfileFiltersEquals(updatedProfileFilter, profileFilter);
    }
    
    /**
     * Test activate profile filter.
     *
     * @throws Exception the exception
     */
    @Test
    public void testActivateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        ProfileFilterDto activatedProfileFilter = client.activateProfileFilter(profileFilter.getId());
        
        Assert.assertEquals(profileFilter.getId(), activatedProfileFilter.getId());
        Assert.assertEquals(profileFilter.getSchemaId(), activatedProfileFilter.getSchemaId());
        Assert.assertEquals(profileFilter.getEndpointGroupId(), activatedProfileFilter.getEndpointGroupId());
        Assert.assertEquals(profileFilter.getBody(), activatedProfileFilter.getBody());
        Assert.assertEquals(profileFilter.getApplicationId(), activatedProfileFilter.getApplicationId());
        Assert.assertEquals(UpdateStatus.ACTIVE, activatedProfileFilter.getStatus());
    }
    
    /**
     * Test deactivate profile filter.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeactivateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        client.activateProfileFilter(profileFilter.getId());
        ProfileFilterDto deactivatedProfileFilter = client.deactivateProfileFilter(profileFilter.getId());
        
        Assert.assertEquals(profileFilter.getId(), deactivatedProfileFilter.getId());
        Assert.assertEquals(profileFilter.getSchemaId(), deactivatedProfileFilter.getSchemaId());
        Assert.assertEquals(profileFilter.getEndpointGroupId(), deactivatedProfileFilter.getEndpointGroupId());
        Assert.assertEquals(profileFilter.getBody(), deactivatedProfileFilter.getBody());
        Assert.assertEquals(profileFilter.getApplicationId(), deactivatedProfileFilter.getApplicationId());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivatedProfileFilter.getStatus());
    }
    
    /**
     * Assert profile filters equals.
     *
     * @param profileFilter the profile filter
     * @param storedProfileFilter the stored profile filter
     */
    private void assertProfileFiltersEquals(ProfileFilterDto profileFilter, ProfileFilterDto storedProfileFilter) {
        Assert.assertEquals(profileFilter.getId(), storedProfileFilter.getId());
        Assert.assertEquals(profileFilter.getSchemaId(), storedProfileFilter.getSchemaId());
        Assert.assertEquals(profileFilter.getEndpointGroupId(), storedProfileFilter.getEndpointGroupId());
        Assert.assertEquals(profileFilter.getBody(), storedProfileFilter.getBody());
    }

}
