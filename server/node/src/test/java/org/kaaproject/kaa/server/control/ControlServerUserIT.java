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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.admin.UserDto;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class ControlServerUserIT.
 */
public class ControlServerUserIT extends AbstractTestControlServer {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  /* (non-Javadoc)
   * @see org.kaaproject.kaa.server.control.AbstractTestControlServer#createTenantDeveloperNeeded()
   */
  @Override
  protected boolean createTenantDeveloperNeeded() {
    return false;
  }

  /**
   * Test create user.
   * Kaa admin creates tenant admin user.
   *
   * @throws Exception the exception
   */
  @Test
  public void testKaaAdminCreateTenantAdmin() throws Exception {
    loginKaaAdmin();
    UserDto user = new UserDto();
    String username = generateString(USERNAME);
    user.setUsername(username);
    user.setMail(username + "@demoproject.org");
    user.setFirstName(generateString("Test"));
    user.setLastName(generateString("User"));
    user.setAuthority(KaaAuthorityDto.TENANT_ADMIN);
    user.setTenantId(tenantAdminDto.getTenantId());
    user = client.editUser(user);

    UserDto storedUser = client.getUser(user.getId());
    Assert.assertNotNull(storedUser);
    Assert.assertEquals(storedUser.getAuthority(), KaaAuthorityDto.TENANT_ADMIN);
    assertUsersEquals(user, storedUser);
  }

  /**
   * Test create user.
   * Kaa admin is not able to create users except of tenant admin.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFailKaaAdminCreateOtherUser() throws Exception {
    expectedException.expect(HttpClientErrorException.class);
    expectedException.expectMessage("You do not have permission to perform this operation!");

    loginKaaAdmin();
    UserDto user = new UserDto();
    String username = generateString(USERNAME);
    user.setUsername(username);
    user.setMail(username + "@demoproject.org");
    user.setFirstName(generateString("Test"));
    user.setLastName(generateString("User"));
    user.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);
    user.setTenantId(tenantAdminDto.getTenantId());
    client.editUser(user);
  }

  /**
   * Test create user.
   * Tenant admin create other users (neither kaa admin nor tenant admin).
   * Tenant id should reflect.
   *
   * @throws Exception the exception
   */
  @Test
  public void testTenantAdminCreateOtherUser() throws Exception {
    loginTenantAdmin(tenantAdminDto.getUsername());
    UserDto user = createUser(KaaAuthorityDto.TENANT_DEVELOPER);

    UserDto storedUser = client.getUser(user.getId());
    Assert.assertEquals(storedUser.getAuthority(), KaaAuthorityDto.TENANT_DEVELOPER);
    Assert.assertNotNull(storedUser);
    assertUsersEquals(user, storedUser);
    Assert.assertEquals(tenantAdminDto.getTenantId(), storedUser.getTenantId());
  }

  /**
   * Test create user.
   * Tenant admin is not able to create other users, which tenant id are not reflect.
   *
   * @throws Exception the exception
   */
  @Ignore("Tenant id got from current user, so no way to set wrong tenant id")
  @Test
  public void testFailTenantAdminCreateOtherUser() throws Exception {
    expectedException.expect(HttpClientErrorException.class);
    expectedException.expectMessage("403 Forbidden");

    loginTenantAdmin(tenantAdminDto.getUsername());
    UserDto user = new UserDto();
    String username = generateString(USERNAME);
    user.setUsername(username);
    user.setMail(username + "@demoproject.org");
    user.setFirstName(generateString("Test"));
    user.setLastName(generateString("User"));
    user.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);
    user.setTenantId(tenantAdminDto.getTenantId() + 1);
    client.editUser(user);
  }

  /**
   * Test create user.
   * Tenant admin is not able to create another tenant admin.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFailTenantAdminCreateTenantAdmin() throws Exception {
    expectedException.expect(HttpClientErrorException.class);
    expectedException.expectMessage("You do not have permission to perform this operation!");

    loginTenantAdmin(tenantAdminDto.getUsername());
    UserDto user = createUser(KaaAuthorityDto.TENANT_ADMIN);
  }

  @Test
  public void testFailCreateUserOnEmailValidation() throws Exception {
    expectedException.expect(HttpClientErrorException.class);
    expectedException.expectMessage("400 Bad Request");

    loginTenantAdmin(tenantAdminDto.getUsername());
    UserDto user = new UserDto();
    String username = generateString(USERNAME);
    user.setUsername(username);
    user.setMail("invalid email!");
    user.setFirstName(generateString("firstName"));
    user.setLastName(generateString("lastName"));
    user.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);
    client.editUser(user);
  }

  /**
   * Test create user.
   * Users unable to have the same email addresses.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFailCreateUserOnDuplicatedEmail() throws Exception {
    expectedException.expect(HttpClientErrorException.class);
    expectedException.expectMessage("Entered email is already used by another user!");

    loginTenantAdmin(tenantAdminDto.getUsername());

    UserDto user = new UserDto();
    String username = generateString(USERNAME);
    user.setUsername(username);
    String email = username + "@demoproject.org";
    user.setMail(email);
    user.setFirstName(generateString("Test"));
    user.setLastName(generateString("User"));
    user.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);

    UserDto userWithSameEmail = new UserDto();
    String username2 = generateString(USERNAME);
    userWithSameEmail.setUsername(username2);
    userWithSameEmail.setMail(email);
    userWithSameEmail.setFirstName(generateString("Test"));
    userWithSameEmail.setLastName(generateString("User"));
    userWithSameEmail.setAuthority(KaaAuthorityDto.TENANT_DEVELOPER);

        client.editUser(user);
        client.editUser(userWithSameEmail);
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

    final String PASSWORD = "test_password";
    client.changePassword(user.getUsername(), user.getTempPassword(), PASSWORD);
    client.login(user.getUsername(), PASSWORD);

    user.setFirstName(generateString("NewFirst"));
    user.setLastName(generateString("NewLast"));

    UserDto updatedUser = client.editUser(user);
    assertUsersEquals(updatedUser, user);
  }

  /**
   * Test fail update user due to access policy.
   * User credentials should be changed by owner only.
   * Expected: HttpClientErrorException (403 Forbidden)
   *
   * @throws Exception the exception
   */
  @Test
  public void testFailEditUser() throws Exception {
    expectedException.expect(HttpClientErrorException.class);
    expectedException.expectMessage("403 Forbidden");

    UserDto user = createUser(KaaAuthorityDto.TENANT_DEVELOPER);
    user.setFirstName(generateString("NewFirst"));
    user.setLastName(generateString("NewLast"));
    UserDto updatedUser = client.editUser(user);
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
   * @param user      the user
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
