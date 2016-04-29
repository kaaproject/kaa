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
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;

/**
 * A bridge between the actual interface and the internal Kaa implementation.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public final class CredentialsServiceAdapter implements CredentialsService {

    private final String applicationId;
    private final org.kaaproject.kaa.server.common.dao.CredentialsService credentialsService;

    /**
     * Constructs an adapter for the given application.
     *
     * @param applicationId The application ID
     * @param credentialsService The internal credentials service used by Kaa
     */
    public CredentialsServiceAdapter(String applicationId, org.kaaproject.kaa.server.common.dao.CredentialsService credentialsService) {
        this.applicationId = applicationId;
        this.credentialsService = credentialsService;
    }

    @Override
    public CredentialsDto provideCredentials(CredentialsDto credentials) throws CredentialsServiceException {
        return credentialsService.provideCredentials(applicationId, credentials);
    }

    @Override
    public Optional<CredentialsDto> lookupCredentials(String credentialsId) throws CredentialsServiceException {
        return credentialsService.lookupCredentials(applicationId, credentialsId);
    }

    @Override
    public void markCredentialsInUse(String credentialsId) throws CredentialsServiceException {
        credentialsService.markCredentialsInUse(applicationId, credentialsId);
    }

    @Override
    public void markCredentialsRevoked(String credentialsId) throws CredentialsServiceException {
        credentialsService.markCredentialsRevoked(applicationId, credentialsId);
    }
}
