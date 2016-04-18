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

import org.kaaproject.kaa.common.dto.admin.AuthResultDto;
import org.kaaproject.kaa.common.dto.admin.ResultCode;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/kaaAuthService")
public interface KaaAuthService extends RemoteService {

    public AuthResultDto checkAuth() throws Exception;

    public void createKaaAdmin(String username, String password) throws KaaAdminServiceException;

    ResultCode changePassword(String username, String oldPassword, String newPassword) throws Exception;

    ResultCode checkUserNameOccupied(String username, Long userId) throws Exception;

    ResultCode checkEmailOccupied(String email, Long userId) throws Exception;
    
    ResultCode checkUsernameOrEmailExists(String usernameOrEmail) throws Exception;
    
    ResultCode sendPasswordResetLinkByEmail(String usernameOrEmail) throws Exception;
    
    ResultCode resetPasswordByResetHash(String passwordResetHash) throws Exception;

}
