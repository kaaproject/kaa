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

package org.kaaproject.kaa.server.admin.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.kaaproject.kaa.common.dto.EndpointGroupDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.ProfileFilterDto;
import org.kaaproject.kaa.common.dto.ProfileFilterRecordDto;
import org.kaaproject.kaa.common.dto.ProfileVersionPairDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/groupService")
public interface GroupService extends RemoteService {

    List<EndpointGroupDto> getEndpointGroupsByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<EndpointGroupDto> getEndpointGroupsByApplicationId(String applicationId) throws KaaAdminServiceException;

    EndpointGroupDto getEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    EndpointGroupDto editEndpointGroup(EndpointGroupDto endpointGroup) throws KaaAdminServiceException;

    void deleteEndpointGroup(String endpointGroupId) throws KaaAdminServiceException;

    List<ProfileFilterRecordDto> getProfileFilterRecordsByEndpointGroupId(String endpointGroupId, boolean includeDeprecated) throws KaaAdminServiceException;

    ProfileFilterRecordDto getProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException;

    List<ProfileVersionPairDto> getVacantProfileSchemasByEndpointGroupId(String endpointGroupId) throws KaaAdminServiceException;

    ProfileFilterDto editProfileFilter(ProfileFilterDto profileFilter) throws KaaAdminServiceException;

    ProfileFilterDto activateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    ProfileFilterDto deactivateProfileFilter(String profileFilterId) throws KaaAdminServiceException;

    void deleteProfileFilterRecord(String endpointProfileSchemaId, String serverProfileSchemaId, String endpointGroupId) throws KaaAdminServiceException;

    EndpointProfilesPageDto getEndpointProfileByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException;

    EndpointProfilesBodyDto getEndpointProfileBodyByEndpointGroupId(String endpointGroupId, String limit, String offset) throws KaaAdminServiceException;

}
