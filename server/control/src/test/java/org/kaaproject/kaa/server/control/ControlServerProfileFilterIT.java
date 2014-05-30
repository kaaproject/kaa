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
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileSchemaDto;
import org.kaaproject.kaa.common.dto.SchemaDto;
import org.kaaproject.kaa.common.dto.StructureRecordDto;
import org.kaaproject.kaa.common.dto.UpdateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerProfileFilterIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerProfileFilterIT.class);
    
    /**
     * Test create profile filter.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateProfileFilter() throws TException, IOException {
        ProfileFilterDto profileFilter = createProfileFilter();
        Assert.assertFalse(strIsEmpty(profileFilter.getId()));
    }
    
    /**
     * Test get profile filter.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetProfileFilter() throws TException, IOException {
        ProfileFilterDto profileFilter = createProfileFilter();
        
        ProfileFilterDto storedProfileFilter = toDto(client.getProfileFilter(profileFilter.getId()));
        
        Assert.assertNotNull(storedProfileFilter);
        assertProfileFiltersEquals(profileFilter, storedProfileFilter);
    }
    
    /**
     * Test get profile filter record.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetProfileFilterRecord() throws TException, IOException {
        ProfileFilterDto profileFilter = createProfileFilter();
        
        StructureRecordDto<ProfileFilterDto> profileFilterRecord = toGenericDto(client.getProfileFilterRecord(profileFilter.getSchemaId(), profileFilter.getEndpointGroupId()));
        
        Assert.assertNotNull(profileFilterRecord);
        Assert.assertNotNull(profileFilterRecord.getInactiveStructureDto());
        assertProfileFiltersEquals(profileFilter, profileFilterRecord.getInactiveStructureDto());
    }
    
    /**
     * Test get profile filter records by endpoint group id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetProfileFilterRecordsByEndpointGroupId() throws TException, IOException {
        
        ApplicationDto application = createApplication();
        EndpointGroupDto endpointGroup = createEndpointGroup(application.getId());
        
        ProfileFilterDto profileFilter1 = createProfileFilter(null, endpointGroup.getId(), application.getId());
        ProfileFilterDto profileFilter2 = createProfileFilter(null, endpointGroup.getId(), application.getId());
        
        List<StructureRecordDto<ProfileFilterDto>> profileFilterRecords = toGenericDtoList(client.getProfileFilterRecordsByEndpointGroupId(endpointGroup.getId(), false));
        
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(2, profileFilterRecords.size());
        assertProfileFiltersEquals(profileFilter1, profileFilterRecords.get(0).getInactiveStructureDto());
        assertProfileFiltersEquals(profileFilter2, profileFilterRecords.get(1).getInactiveStructureDto());
    }
    
    /**
     * Test delete profile filter record
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDeleteProfileFilterRecord() throws TException, IOException {
        EndpointGroupDto endpointGroup = createEndpointGroup();
        
        ProfileFilterDto profileFilter1 = createProfileFilter(null, endpointGroup.getId());
        ProfileFilterDto profileFilter2 = createProfileFilter(null, endpointGroup.getId());
        
        client.activateProfileFilter(profileFilter2.getId(), null);
        
        client.deleteProfileFilterRecord(profileFilter2.getSchemaId(), endpointGroup.getId(), null);
        
        List<StructureRecordDto<ProfileFilterDto>> profileFilterRecords = toGenericDtoList(client.getProfileFilterRecordsByEndpointGroupId(endpointGroup.getId(), false));
        
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(1, profileFilterRecords.size());
        assertProfileFiltersEquals(profileFilter1, profileFilterRecords.get(0).getInactiveStructureDto());
        
        client.deleteProfileFilterRecord(profileFilter1.getSchemaId(), endpointGroup.getId(), null);
        profileFilterRecords = toGenericDtoList(client.getProfileFilterRecordsByEndpointGroupId(endpointGroup.getId(), false));
        Assert.assertNotNull(profileFilterRecords);
        Assert.assertEquals(0, profileFilterRecords.size());
    }
    
    /**
     * Test get vacant schemas by endpoint group id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetVacantSchemasByEndpointGroupId() throws TException, IOException {
        
        EndpointGroupDto endpointGroup = createEndpointGroup();
        ProfileSchemaDto profileSchema1 = createProfileSchema(endpointGroup.getApplicationId());
        ProfileSchemaDto profileSchema2 = createProfileSchema(endpointGroup.getApplicationId());
        ProfileSchemaDto profileSchema3 = createProfileSchema(endpointGroup.getApplicationId());
        
        createProfileFilter(profileSchema1.getId(), endpointGroup.getId());
        createProfileFilter(profileSchema2.getId(), endpointGroup.getId());
        
        List<SchemaDto> schemas = toDtoList(client.getVacantProfileSchemasByEndpointGroupId(endpointGroup.getId()));
        
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
        Assert.assertEquals(profileSchema3.getId(), schema.getId());
        Assert.assertEquals(profileSchema3.getMajorVersion(), schema.getMajorVersion());
        Assert.assertEquals(profileSchema3.getMinorVersion(), schema.getMinorVersion());
    }
    
    /**
     * Test update profile filter.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testUpdateProfileFilter() throws TException, IOException {
        ProfileFilterDto profileFilter = createProfileFilter();
        
        String filterUpdated = getResourceAsString(TEST_PROFILE_FILTER_UPDATED);
        
        profileFilter.setBody(filterUpdated);
        
        ProfileFilterDto updatedProfileFilter = toDto(client
                .editProfileFilter(toDataStruct(profileFilter)));
        
        assertProfileFiltersEquals(updatedProfileFilter, profileFilter);
    }
    
    /**
     * Test activate profile filter.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testActivateProfileFilter() throws TException, IOException {
        ProfileFilterDto profileFilter = createProfileFilter();
        ProfileFilterDto activatedProfileFilter = toDto(client.activateProfileFilter(profileFilter.getId(), null));
        
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
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testDeactivateProfileFilter() throws TException, IOException {
        ProfileFilterDto profileFilter = createProfileFilter();
        client.activateProfileFilter(profileFilter.getId(), null);
        ProfileFilterDto deactivatedProfileFilter = toDto(client.deactivateProfileFilter(profileFilter.getId(), null));
        
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
