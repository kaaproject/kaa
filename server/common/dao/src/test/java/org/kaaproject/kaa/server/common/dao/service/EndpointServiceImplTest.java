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

package org.kaaproject.kaa.server.common.dao.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.EndpointUserDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.AbstractTest;

@Ignore("This test should be extended and initialized with proper context in each NoSQL submodule")
public class EndpointServiceImplTest extends AbstractTest {

    private static final String INCORRECT_ID = "incorrect id";
    private static final String DEFAULT_LIMIT = "1";
    private static final String DEFAULT_OFFSET = "0";

    @Test
    public void findEndpointGroupsByAppIdTest() {
        EndpointGroupDto group = generateEndpointGroupDto(null);
        List<EndpointGroupDto> groups = endpointService.findEndpointGroupsByAppId(group.getApplicationId());
        Assert.assertNotNull(groups);
        Assert.assertFalse(groups.isEmpty());
        Assert.assertEquals(2, groups.size());;
    }

    @Test(expected = IncorrectParameterException.class)
    public void findEndpointGroupByIdTest() {
        EndpointGroupDto group = generateEndpointGroupDto(null);
        EndpointGroupDto foundGroup = endpointService.findEndpointGroupById(group.getId());
        Assert.assertNotNull(foundGroup);
        group = endpointService.findEndpointGroupById(INCORRECT_ID);
    }

    @Test
    public void findEndpointProfileByEndpointGroupIdTest() {
        EndpointGroupDto group = generateEndpointGroupDto(null);
        String endpointGroupId = group.getId();
        PageLinkDto pageLinkDto = new PageLinkDto(endpointGroupId, DEFAULT_LIMIT, DEFAULT_OFFSET);
        EndpointProfileDto savedEndpointProfileDto = generateEndpointProfileWithGroupIdDto(endpointGroupId, false);
        EndpointProfilesPageDto endpointProfilesPage = endpointService.findEndpointProfileByEndpointGroupId(pageLinkDto);
        EndpointProfileDto endpointProfileDto = endpointProfilesPage.getEndpointProfiles().get(0);
        Assert.assertEquals(savedEndpointProfileDto, endpointProfileDto);
    }

    @Test
    public void findEndpointProfileBodyByEndpointGroupIdTest() {
        EndpointGroupDto group = generateEndpointGroupDto(null);
        String endpointGroupId = group.getId();
        PageLinkDto pageLinkDto = new PageLinkDto(endpointGroupId, DEFAULT_LIMIT, DEFAULT_OFFSET);
        EndpointProfileDto savedEndpointProfileDto = generateEndpointProfileWithGroupIdDto(endpointGroupId, false);
        EndpointProfilesBodyDto endpointProfilesPage = endpointService.findEndpointProfileBodyByEndpointGroupId(pageLinkDto);
        EndpointProfileBodyDto endpointProfileBodyDto = endpointProfilesPage.getEndpointProfilesBody().get(0);
        Assert.assertEquals(savedEndpointProfileDto.getClientProfileBody(), endpointProfileBodyDto.getProfile());
    }

    @Test
    public void findEndpointProfileByKeyHashTest() {
        String endpointGroupId = "124";
        EndpointProfileDto savedEndpointProfileDto = generateEndpointProfileWithGroupIdDto(endpointGroupId, false);
        EndpointProfileDto endpointProfileDto = endpointService.findEndpointProfileByKeyHash(savedEndpointProfileDto.getEndpointKeyHash());
        Assert.assertEquals(savedEndpointProfileDto, endpointProfileDto);
    }

