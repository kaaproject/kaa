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

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.model.Credentials;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @param <T> A specific security credentials type
 */
public interface CredentialsDao<T extends Credentials> extends Dao<T, ByteBuffer> {

    /**
     * Saves the given security credentials for the given application.
     *
     * @param applicationId The application ID
     * @param credentials The security credentials to save
     *
     * @return The security credentials saved
     */
    T save(String applicationId, CredentialsDto credentials);

    /**
     * Returns the security credentials with the given ID.
     *
     * @param applicationId The application ID to search credentials for
     * @param credentialsId The security credentials ID
     *
     * @return The security credentials with the given ID
     */
    Optional<T> find(String applicationId, String credentialsId);

    /**
     * Updates the status of the security credentials with the given ID.
     *
     * @param applicationId The application ID to update credentials for
     * @param credentialsId The security credentials ID
     * @param status The credentials status to set
     *
     * @return The security credentials with the status updated
     */
    Optional<T> updateStatus(String applicationId, String credentialsId, CredentialsStatus status);

    /**
     * Removes the security credentials with the given ID from the application
     * specified.
     *
     * @param applicationId The application ID to remove credentials from
     * @param credentialsId The secuity credentials ID
     */
    void remove(String applicationId, String credentialsId);
}
