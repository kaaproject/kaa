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
import org.kaaproject.avro.ui.shared.RecordField;
import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.server.admin.shared.endpoint.EndpointProfileViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.CtlSchemaFormDto;
import org.kaaproject.kaa.server.admin.shared.schema.ProfileSchemaViewDto;
import org.kaaproject.kaa.server.admin.shared.schema.SchemaInfoDto;
import org.kaaproject.kaa.server.admin.shared.schema.ServerProfileSchemaViewDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/profileService")
public interface ProfileService extends RemoteService {

    List<EndpointProfileSchemaDto> getProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<EndpointProfileSchemaDto> getProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    EndpointProfileSchemaDto getProfileSchema(String profileSchemaId) throws KaaAdminServiceException;

    EndpointProfileSchemaDto saveProfileSchema(EndpointProfileSchemaDto profileSchema) throws KaaAdminServiceException;

    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationToken(String applicationToken) throws KaaAdminServiceException;

    List<ServerProfileSchemaDto> getServerProfileSchemasByApplicationId(String applicationId) throws KaaAdminServiceException;

    ServerProfileSchemaDto getServerProfileSchema(String serverProfileSchemaId) throws KaaAdminServiceException;

    ServerProfileSchemaDto saveServerProfileSchema(ServerProfileSchemaDto serverProfileSchema) throws KaaAdminServiceException;

    EndpointProfileDto updateServerProfile(String endpointKeyHash, int serverProfileVersion, String serverProfileBody) throws KaaAdminServiceException;

    EndpointProfileDto getEndpointProfileByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException;

    EndpointProfileBodyDto getEndpointProfileBodyByKeyHash(String endpointKeyHash) throws KaaAdminServiceException;

    List<EndpointProfileDto> getEndpointProfilesByUserExternalId(String endpointUserExternalId) throws KaaAdminServiceException;

    void removeEndpointProfileByKeyHash(String endpointKeyHash) throws KaaAdminServiceException;;

    ProfileSchemaViewDto saveProfileSchemaView(ProfileSchemaViewDto profileSchema) throws KaaAdminServiceException;

    ProfileSchemaViewDto createProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    ProfileSchemaViewDto getProfileSchemaView(String profileSchemaId) throws KaaAdminServiceException;

    List<SchemaInfoDto> getServerProfileSchemaInfosByApplicationId(String applicationId) throws KaaAdminServiceException;

    List<SchemaInfoDto> getServerProfileSchemaInfosByEndpointKey(String endpointKeyHash) throws KaaAdminServiceException;

    ServerProfileSchemaViewDto getServerProfileSchemaView(String serverProfileSchemaId) throws KaaAdminServiceException;

    ServerProfileSchemaViewDto saveServerProfileSchemaView(ServerProfileSchemaViewDto serverProfileSchema) throws KaaAdminServiceException;

    ServerProfileSchemaViewDto createServerProfileSchemaFormCtlSchema(CtlSchemaFormDto ctlSchemaForm) throws KaaAdminServiceException;

    SchemaInfoDto getEndpointProfileSchemaInfo(String endpointProfileSchemaId) throws KaaAdminServiceException;

    SchemaInfoDto getServerProfileSchemaInfo(String serverProfileSchemaId) throws KaaAdminServiceException;

    boolean testProfileFilter(RecordField endpointProfile, RecordField serverProfile, String filterBody) throws KaaAdminServiceException;

    EndpointProfileViewDto getEndpointProfileViewByKeyHash(String endpointProfileKeyHash) throws KaaAdminServiceException;

    EndpointProfileDto updateServerProfile(String endpointKeyHash, int serverProfileVersion, RecordField serverProfileRecord) throws KaaAdminServiceException;

}
