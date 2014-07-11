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
import static org.kaaproject.kaa.server.control.AbstractTestControlServer.strIsEmpty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.TenantDto;

public class ControlServerEndpointUserIT extends AbstractTestControlServer {

    /**
     * Test create endpoint user.
     *
     * @throws TException the t exception
     */
    @Test
    public void testCreateEndpointUser() throws TException {
        TenantDto tenant = createTenant();
        EndpointUserDto endpointUser = createEndpointUser("", tenant.getId());
        Assert.assertFalse(strIsEmpty(endpointUser.getId()));
        client.deleteEndpointUser(endpointUser.getId());
        client.deleteTenant(tenant.getId());
    }

    /**
     * Test get endpoint user.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetEndpointUser() throws TException {
        TenantDto tenant = createTenant();
        EndpointUserDto endpointUser = createEndpointUser("", tenant.getId());

        EndpointUserDto storedEndpointUser = toDto(client.getEndpointUser(endpointUser.getId()));

        Assert.assertNotNull(storedEndpointUser);
        Assert.assertEquals(endpointUser, storedEndpointUser);
        client.deleteEndpointUser(endpointUser.getId());
    }

    /**
     * Test get endpoint users.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGetEndpointUsers() throws TException {
        TenantDto tenant = createTenant();

        List<EndpointUserDto> endpointUsers = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            EndpointUserDto endpointUser = createEndpointUser(((Integer) i).toString(), tenant.getId());
            endpointUsers.add(endpointUser);
        }

        Collections.sort(endpointUsers, new IdComparator());

        List<EndpointUserDto> storedEndpointUsers = toDtoList(client.getEndpointUsers());
        Collections.sort(storedEndpointUsers, new IdComparator());

        Assert.assertEquals(endpointUsers.size(), storedEndpointUsers.size());
        for (int i = 0; i < endpointUsers.size(); i++) {
            EndpointUserDto endpointUser = endpointUsers.get(i);
            EndpointUserDto storedEndpointUser = storedEndpointUsers.get(i);
            Assert.assertEquals(endpointUser, storedEndpointUser);
        }
        for (EndpointUserDto endpointUser : storedEndpointUsers) {
            client.deleteEndpointUser(endpointUser.getId());
        }
        client.deleteTenant(tenant.getId());
    }

    /**
     * Test update endpoint user.
     *
     * @throws TException the t exception
     */
    @Test
    public void testUpdateEndpointUser() throws TException {
        TenantDto tenant = createTenant();
        EndpointUserDto endpointUser = createEndpointUser("", tenant.getId());

        endpointUser.setUsername(generateString(ENDPOINT_USER_NAME));

        EndpointUserDto updatedEndpointUser = toDto(client
                .editEndpointUser(toDataStruct(endpointUser)));

        Assert.assertEquals(updatedEndpointUser, endpointUser);
        client.deleteEndpointUser(endpointUser.getId());
        client.deleteTenant(tenant.getId());
    }

    /**
     * Test delete endpoint user.
     *
     * @throws TException the t exception
     */
    @Test
    public void testDeleteEndpointUser() throws TException {
        TenantDto tenant = createTenant();
        EndpointUserDto endpointUser = createEndpointUser("", tenant.getId());

        client.deleteEndpointUser(endpointUser.getId());
        EndpointUserDto storedEndpointUser = toDto(client.getEndpointUser(endpointUser.getId()));
        Assert.assertNull(storedEndpointUser);
        client.deleteTenant(tenant.getId());
    }

    /**
     * Test generate endpoint user access token.
     *
     * @throws TException the t exception
     */
    @Test
    public void testGenerateEndpointUserAccessToken() throws TException {
        TenantDto tenant = createTenant();
        EndpointUserDto savedEndpointUser = createEndpointUser("", tenant.getId());

        Assert.assertNull(savedEndpointUser.getAccessToken());

        String generatedAccessToken = client.generateEndpointUserAccessToken(savedEndpointUser.getExternalId(), savedEndpointUser.getTenantId());
        EndpointUserDto endpointUser = toDto(client.getEndpointUser(savedEndpointUser.getId()));

        Assert.assertNotNull(generatedAccessToken);
        Assert.assertEquals(generatedAccessToken, endpointUser.getAccessToken());

        client.deleteEndpointUser(endpointUser.getId());
        client.deleteTenant(tenant.getId());
    }
}
