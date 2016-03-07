/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.common.dao.impl;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointCredentials;

/**
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public interface EndpointCredentialsDao<T extends EndpointCredentials> extends Dao<T, String> {

    /**
     * Saves the given endpoint security credentials.
     *
     * @param endpointCredentials
     *        The endpoint security credentials
     *
     * @return The endpoint security credentials saved
     */
    T save(EndpointCredentialsDto endpointCredentials);

    /**
     * Returns the security credentials of the given endpoint.
     *
     * @param endpointId
     *        The endpoint ID
     *
     * @return The security credentials of the given endpoint
     */
    T findByEndpointId(String endpointId);

    /**
     * Returns the list of endpoint security credentials for the given
     * application.
     *
     * @param applicationId
     *        The application ID
     *
     * @return The list of endpoint security credentials for the given
     *         application
     */
    List<T> findByApplicationId(String applicationId);

    /**
     * Removes the security credentials of the given endpoint.
     *
     * @param endpointId
     *        The endpoint ID
     */
    void removeByEndpointId(String endpointId);
}
