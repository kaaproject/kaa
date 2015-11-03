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
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerEndpointGroupIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerEndpointGroupIT.class);
    
    /**
     * Test create endpoint group.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testCreateEndpointGroup() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();
        Assert.assertFalse(strIsEmpty(endpointGroup.getId()));
    }
    
    /**
     * Test get endpoint group.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetEndpointGroup() throws Exception {
        EndpointGroupDto endpointGroup = createEndpointGroup();
        
        EndpointGroupDto storedEndpointGroup = client.getEndpointGroup(endpointGroup.getId());
        
        Assert.assertNotNull(storedEndpointGroup);
        assertEndpointGroupsEquals(endpointGroup, storedEndpointGroup);
    }
    
    /**
     * Test get endpoint groups by application id.
     * 
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void testGetEndpointGroupsByApplicationId() throws Exception {
        
        List<EndpointGroupDto> endpointGroups  = new ArrayList<>(11);

        ApplicationDto application = createApplication(tenantAdminDto);
        loginTenantDeveloper(tenantDeveloperDto.getUsername());

        List<EndpointGroupDto> defaultEndpointGroups = client.getEndpointGroups(application.getId());
        endpointGroups.addAll(defaultEndpointGroups);

        for (int i = 0; i < 10; i++) {
            EndpointGroupDto endpointGroup = createEndpointGroup(application.getId());
            endpointGroups.add(endpointGroup);
        }
        
        Collections.sort(endpointGroups, new IdComparator());
        
        List<EndpointGroupDto> storedEndpointGroups = client.getEndpointGroups(application.getId());

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
     * @throws TException
     *             the t exception
     * @throws IOException Signals that an I/O exception has occurred.
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