    @Test
    public void findEndpointProfileBodyByKeyHashTest() {
        String endpointGroupId = "124";
        EndpointProfileDto savedEndpointProfileDto = generateEndpointProfileWithGroupIdDto(endpointGroupId, false);
        EndpointProfileBodyDto endpointProfileBodyDto = endpointService.findEndpointProfileBodyByKeyHash(savedEndpointProfileDto.getEndpointKeyHash());
        Assert.assertEquals(savedEndpointProfileDto.getClientProfileBody(), endpointProfileBodyDto.getProfile());
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveEndpointGroupWithSameWeightTest() {
        EndpointGroupDto group = generateEndpointGroupDto(null);
        EndpointGroupDto found = endpointService.findEndpointGroupById(group.getId());
        found.setId(null);
        endpointService.saveEndpointGroup(found);
    }

    @Test
    public void removeEndpointGroupByAppIdTest() {
        String appId = generateApplicationDto().getId();
        List<EndpointGroupDto> groupDtoList = endpointService.findEndpointGroupsByAppId(appId);
        Assert.assertNotNull(groupDtoList);
        Assert.assertFalse(groupDtoList.isEmpty());
        endpointService.removeEndpointGroupByAppId(appId);
        groupDtoList = endpointService.findEndpointGroupsByAppId(appId);
        Assert.assertNotNull(groupDtoList);
        Assert.assertTrue(groupDtoList.isEmpty());
    }

    @Test
    public void removeEndpointGroupByIdTest() {

    }

    @Test(expected = IncorrectParameterException.class)
    public void invalidUpdateEndpointGroupTest() {
        ApplicationDto app = generateApplicationDto();
        List<EndpointGroupDto> groups = endpointService.findEndpointGroupsByAppId(app.getId());
        Assert.assertFalse(groups.isEmpty());
        EndpointGroupDto group = groups.get(0);
        group.setName("Updated Group Name");
        endpointService.saveEndpointGroup(group);
    }

    @Test(expected = IncorrectParameterException.class)
    public void saveEndpointGroupWithExistingWeightTest() {
        ApplicationDto app = generateApplicationDto();
        List<EndpointGroupDto> groups = endpointService.findEndpointGroupsByAppId(app.getId());
        Assert.assertFalse(groups.isEmpty());
        EndpointGroupDto group = groups.get(0);
        group.setId(null);
        group.setName("Updated Group Name");
        endpointService.saveEndpointGroup(group);
    }

    @Test
    public void findAllEndpointUsersTest() {
        removeAllEndpointUsers();
        TenantDto tenantDto = generateTenantDto();
        EndpointUserDto endpointUserDto = generateEndpointUserDto(tenantDto.getId());
        List<EndpointUserDto> saved = new ArrayList<>(1);
        saved.add(endpointUserDto);
        List<EndpointUserDto> endpointUsers = endpointService.findAllEndpointUsers();
        Assert.assertEquals(saved, endpointUsers);
    }

    private void removeAllEndpointUsers() {
        List<EndpointUserDto> endpointUsers = endpointService.findAllEndpointUsers();
        for (EndpointUserDto endpointUser : endpointUsers) {
            endpointService.removeEndpointUserById(endpointUser.getId());
        }
    }

    @Test
    public void findEndpointUserByIdTest() {
        TenantDto tenantDto = generateTenantDto();
        EndpointUserDto savedEndpointUserDto = generateEndpointUserDto(tenantDto.getId());
        EndpointUserDto endpointUser = endpointService.findEndpointUserById(savedEndpointUserDto.getId());
        Assert.assertEquals(savedEndpointUserDto, endpointUser);
    }

    @Test
    public void saveEndpointUserTest() {
        TenantDto tenantDto = generateTenantDto();
        EndpointUserDto savedEndpointUserDto = generateEndpointUserDto(tenantDto.getId());
        EndpointUserDto endpointUser = endpointService.findEndpointUserById(savedEndpointUserDto.getId());
        Assert.assertEquals(savedEndpointUserDto, endpointUser);
    }

    @Test
    public void removeEndpointUserByIdTest() {
        TenantDto tenantDto = generateTenantDto();
        EndpointUserDto savedEndpointUserDto = generateEndpointUserDto(tenantDto.getId());
        endpointService.removeEndpointUserById(savedEndpointUserDto.getId());
        EndpointUserDto endpointUser = endpointService.findEndpointUserById(savedEndpointUserDto.getId());
        Assert.assertNull(endpointUser);
    }

    @Test
    public void generateEndpointUserAccessTokenTest() {
        TenantDto tenantDto = generateTenantDto();
        EndpointUserDto savedEndpointUserDto = generateEndpointUserDto(tenantDto.getId());
        Assert.assertNull(savedEndpointUserDto.getAccessToken());
        String generatedAccessToken = endpointService.generateEndpointUserAccessToken(savedEndpointUserDto.getExternalId(), savedEndpointUserDto.getTenantId());
        EndpointUserDto endpointUser = endpointService.findEndpointUserById(savedEndpointUserDto.getId());
        Assert.assertNotNull(generatedAccessToken);
        Assert.assertEquals(generatedAccessToken, endpointUser.getAccessToken());
    }
}
