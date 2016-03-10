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

package org.kaaproject.kaa.server.common.dao;

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;

/**
 * This service is used to retrieve endpoint credentials for further processing.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public interface EndpointVerificationService {

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
    List<EndpointCredentialsDto> getEndpointCredentialsByApplicationId(String applicationId);

    /**
     * Returns the security credentials of the given endpoint.
     *
     * @param endpointKeyHash
     *        The endpoint public key hash
     *
     * @return The security credentials of the given endpoint
     */
    EndpointCredentialsDto getEndpointCredentialsByEndpointKeyHash(byte[] endpointKeyHash);

    /**
     * Saves the given endpoint security credentials.
     *
     * @param endpointCredentials
     *        The endpoint security credentials
     *
     * @return The endpoint security credentials saved
     */
    EndpointCredentialsDto saveEndpointCredentials(EndpointCredentialsDto endpointCredentials);

    /**
     * Removes the security credentials of the given endpoint.
     *
     * @param endpointKeyHash
     *        The endpoint public key hash
     */
    void removeEndpointCredentialsByEndpointKeyHash(byte[] endpointKeyHash);
}
