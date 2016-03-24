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

/**
 * A bridge between the {@link CredentialsService} and
 * {@link InternalCredentialsService} interfaces.
 *
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public final class InternalCredentialsServiceAdapter implements CredentialsService {

    private final String applicationId;
    private final InternalCredentialsService credentialsService;

    /**
     * Constructs an adapter for the given application.
     *
     * @param applicationId The application ID
     * @param credentialsService The internal credentials service used by Kaa
     */
    public InternalCredentialsServiceAdapter(String applicationId, InternalCredentialsService credentialsService) {
        this.applicationId = applicationId;
        this.credentialsService = credentialsService;
    }

    @Override
    public CredentialsDto provisionCredentials(CredentialsDto credentials) throws CredentialsServiceException {
        return credentialsService.provisionCredentials(applicationId, credentials);
    }

    @Override
    public Optional<CredentialsDto> lookupCredentials(String credentialsId) {
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
