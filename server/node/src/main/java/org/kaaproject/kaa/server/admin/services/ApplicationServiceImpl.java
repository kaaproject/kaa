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

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.common.dto.KaaAuthorityDto;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.ApplicationService;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.ServiceErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.kaaproject.kaa.server.admin.shared.util.Utils.isEmpty;

@Service("applicationService")
public class ApplicationServiceImpl extends AbstractAdminService implements ApplicationService {

    @Override
    public List<ApplicationDto> getApplications() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return controlService.getApplicationsByTenantId(getTenantId());
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto getApplicationByApplicationToken(String applicationToken) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            if (isEmpty(applicationToken)) {
                throw new KaaAdminServiceException(ServiceErrorCode.INVALID_ARGUMENTS);
            }
            ApplicationDto application = controlService.getApplicationByApplicationToken(applicationToken);
            checkApplication(application);
            return application;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto getApplication(String applicationId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN, KaaAuthorityDto.TENANT_DEVELOPER, KaaAuthorityDto.TENANT_USER);
        try {
            return checkApplicationId(applicationId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public ApplicationDto editApplication(ApplicationDto application) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.TENANT_ADMIN);
        try {
            if (!isEmpty(application.getId())) {
                checkApplicationId(application.getId());
            }
            if (!isEmpty(application.getTenantId())) {
                checkTenantId(application.getTenantId());
            }
            application.setTenantId(getTenantId());
            return controlService.editApplication(application);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

}
