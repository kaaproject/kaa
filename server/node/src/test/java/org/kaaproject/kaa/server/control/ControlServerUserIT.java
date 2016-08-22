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
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;

/**
 * The Class ControlServerUserIT.
 */
public class ControlServerUserIT extends AbstractTestControlServer {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantDeveloperNeeded()
     */
    @Override
    protected boolean createTenantDeveloperNeeded() {
        return false;
    }
    
    /**
     * Test create user.
     *
     * @throws Exception the exception
     */
    @Test
    public void testCreateUser() throws Exception {
        UserDto user = createUser(KaaAuthorityDto.TENANT_DEVELOPER);
        Assert.assertFalse(strIsEmpty(user.getId()));
    }
    
    /**
     * Test get user.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetUser() throws Exception {
        UserDto user = createUser(KaaAuthorityDto.TENANT_DEVELOPER);
        
        UserDto storedUser = client.getUser(user.getId());
        
        Assert.assertNotNull(storedUser);
        
        assertUsersEquals(user, storedUser);
    }
    
    /**
     * Test get users.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetUsers() throws Exception {
        loginTenantAdmin(tenantAdminDto.getUsername());
        List<UserDto> users  = new ArrayList<UserDto>(10);
        for (int i=0;i<10;i++) {
            UserDto user = createUser(tenantAdminDto, i%2==0 ? KaaAuthorityDto.TENANT_DEVELOPER : KaaAuthorityDto.TENANT_USER);
            users.add(user);
        }
        
        Collections.sort(users, new IdComparator());
        
        List<UserDto> storedUsers = client.getUsers();
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
     * @throws Exception the exception
     */
    @Test
    public void testUpdateUser() throws Exception {
        UserDto user = createUser(KaaAuthorityDto.TENANT_DEVELOPER);

        user.setFirstName(generateString("NewFirst"));
        user.setLastName(generateString("NewLast"));

        UserDto updatedUser = client.editUser(user);
        assertUsersEquals(updatedUser, user);
    }
    
    /**
     * Test delete user.
     *
     * @throws Exception the exception
     */
    @Test
    public void testDeleteUser() throws Exception {
        final UserDto user = createUser(KaaAuthorityDto.TENANT_DEVELOPER);
        client.deleteUser(user.getId());
        
        checkNotFound(new TestRestCall() {
            @Override
            public void executeRestCall() throws Exception {
                client.getUser(user.getId());
            }
        });
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
        Assert.assertEquals(user.getMail(), otherUser.getMail());
        Assert.assertEquals(user.getFirstName(), otherUser.getFirstName());
        Assert.assertEquals(user.getLastName(), otherUser.getLastName());
        Assert.assertEquals(user.getExternalUid(), otherUser.getExternalUid());
        Assert.assertEquals(user.getTenantId(), otherUser.getTenantId());
        Assert.assertEquals(user.getAuthority(), otherUser.getAuthority());
    }

}
