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

/**
 * The Class ControlServerEndpointGroupIT.
 */
public class ControlServerEndpointGroupIT extends AbstractTestControlServer {

    /**
     * Test create endpoint group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateEndpointGroup() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();
        Assert.assertFalse(strIsEmpty(endpointGroup.getId()));
    }
    
    /**
     * Test get endpoint group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEndpointGroup() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();
        
        EndpointGroupDto storedEndpointGroup = client.getEndpointGroup(endpointGroup.getId());
        
        Assert.assertNotNull(storedEndpointGroup);
        assertEndpointGroupsEquals(endpointGroup, storedEndpointGroup);
    }

    /**
     * Test get endpoint groups by application token.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetEndpointGroupsByApplicationToken() throws Exception {

        List<EndpointGroupDto> endpointGroups  = new ArrayList<>(11);

        ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        List<EndpointGroupDto> defaultEndpointGroups = client.getEndpointGroupsByAppToken(application.getApplicationToken());
        endpointGroups.addAll(defaultEndpointGroups);

        for (int i = 0; i < 10; i++) {
            EndpointGroupDto endpointGroup = createEndpointGroup(application.getId());
            endpointGroups.add(endpointGroup);
        }

        Collections.sort(endpointGroups, new IdComparator());

        List<EndpointGroupDto> storedEndpointGroups = client.getEndpointGroupsByAppToken(application.getApplicationToken());

        Collections.sort(storedEndpointGroups, new IdComparator());

        Assert.assertEquals(endpointGroups.size(), storedEndpointGroups.size());
        for (int i=0;i<endpointGroups.size();i++) {
            EndpointGroupDto endpointGroup = endpointGroups.get(i);
            EndpointGroupDto storedEndpointGroup = storedEndpointGroups.get(i);
            assertEndpointGroupsEquals(endpointGroup, storedEndpointGroup);
        }
    }

    /**
     * Test delete endpoint group.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteEndpointGroup() throws Exception {
        final EndpointGroupDto endpointGroup = createEndpointGroup();
        client.deleteEndpointGroup(endpointGroup.getId());
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getEndpointGroup(endpointGroup.getId());
            }
        });
   }

    /**
     * Assert endpoint groups equals.
     *
     * @param endpointGroup the endpoint group
     * @param storedEndpointGroup the stored endpoint group
     */
    private void assertEndpointGroupsEquals(EndpointGroupDto endpointGroup, EndpointGroupDto storedEndpointGroup) {
        Assert.assertEquals(endpointGroup.getId(), storedEndpointGroup.getId());
        Assert.assertEquals(endpointGroup.getApplicationId(), storedEndpointGroup.getApplicationId());
        Assert.assertEquals(endpointGroup.getName(), storedEndpointGroup.getName());
        Assert.assertEquals(endpointGroup.getWeight(), storedEndpointGroup.getWeight());
    }

}
