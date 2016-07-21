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

package org.kaaproject.kaa.server.admin.services;

import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.common.dto.user.UserVerifierDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.plugin.PluginInfoDto;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.VerifierService;
import org.kaaproject.kaa.server.common.plugin.PluginType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static org.kaaproject.kaa.server.admin.services.util.Utils.getCurrentUser;
import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("verifierService")
public class VerifierServiceImpl extends AbstractAdminService implements VerifierService {

    @Override
    public List<UserVerifierDto> getRestUserVerifiersByApplicationToken(String appToken) throws KaaAdminServiceException {
        return getRestUserVerifiersByApplicationId(checkApplicationToken(appToken));
    }

    @Override
    public List<UserVerifierDto> getRestUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException {
        List<UserVerifierDto> userVerifiers = getUserVerifiersByApplicationId(appId);
        for (UserVerifierDto userVerifier : userVerifiers) {
            setPluginJsonConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
        }
        return userVerifiers;
    }

    @Override
    public List<UserVerifierDto> getUserVerifiersByApplicationId(String appId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            checkApplicationId(appId);
            return controlService.getUserVerifiersByApplicationId(appId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto getRestUserVerifier(String userVerifierId) throws KaaAdminServiceException {
        UserVerifierDto userVerifier = getUserVerifier(userVerifierId);
        setPluginJsonConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
        return userVerifier;
    }

    @Override
    public UserVerifierDto getUserVerifier(String userVerifierId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            UserVerifierDto userVerifier = controlService.getUserVerifier(userVerifierId);
            Utils.checkNotNull(userVerifier);
            checkApplicationId(userVerifier.getApplicationId());
            return userVerifier;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto editRestUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException {
        setPluginRawConfigurationFromJson(userVerifier, PluginType.USER_VERIFIER);
        UserVerifierDto savedUserVerifier = editUserVerifier(userVerifier);
        setPluginJsonConfigurationFromRaw(savedUserVerifier, PluginType.USER_VERIFIER);
        return savedUserVerifier;
    }

    @Override
    public UserVerifierDto editUserVerifier(UserVerifierDto userVerifier) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(userVerifier.getId())) {
                userVerifier.setCreatedUsername(getCurrentUser().getUsername());
                checkApplicationId(userVerifier.getApplicationId());
            } else {
                UserVerifierDto storedUserVerifier = controlService.getUserVerifier(userVerifier.getId());
                Utils.checkNotNull(storedUserVerifier);
                checkApplicationId(storedUserVerifier.getApplicationId());
            }
            return controlService.editUserVerifier(userVerifier);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteUserVerifier(String userVerifierId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(userVerifierId)) {
                throw new IllegalArgumentException("The userVerifierId parameter is empty.");
            }
            UserVerifierDto userVerifier = controlService.getUserVerifier(userVerifierId);
            Utils.checkNotNull(userVerifier);
            checkApplicationId(userVerifier.getApplicationId());
            controlService.deleteUserVerifier(userVerifierId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto getUserVerifierForm(String userVerifierId) throws KaaAdminServiceException {
        UserVerifierDto userVerifier = getUserVerifier(userVerifierId);
        try {
            setPluginFormConfigurationFromRaw(userVerifier, PluginType.USER_VERIFIER);
            return userVerifier;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public UserVerifierDto editUserVerifierForm(UserVerifierDto userVerifier) throws KaaAdminServiceException {
        try {
            setPluginRawConfigurationFromForm(userVerifier);
            UserVerifierDto saved = editUserVerifier(userVerifier);
            return saved;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public List<PluginInfoDto> getUserVerifierPluginInfos() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        return new ArrayList<PluginInfoDto>(pluginsInfo.get(PluginType.USER_VERIFIER).values());
    }

}
