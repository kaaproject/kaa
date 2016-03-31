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

package org.kaaproject.kaa.server.node.service.credentials;

import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;

/**
 * A service that communicates with an external system to manage credentials
 * information.
 *
 * @author Bohdan Khablenko
 * @author Andrew Shvayka
 *
 * @since v0.9.0
 */
public interface CredentialsService {

    /**
     * Provide credentials information to the external system.
     *
     * @param credentials The credentials to provision
     *
     * @return The credentials provided
     *
     * @throws UnsupportedOperationException - if the underlying implementation
     *             forbids credentials provisioning.
     * @throws CredentialsServiceException - if an unexpected exception occures.
     */
    CredentialsDto provideCredentials(CredentialsDto credentials) throws CredentialsServiceException;

    /**
     * Returns the credentials by ID.
     *
     * @param credentialsId The credentials ID
     *
     * @return The credentials with the given ID
     *
     * @throws CredentialsServiceException - if an unexpected exception occures.
     */
    Optional<CredentialsDto> lookupCredentials(String credentialsId) throws CredentialsServiceException;

    /**
     * Sets the status of the given credentials to
     * {@link CredentialsStatus#IN_USE}.
     *
     * @param credentialsId The credentials ID
     *
     * @throws CredentialsServiceException - if the credentials are not
     *             {@link CredentialsStatus#AVAILABLE}.
     */
    void markCredentialsInUse(String credentialsId) throws CredentialsServiceException;

    /**
     * Revokes the given credentials by setting their status to
     * {@link CredentialsStatus#REVOKED}.
     *
     * @param credentialsId The credentials ID
     *
     * @throws UnsupportedOperationException - if the underlying implementation
     *             forbids credentials revokation.
     * @throws CredentialsServiceException - if an unexpected exception occures.
     */
    void markCredentialsRevoked(String credentialsId) throws CredentialsServiceException;
}
