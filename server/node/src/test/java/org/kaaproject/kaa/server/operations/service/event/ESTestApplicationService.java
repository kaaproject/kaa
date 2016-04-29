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

package org.kaaproject.kaa.server.operations.service.event;

import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;

/**
 * @author Andrey Panasenko
 *
 */
public class ESTestApplicationService implements ApplicationService {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.ApplicationService#findAppsByTenantId(java.lang.String)
     */
    @Override
    public List<ApplicationDto> findAppsByTenantId(String tenantId) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.ApplicationService#removeAppsByTenantId(java.lang.String)
     */
    @Override
    public void removeAppsByTenantId(String tenantId) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.ApplicationService#findAppById(java.lang.String)
     */
    @Override
    public ApplicationDto findAppById(String id) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.ApplicationService#removeAppById(java.lang.String)
     */
    @Override
    public void removeAppById(String id) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.ApplicationService#findAppByApplicationToken(java.lang.String)
     */
    @Override
    public ApplicationDto findAppByApplicationToken(String applicationToken) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.dao.ApplicationService#saveApp(org.kaaproject.kaa.common.dto.ApplicationDto)
     */
    @Override
    public ApplicationDto saveApp(ApplicationDto applicationDto) {
        // TODO Auto-generated method stub
        return null;
    }

}
