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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.thrift.TException;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlServerUserIT extends AbstractTestControlServer {

    /** The Constant logger. */
    private static final Logger logger = LoggerFactory
            .getLogger(ControlServerUserIT.class);
    
    /**
     * Test create user.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testCreateUser() throws TException {
        UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
        Assert.assertFalse(strIsEmpty(user.getId()));
    }
    
    /**
     * Test get user.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetUser() throws TException {
        UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
        
        UserDto storedUser = toDto(client.getUser(user.getId()));
        
        Assert.assertNotNull(storedUser);
        
        assertUsersEquals(user, storedUser);
    }
    
    /**
     * Test get user by external Uid.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetUserByExternalUid() throws TException {
        UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
        
        UserDto storedUser = toDto(client.getUserByExternalUid(user.getExternalUid()));
        
        Assert.assertNotNull(storedUser);
        assertUsersEquals(user, storedUser);
    }
    
    /**
     * Test get users.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetUsers() throws TException {
        List<UserDto> users  = new ArrayList<UserDto>(10);
        for (int i=0;i<10;i++) {
            UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
            users.add(user);
        }
        
        Collections.sort(users, new IdComparator());
        
        List<UserDto> storedUsers = toDtoList(client.getUsers());
        Collections.sort(storedUsers, new IdComparator());
        
        Assert.assertEquals(users.size(), storedUsers.size());
        for (int i=0;i<users.size();i++) {
            UserDto user = users.get(i);
            UserDto storedUser = storedUsers.get(i);
            assertUsersEquals(user, storedUser);
        }
    }
    
    /**
     * Test get tenant users.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testGetTenantUsers() throws TException {
        TenantDto tenant = createTenant();
        List<UserDto> users  = new ArrayList<UserDto>(10);
        for (int i=0;i<10;i++) {
            UserDto user = createUser(tenant.getId(), i%2==0 ? KaaAuthorityDto.TENANT_DEVELOPER : KaaAuthorityDto.TENANT_USER);
            users.add(user);
        }
        
        Collections.sort(users, new IdComparator());
        
        List<UserDto> storedUsers = toDtoList(client.getTenantUsers(tenant.getId()));
        Collections.sort(storedUsers, new IdComparator());
        
        Assert.assertEquals(users.size(), storedUsers.size());
        for (int i=0;i<users.size();i++) {
            UserDto user = users.get(i);
            UserDto storedUser = storedUsers.get(i);
            assertUsersEquals(user, storedUser);
        }
    }
    
    /**
     * Test update user.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testUpdateUser() throws TException {
        UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
        
        user.setExternalUid(UUID.randomUUID().toString());
        
        UserDto updatedUser = toDto(client
                .editUser(toDataStruct(user)));
        
        assertUsersEquals(updatedUser, user);
    }
    
    /**
     * Test delete user.
     * 
     * @throws TException
     *             the t exception
     */
    @Test
    public void testDeleteUser() throws TException {
        UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
        client.deleteUser(user.getId());
        UserDto storedUser = toDto(client.getUser(user.getId()));
        Assert.assertNull(storedUser);
   }

    /**
     * Assert users equals.
     *
     * @param user the user
     * @param otherUser the other user
     */
    private void assertUsersEquals(UserDto user, UserDto otherUser) {
        Assert.assertEquals(user.getId(), otherUser.getId());
        Assert.assertEquals(user.getUsername(), otherUser.getUsername());
        Assert.assertEquals(user.getExternalUid(), otherUser.getExternalUid());
        Assert.assertEquals(user.getTenantId(), otherUser.getTenantId());
        Assert.assertEquals(user.getAuthority(), otherUser.getAuthority());
    }

}
