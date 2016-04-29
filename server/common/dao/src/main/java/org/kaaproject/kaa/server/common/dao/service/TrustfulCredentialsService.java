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

package org.kaaproject.kaa.server.common.dao.service;

import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.server.common.dao.CredentialsService;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * A dummy credentials service to be used in case of no credentials validation
 * is needed.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Service
public class TrustfulCredentialsService implements CredentialsService {

    private static final Logger LOG = LoggerFactory.getLogger(TrustfulCredentialsService.class);

    @Override
    public CredentialsDto provideCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException {
        LOG.debug("Returning credentials provided [{}]", credentials);
        return credentials;
    }

    @Override
    public Optional<CredentialsDto> lookupCredentials(String applicationId, String credentialsId) throws CredentialsServiceException {
        CredentialsDto credentials = new CredentialsDto(credentialsId, null, CredentialsStatus.AVAILABLE);
        LOG.debug("Returning dummy credentials [{}]", credentials);
        return Optional.of(credentials);
    }

    @Override
    public void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException {
        this.updateStatus(applicationId, credentialsId, CredentialsStatus.IN_USE);
    }

    @Override
    public void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException {
        this.updateStatus(applicationId, credentialsId, CredentialsStatus.REVOKED);
    }

    private void updateStatus(String applicationId, String credentialsId, CredentialsStatus status) {
        LOG.debug("Consider credentials [{}] for application [{}] to be [{}]", credentialsId, applicationId, status);
    }
}
