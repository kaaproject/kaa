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
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;

/**
 * The Class ControlServerProfileFilterIT.
 */
public class ControlServerProfileFilterIT extends AbstractTestControlServer {

    /**
     * Test create profile filter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testCreateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        Assert.assertFalse(strIsEmpty(profileFilter.getId()));
    }

    /**
     * Test get profile filter record.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetProfileFilterRecord() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();

        ProfileFilterRecordDto profileFilterRecord = client.getProfileFilterRecord(profileFilter.getEndpointProfileSchemaId(),
                profileFilter.getServerProfileSchemaId(), profileFilter.getEndpointGroupId());

        Assert.assertNotNull(profileFilterRecord);
        Assert.assertNotNull(profileFilterRecord.getInactiveStructureDto());
        assertProfileFiltersEquals(profileFilter, profileFilterRecord.getInactiveStructureDto());
    }

    /**
     * Test get profile filter records by endpoint group id.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetProfileFilterRecordsByEndpointGroupId() throws Exception {

        ApplicationDto application = createApplication(tenantAdminDto);

        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        EndpointGroupDto endpointGroup = createEndpointGroup(application.getId());
        
        List<ProfileFilterDto> profileFilters  = new ArrayList<ProfileFilterDto>(2);

        profileFilters.add(createProfileFilter(null, null, endpointGroup.getId()));
        profileFilters.add(createProfileFilter(null, null, endpointGroup.getId()));
        
        Collections.sort(profileFilters, new IdComparator());

        List<ProfileFilterRecordDto> profileFilterRecords = client.getProfileFilterRecords(endpointGroup.getId(), false);

        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(2, profileFilterRecords.size());
        
        List<ProfileFilterDto> storedProfileFilters  = new ArrayList<ProfileFilterDto>(2);
        for (ProfileFilterRecordDto profileFilterRecord : profileFilterRecords) {
            storedProfileFilters.add(profileFilterRecord.getInactiveStructureDto());
        }
        
        Collections.sort(storedProfileFilters, new IdComparator());
        
        for (int i=0;i<profileFilters.size();i++) {
            ProfileFilterDto profileFilter = profileFilters.get(i);
            ProfileFilterDto storedProfileFilter = storedProfileFilters.get(i);
            assertProfileFiltersEquals(profileFilter, storedProfileFilter);
        }
    }

    /**
     * Test delete profile filter record.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeleteProfileFilterRecord() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();

        ProfileFilterDto profileFilter1 = createProfileFilter(null, null, endpointGroup.getId());
        ProfileFilterDto profileFilter2 = createProfileFilter(null, null, endpointGroup.getId());

        client.activateProfileFilter(profileFilter2.getId());

        client.deleteProfileFilterRecord(profileFilter2.getEndpointProfileSchemaId(), profileFilter2.getServerProfileSchemaId(), endpointGroup.getId());

        List<ProfileFilterRecordDto> profileFilterRecords = client.getProfileFilterRecords(endpointGroup.getId(), false);

        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(1, profileFilterRecords.size());
        assertProfileFiltersEquals(profileFilter1, profileFilterRecords.get(0).getInactiveStructureDto());

        client.deleteProfileFilterRecord(profileFilter1.getEndpointProfileSchemaId(), profileFilter1.getServerProfileSchemaId(), endpointGroup.getId());
        profileFilterRecords = client.getProfileFilterRecords(endpointGroup.getId(), false);
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(0, profileFilterRecords.size());
    }

    /**
     * Test get vacant schemas by endpoint group id.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testGetVacantSchemasByEndpointGroupId() throws Exception {

        EndpointGroupDto endpointGroup = createEndpointGroup();

        CTLSchemaDto ctlSchema = this.createCTLSchema(this.ctlRandomFieldType(), CTL_DEFAULT_NAMESPACE, 1, tenantDeveloperDto.getTenantId(),
                null, null, null);

        EndpointProfileSchemaDto profileSchema1 = createEndpointProfileSchema(endpointGroup.getApplicationId(), ctlSchema.getId());
        EndpointProfileSchemaDto profileSchema2 = createEndpointProfileSchema(endpointGroup.getApplicationId(), ctlSchema.getId());
        EndpointProfileSchemaDto profileSchema3 = createEndpointProfileSchema(endpointGroup.getApplicationId(), ctlSchema.getId());

        createProfileFilter(profileSchema1.getId(), null, endpointGroup.getId());
        createProfileFilter(profileSchema2.getId(), null, endpointGroup.getId());

        List<ProfileVersionPairDto> schemas = client.getVacantProfileSchemasByEndpointGroupId(endpointGroup.getId());

        Assert.assertNotNull(schemas);
        Assert.assertEquals(9, schemas.size());
    }

    /**
     * Test update profile filter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testUpdateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();

        String filterUpdated = getResourceAsString(TEST_PROFILE_FILTER_UPDATED);

        profileFilter.setBody(filterUpdated);

        ProfileFilterDto updatedProfileFilter = client.editProfileFilter(profileFilter);

        assertProfileFiltersEquals(updatedProfileFilter, profileFilter);
    }

    /**
     * Test activate profile filter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testActivateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        ProfileFilterDto activatedProfileFilter = client.activateProfileFilter(profileFilter.getId());

        Assert.assertEquals(profileFilter.getId(), activatedProfileFilter.getId());
        Assert.assertEquals(profileFilter.getEndpointProfileSchemaId(), activatedProfileFilter.getEndpointProfileSchemaId());
        Assert.assertEquals(profileFilter.getServerProfileSchemaId(), activatedProfileFilter.getServerProfileSchemaId());
        Assert.assertEquals(profileFilter.getEndpointGroupId(), activatedProfileFilter.getEndpointGroupId());
        Assert.assertEquals(profileFilter.getBody(), activatedProfileFilter.getBody());
        Assert.assertEquals(profileFilter.getApplicationId(), activatedProfileFilter.getApplicationId());
        Assert.assertEquals(UpdateStatus.ACTIVE, activatedProfileFilter.getStatus());
    }

    /**
     * Test deactivate profile filter.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void testDeactivateProfileFilter() throws Exception {
        ProfileFilterDto profileFilter = createProfileFilter();
        client.activateProfileFilter(profileFilter.getId());
        ProfileFilterDto deactivatedProfileFilter = client.deactivateProfileFilter(profileFilter.getId());

        Assert.assertEquals(profileFilter.getId(), deactivatedProfileFilter.getId());
        Assert.assertEquals(profileFilter.getEndpointProfileSchemaId(), deactivatedProfileFilter.getEndpointProfileSchemaId());
        Assert.assertEquals(profileFilter.getServerProfileSchemaId(), deactivatedProfileFilter.getServerProfileSchemaId());
        Assert.assertEquals(profileFilter.getEndpointGroupId(), deactivatedProfileFilter.getEndpointGroupId());
        Assert.assertEquals(profileFilter.getBody(), deactivatedProfileFilter.getBody());
        Assert.assertEquals(profileFilter.getApplicationId(), deactivatedProfileFilter.getApplicationId());
        Assert.assertEquals(UpdateStatus.DEPRECATED, deactivatedProfileFilter.getStatus());
    }

    /**
     * Assert profile filters equals.
     *
     * @param profileFilter
     *            the profile filter
     * @param storedProfileFilter
     *            the stored profile filter
     */
    private void assertProfileFiltersEquals(ProfileFilterDto profileFilter, ProfileFilterDto storedProfileFilter) {
        Assert.assertEquals(profileFilter.getId(), storedProfileFilter.getId());
        Assert.assertEquals(profileFilter.getEndpointProfileSchemaId(), storedProfileFilter.getEndpointProfileSchemaId());
        Assert.assertEquals(profileFilter.getServerProfileSchemaId(), storedProfileFilter.getServerProfileSchemaId());
        Assert.assertEquals(profileFilter.getEndpointGroupId(), storedProfileFilter.getEndpointGroupId());
        Assert.assertEquals(profileFilter.getBody(), storedProfileFilter.getBody());
    }

}
