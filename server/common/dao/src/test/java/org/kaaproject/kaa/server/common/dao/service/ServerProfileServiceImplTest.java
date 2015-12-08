/*
 * Copyright 2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.common.dao.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

import java.util.ArrayList;
import java.util.List;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class ServerProfileServiceImplTest extends AbstractTest {

    @After
    public void afterTest() {
        clearDBData();
    }

    @Test
    public void testSaveServerProfileSchema() {
        ServerProfileSchemaDto schemaDto = generateServiceProfileSchema(null, null);
        Assert.assertNotNull(schemaDto.getId());
    }

    @Test
    public void testFindLatestServerProfileSchema() {
        ApplicationDto app = generateApplicationDto();
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        ServerProfileSchemaDto expected = generateServiceProfileSchema(app.getId(), app.getTenantId());
        ServerProfileSchemaDto last = serverProfileService.findLatestServerProfileSchema(app.getId());
        Assert.assertEquals(expected, last);
    }

    @Test
    public void testFindServerProfileSchema() {
        ApplicationDto app = generateApplicationDto();
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        ServerProfileSchemaDto expected = generateServiceProfileSchema(app.getId(), app.getTenantId());
        ServerProfileSchemaDto found = serverProfileService.findServerProfileSchema(expected.getId());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testFindServerProfileSchemasByAppId() {
        ApplicationDto app = generateApplicationDto();
        
        List<ServerProfileSchemaDto> found = serverProfileService.findServerProfileSchemasByAppId(app.getId());
        Assert.assertEquals(1, found.size());
        
        generateServiceProfileSchema(null, null);
        generateServiceProfileSchema(null, null);
        generateServiceProfileSchema(null, null);
        generateServiceProfileSchema(null, null);

        List<ServerProfileSchemaDto> expected = new ArrayList<>();
        expected.add(found.get(0));
        expected.add(generateServiceProfileSchema(app.getId(), app.getTenantId()));
        expected.add(generateServiceProfileSchema(app.getId(), app.getTenantId()));
        expected.add(generateServiceProfileSchema(app.getId(), app.getTenantId()));
        expected.add(generateServiceProfileSchema(app.getId(), app.getTenantId()));

        found = serverProfileService.findServerProfileSchemasByAppId(app.getId());
        Assert.assertEquals(expected, found);
    }

    @Test
    public void testRemoveServerProfileSchemaById() {
        ServerProfileSchemaDto schemaDto = generateServiceProfileSchema(null, null);
        serverProfileService.removeServerProfileSchemaById(schemaDto.getId());
        Assert.assertNull(serverProfileService.findServerProfileSchema(schemaDto.getId()));
    }

    @Test
    public void testRemoveServerProfileSchemaByAppId() {
        ApplicationDto app = generateApplicationDto();
        generateServiceProfileSchema(null, null);
        generateServiceProfileSchema(null, null);
        generateServiceProfileSchema(null, null);
        generateServiceProfileSchema(null, null);

        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        generateServiceProfileSchema(app.getId(), app.getTenantId());
        serverProfileService.removeServerProfileSchemaByAppId(app.getId());
        List<ServerProfileSchemaDto> found = serverProfileService.findServerProfileSchemasByAppId(app.getId());
        Assert.assertTrue(found.isEmpty());
    }

    @Test
    public void testSaveServerProfile() {
        ServerProfileSchemaDto schemaDto = generateServiceProfileSchema(null, null);
        EndpointProfileDto ep = generateEndpointProfileDtoWithSchemaVersion(schemaDto.getApplicationId(), schemaDto.getVersion(), null);
        EndpointProfileDto updated = serverProfileService.saveServerProfile(ep.getEndpointKeyHash(), "New profile body");
        Assert.assertArrayEquals(ep.getEndpointKeyHash(), updated.getEndpointKeyHash());
        Assert.assertNotEquals(ep.getServerProfileBody(), updated.getServerProfileBody());
        Assert.assertEquals(ep.getServerProfileVersion(), updated.getServerProfileVersion());
    }

    @Test
    public void testFindServerProfileSchemaByKeyHash() {
        ServerProfileSchemaDto schemaDto = generateServiceProfileSchema(null, null);
        EndpointProfileDto ep = generateEndpointProfileDtoWithSchemaVersion(schemaDto.getApplicationId(), schemaDto.getVersion(), null);
        EndpointProfileDto found = endpointService.findEndpointProfileByKeyHash(ep.getEndpointKeyHash());
        ServerProfileSchemaDto foundSchema = serverProfileService.findServerProfileSchemaByAppIdAndVersion(found.getApplicationId(), found.getServerProfileVersion());
        Assert.assertEquals(schemaDto, foundSchema);
    }
}
