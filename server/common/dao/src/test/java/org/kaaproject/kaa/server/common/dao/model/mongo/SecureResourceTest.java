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

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.logs.security.MongoResourceDto;
import org.kaaproject.kaa.server.common.dao.model.mongo.SecureResource;

public class SecureResourceTest {
    
    private static final String TEST_DB = "test db";
    private static final String TEST_COLLECCTION = "test collection";

    @Test
    public void basicLogEventTest() {
        SecureResource resource = new SecureResource();
        
        Assert.assertNull(resource.getDB());
        Assert.assertNull(resource.getCollection());
        
        resource.setDB(TEST_DB);
        resource.setCollection(TEST_COLLECCTION);
        
        Assert.assertEquals(TEST_DB, resource.getDB());
        Assert.assertEquals(TEST_COLLECCTION, resource.getCollection());
        
        MongoResourceDto dto = resource.toDto();
        
        Assert.assertEquals(TEST_DB, dto.getDB());
        Assert.assertEquals(TEST_COLLECCTION, dto.getCollection());
    }
    
    @Test
    public void hashCodeEqualsTest(){
        EqualsVerifier.forClass(SecureResource.class).suppress(Warning.NONFINAL_FIELDS).verify();
    }

}
