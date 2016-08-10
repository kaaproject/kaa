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
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;

import java.util.List;

@RemoteServiceRelativePath("springGwtServices/verifierService")
public interface VerifierService extends RemoteService {

    List<UserVerifierDto> getRestUserVerifiersByApplicationToken(String appToken) throws KaaAdminServiceException;

    List<UserVerifierDto> getRestUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException;

    List<UserVerifierDto> getUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException;

    UserVerifierDto getUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto getRestUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    void deleteUserVerifier(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto getUserVerifierForm(String userVerifierId) throws KaaAdminServiceException;

    UserVerifierDto editUserVerifierForm(UserVerifierDto userVerifier) throws KaaAdminServiceException;

    List<PluginInfoDto> getUserVerifierPluginInfos() throws KaaAdminServiceException;
}
