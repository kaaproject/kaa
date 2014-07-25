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
package org.kaaproject.kaa.server.common.dao.model.mongo;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.security.MongoPrivilegeDto;
import org.kaaproject.kaa.common.dto.logs.security.MongoRoleDto;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureRole;
import org.springframework.test.util.ReflectionTestUtils;

public class SecureRoleTest {

    private static final String TEST_ID = "test id";
    private static final String TEST_ROLE = "test role";
    private static final List<SecurePrivilege> TEST_PRIVILEGES = new ArrayList<>();
    private static final List<String> TEST_ROLES = new ArrayList<>();

    @Test
    public void noPrivilegesTest() {
        SecureRole role = new SecureRole();
        List<MongoPrivilegeDto> privileges = null;
        Assert.assertNull(ReflectionTestUtils.invokeMethod(role, "convertPrivileges", privileges));
    }
    
    @Test
    public void basicLogEventTest() {
        SecureRole role = new SecureRole();
        
        Assert.assertNull(role.getId());
        Assert.assertNull(role.getRoleName());
        Assert.assertNull(role.getPrivileges());
        Assert.assertNull(role.getRoles());
        
        role.setId(TEST_ID);
        role.setRoleName(TEST_ROLE);
        role.setPrivileges(TEST_PRIVILEGES);
        role.setRoles(TEST_ROLES);
        
        Assert.assertEquals(TEST_ID, role.getId());
        Assert.assertEquals(TEST_ROLE, role.getRoleName());
        Assert.assertEquals(TEST_PRIVILEGES, role.getPrivileges());
        Assert.assertEquals(TEST_ROLES, role.getRoles());
        
        MongoRoleDto dto = role.toDto();
        
        Assert.assertEquals(TEST_ID, dto.getId());
        Assert.assertEquals(TEST_ROLE, dto.getRoleName());
        Assert.assertEquals(TEST_ROLES, dto.getRoles());
    }
    
    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(SecureRole.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
