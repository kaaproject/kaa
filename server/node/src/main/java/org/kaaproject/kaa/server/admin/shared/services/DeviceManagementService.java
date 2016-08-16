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
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/deviceManagementService")
public interface DeviceManagementService extends RemoteService {

    CredentialsDto provisionCredentials(String applicationToken, String credentialsBody) throws KaaAdminServiceException;

    void provisionRegistration(String applicationToken, String credentialsId, Integer serverProfileVersion, String serverProfileBody) throws KaaAdminServiceException;

    void revokeCredentials(String applicationToken, String credentialsId) throws KaaAdminServiceException;

    void onCredentialsRevoked(String applicationToken, String credentialsId) throws KaaAdminServiceException;

    CredentialsStatus getCredentialsStatus(
            String applicationToken,
            String credentialsId
    ) throws KaaAdminServiceException;

    List<String> getCredentialsServiceNames() throws KaaAdminServiceException;

}
