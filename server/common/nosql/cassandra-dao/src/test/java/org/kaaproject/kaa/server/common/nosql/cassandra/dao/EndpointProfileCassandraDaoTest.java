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

package org.kaaproject.kaa.server.common.nosql.cassandra.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointGroupStateDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.nosql.cassandra.dao.model.CassandraEndpointProfile;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/cassandra-client-test-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EndpointProfileCassandraDaoTest extends AbstractCassandraTest {

    @Test
    public void testFindByEndpointGroupId() throws Exception {
        List<EndpointProfileDto> endpointProfileList = new ArrayList<>();
        List<EndpointGroupStateDto> cfGroupState = new ArrayList<>();
        List<String> id = new ArrayList<>();
        String appId = "1";
        for (int i = 0; i < 5; i++) {
            endpointProfileList.add(generateEndpointProfileWithEndpointGroupId(appId, null, null));
            cfGroupState.addAll(endpointProfileList.get(i).getCfGroupStates());
            id.add(cfGroupState.get(0).getEndpointGroupId());
        }
        String limit = "3";
        String offset = "0";
        int lim = Integer.valueOf(limit);
        PageLinkDto pageLink = new PageLinkDto(id.get(0), limit, offset);
        EndpointGroupDto endpointGroupDto = new EndpointGroupDto();
        endpointGroupDto.setWeight(1);
        endpointGroupDto.setApplicationId(endpointProfileList.get(0).getApplicationId());
        EndpointProfilesPageDto found = endpointProfileDao.findByEndpointGroupId(pageLink, endpointGroupDto);
        Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        Assert.assertEquals(lim, found.getEndpointProfiles().size());
        endpointGroupDto.setWeight(0);
        EndpointProfilesPageDto foundbyAppId = endpointProfileDao.findByEndpointGroupId(pageLink, endpointGroupDto);
        Assert.assertFalse(foundbyAppId.getEndpointProfiles().isEmpty());
        Assert.assertEquals(lim, foundbyAppId.getEndpointProfiles().size());
    }

    @Test
    public void testUpdate() throws Exception {
        List<EndpointGroupStateDto> cfGroupStateSave = new ArrayList<EndpointGroupStateDto>();
        List<EndpointGroupStateDto> cfGroupStateUpdate = new ArrayList<EndpointGroupStateDto>();
        PageLinkDto pageLink;
        EndpointProfilesPageDto found;
        EndpointGroupDto endpointGroupDto = new EndpointGroupDto();
        endpointGroupDto.setWeight(1);
        cfGroupStateSave.add(new EndpointGroupStateDto("111", null, null));
        cfGroupStateSave.add(new EndpointGroupStateDto("222", null, null));
        cfGroupStateSave.add(new EndpointGroupStateDto("333", null, null));
        EndpointProfileDto endpointProfileSave = generateEndpointProfileForTestUpdate(null, cfGroupStateSave);
        endpointProfileDao.save(endpointProfileSave);
        cfGroupStateUpdate.add(new EndpointGroupStateDto("111", null, null));
        cfGroupStateUpdate.add(new EndpointGroupStateDto("444", null, null));
        EndpointProfileDto endpointProfileUpdate = generateEndpointProfileForTestUpdate("11", cfGroupStateUpdate);
        endpointProfileDao.save(endpointProfileUpdate);
        String limit = "10";
        String offset = "0";
        String[] endpointGroupId = {"111", "444", "222", "333"};
        for (int i = 0; i < 2; i++) {
            pageLink = new PageLinkDto(endpointGroupId[i], limit, offset);
            found = endpointProfileDao.findByEndpointGroupId(pageLink, endpointGroupDto);
            Assert.assertFalse(found.getEndpointProfiles().isEmpty());
        }
        for (int i = 2; i < 4; i++) {
            pageLink = new PageLinkDto(endpointGroupId[i], limit, offset);
            found = endpointProfileDao.findByEndpointGroupId(pageLink, endpointGroupDto);
            Assert.assertTrue(found.getEndpointProfiles().isEmpty());
        }
    }

    @Test
    public void testSave() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(endpointProfile, found.toDto());
    }

    @Test
    public void testFindByKeyHash() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null);
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testGetCountByKeyHash() throws Exception {
        EndpointProfileDto endpointProfile = generateEndpointProfile(null, null, null);
        long count = endpointProfileDao.getCountByKeyHash(endpointProfile.getEndpointKeyHash());
        Assert.assertEquals(1L, count);
    }

    @Test
    public void testRemoveByKeyHash() throws Exception {
        List<EndpointGroupStateDto> cfGroupState = new ArrayList<EndpointGroupStateDto>();
        cfGroupState.add(new EndpointGroupStateDto("111", null, null));
        EndpointProfileDto expected = generateEndpointProfileForTestUpdate(null, cfGroupState);
        endpointProfileDao.save(expected);
        endpointProfileDao.removeByKeyHash(expected.getEndpointKeyHash());
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNull(found);
    }

    @Test
    public void testRemoveByAppId() throws Exception {
        List<EndpointGroupStateDto> cfGroupState = new ArrayList<EndpointGroupStateDto>();
        cfGroupState.add(new EndpointGroupStateDto("111", null, null));
        EndpointProfileDto expected = generateEndpointProfileForTestUpdate(null, cfGroupState);
        endpointProfileDao.save(expected);
        endpointProfileDao.removeByAppId(expected.getApplicationId());
        EndpointProfile found = endpointProfileDao.findByKeyHash(expected.getEndpointKeyHash());
        Assert.assertNull(found);
    }

    @Test
    public void testFindByAccessToken() throws Exception {
        EndpointProfileDto expected = generateEndpointProfile(null, null, null);
        EndpointProfile found = endpointProfileDao.findByAccessToken(expected.getAccessToken());
        Assert.assertEquals(expected, found.toDto());
    }

    @Test
    public void testFindByEndpointUserId() throws Exception {
        EndpointProfileDto endpointProfileDto = generateEndpointProfile(null, null, null);
        EndpointUserDto endpointUserDto = generateEndpointUser(Arrays.asList(endpointProfileDto.getId()));
        List<CassandraEndpointProfile> found = endpointProfileDao.findByEndpointUserId(endpointUserDto.getId());
        Assert.assertFalse(found.isEmpty());
        Assert.assertEquals(endpointProfileDto, found.get(0).toDto());
    }
}
