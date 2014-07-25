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
import org.kaaproject.kaa.common.dto.logs.security.MongoUserDto;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureUser;

public class SecureUserTest {
    
    private static final String TEST_ID = "test id";
    private static final String TEST_NAME = "test name";
    private static final String TEST_PASSWORD = "test password";
    private static final List<String> TEST_ROLES = new ArrayList<>();

    @Test
    public void basicLogEventTest() {
        SecureUser user = new SecureUser();
        
        Assert.assertNull(user.getId());
        Assert.assertNull(user.getUserName());
        Assert.assertNull(user.getPassword());
        Assert.assertNull(user.getRoles());
        
        user.setId(TEST_ID);
        user.setUserName(TEST_NAME);
        user.setPassword(TEST_PASSWORD);
        user.setRoles(TEST_ROLES);
        
        Assert.assertEquals(TEST_ID, user.getId());
        Assert.assertEquals(TEST_NAME, user.getUserName());
        Assert.assertEquals(TEST_PASSWORD, user.getPassword());
        Assert.assertEquals(TEST_ROLES, user.getRoles());
        
        MongoUserDto dto = user.toDto();
        
        Assert.assertEquals(TEST_ID, dto.getId());
        Assert.assertEquals(TEST_NAME, dto.getUserName());
        Assert.assertEquals(TEST_PASSWORD, dto.getPassword());
        Assert.assertEquals(TEST_ROLES, dto.getRoles());
    }
    
    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(SecureUser.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
