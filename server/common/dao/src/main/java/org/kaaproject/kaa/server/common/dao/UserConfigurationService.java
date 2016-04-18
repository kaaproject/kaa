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

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;

import java.util.List;

/**
 * Provides methods to operate with {@link EndpointUserConfigurationDto} 
 */
public interface UserConfigurationService {

    /**
     *
     * @param   dto the dto
     * @return  the endpoint user configuration dto
     */
    EndpointUserConfigurationDto saveUserConfiguration(EndpointUserConfigurationDto dto);

    /**
     *
     * @param   userId          the user id
     * @param   appToken        the app token
     * @param   schemaVersion   the schema version
     * @return  the endpoint user configuration dto
     */
    EndpointUserConfigurationDto findUserConfigurationByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);

    /**
     *
     * @param   userId  the user id
     * @return  the list endpoint user configuration dto
     */
    List<EndpointUserConfigurationDto> findUserConfigurationByUserId(String userId);

    /**
     *
     * @param userId        the user id
     * @param appToken      the app token
     * @param schemaVersion the schema version
     */
    void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);
}
