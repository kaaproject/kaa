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
import org.kaaproject.kaa.common.dto.TenantDto;
import org.kaaproject.kaa.common.dto.UserDto;
import org.kaaproject.kaa.server.admin.services.entity.User;
import org.kaaproject.kaa.server.admin.services.util.Utils;
import org.kaaproject.kaa.server.admin.shared.services.KaaAdminServiceException;
import org.kaaproject.kaa.server.admin.shared.services.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("tenantService")
public class TenantServiceImpl extends AbstractAdminService implements TenantService {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TenantServiceImpl.class);

    @Override
    public List<TenantDto> getTenants() throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            List<TenantDto> tenants = controlService.getTenants();
            Utils.checkNotNull(tenants);
            return  tenants;

        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TenantDto getTenant(String tenantId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            TenantDto tenantDto = controlService.getTenant(tenantId);
            Utils.checkNotNull(tenantDto);
            return tenantDto;
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public TenantDto editTenant(TenantDto tenantUser) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            return controlService.editTenant(tenantUser);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }

    @Override
    public void deleteTenant(String tenantId) throws KaaAdminServiceException {
        checkAuthority(KaaAuthorityDto.KAA_ADMIN);
        try {
            controlService.deleteTenant(tenantId);
        } catch (Exception e) {
            throw Utils.handleException(e);
        }
    }


}
