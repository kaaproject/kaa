/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.server.common.dao.impl;

import org.kaaproject.kaa.common.dto.EndpointUserConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointUserConfiguration;

import java.util.List;

public interface EndpointUserConfigurationDao<T extends EndpointUserConfiguration> extends Dao<T, String> {

    /**
     *
     * @param dto
     *            the endpoint user configuration
     * @return the saved endpoint user configuration object
     */
    T save(EndpointUserConfigurationDto dto);

    /**
     *
     * @param userId
     *            the endpoint user id
     * @param appToken
     *            the application token
     * @param schemaVersion
     *            the schema version
     * @return
     */
    T findByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);

    /**
     *
     * @param userId
     * @return
     */
    List<T> findByUserId(String userId);

    /**
     *
     * @param userId
     *            the endpoint user id
     * @param appToken
     *            the application token
     * @param schemaVersion
     *            the schema version
     */
    void removeByUserIdAndAppTokenAndSchemaVersion(String userId, String appToken, Integer schemaVersion);

}
