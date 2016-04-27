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

package org.kaaproject.kaa.server.common.dao;


import java.util.List;

import org.kaaproject.kaa.common.dto.ApplicationDto;

/**
 * The interface Application service.
 */
public interface ApplicationService {


    /**
     * Find applications by tenant id.
     *
     * @param tenantId the tenant id
     * @return the list of found applications
     */
    List<ApplicationDto> findAppsByTenantId(String tenantId);

    /**
     * Remove applications by tenant id.
     *
     * @param tenantId the tenant id
     */
    void removeAppsByTenantId(String tenantId);

    /**
     * Find application by id.
     *
     * @param id the string id of application
     * @return the application dto object
     */
    ApplicationDto findAppById(String id);

    /**
     * Remove application by id.
     *
     * @param id the string id of application
     */
    void removeAppById(String id);

    /**
     * Find application by application token.
     *
     * @param applicationToken the application token (random generated string)
     * @return the application dto object
     */
    ApplicationDto findAppByApplicationToken(String applicationToken);

    /**
     * Save application. If application object has id,
     * than application will be updated else will be inserted like new object.
     * <p>
     * After saving new application will be generated:
     * </p>
     * <ul>
     * <li>Default Configuration schema</li>
     * <li>Default Profile schema</li>
     * <li>Default Notification schema</li>
     * <li>Default group "All"</li>
     * <li>Default profile filter attached to group "All".</li>
     * <li>Default configuration will be generated based on base schema.</li>
     * </ul>
     *
     * @param applicationDto the application dto
     * @return the saved application dto object
     */
    ApplicationDto saveApp(ApplicationDto applicationDto);
}
