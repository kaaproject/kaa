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

import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;

/**
 * A service to manage security credentials.
 *
 * In general, each application has its own independent credentials service used
 * as a bridge to some external system. Since Kaa acts as such a system by
 * default, a single credentials service is enough to be used across all
 * applications. Its methods require an additonal parameter, though, namely the
 * application ID.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public interface CredentialsService {

    /**
     * Provides credentials information to the internal system.
     * 
     * @param applicationId The application ID
     * @param credentials The credentials to provision
     *
     * @return The credentials provided
     *
     * @throws CredentialsServiceException - if an unexpected exception occures.
     */
    CredentialsDto provideCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException;

    /**
     * Returns the credentials by ID.
     *
     * @param applicationId The application ID
     * @param credentialsId The credentials ID
     *
     * @return The credentials with the given ID
     *
     * @throws CredentialsServiceException - if an unexpected exception occures.
     */
    Optional<CredentialsDto> lookupCredentials(String applicationId, String credentialsId) throws CredentialsServiceException;

    /**
     * Sets the status of the given credentials to
     * {@link CredentialsStatus#IN_USE}.
     *
     * @param applicationId The application ID
     * @param credentialsId The credentials ID
     *
     * @throws CredentialsServiceException - if the credentials are not
     *             {@link CredentialsStatus#AVAILABLE}.
     */
    void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException;

    /**
     * Revokes the given credentials by setting their status to
     * {@link CredentialsStatus#REVOKED}.
     *
     * @param applicationId The application ID
     * @param credentialsId The credentials ID
     *
     * @throws CredentialsServiceException - if an unexpected exception occures.
     */
    void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException;
}
