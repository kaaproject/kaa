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

import java.text.MessageFormat;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.kaaproject.kaa.server.common.dao.CredentialsService;
import org.kaaproject.kaa.server.common.dao.exception.CredentialsServiceException;
import org.kaaproject.kaa.server.common.dao.impl.CredentialsDao;
import org.kaaproject.kaa.server.common.dao.model.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

/**
 * The default implementation of the {@link CredentialsService} interface.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Service
@Transactional
public class InternalCredentialsService implements CredentialsService {

    private static final Logger LOG = LoggerFactory.getLogger(InternalCredentialsService.class);

    private CredentialsDao<Credentials> credentialsDao;

    public void setCredentialsDao(CredentialsDao<Credentials> credentialsDao) {
        this.credentialsDao = credentialsDao;
    }

    @Override
    public CredentialsDto provideCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException {
        Validate.notBlank(applicationId, "Invalid application ID provided!");
        Validate.notNull(credentials, "Invalid credentials provided!");
        try {
            byte[] credentialsBody = credentials.getCredentialsBody();
            credentials.setId(Base64Utils.encodeToString(SHA1HashUtils.hashToBytes(credentialsBody)));
            return this.credentialsDao.save(applicationId, credentials).toDto();
        } catch (Exception cause) {
            String message = MessageFormat.format("[{0}] An unexpected exception occured while saving credentials!", applicationId);
            LOG.error(message, cause);
            throw new CredentialsServiceException(cause);
        }
    }

    @Override
    public Optional<CredentialsDto> lookupCredentials(String applicationId, String credentialsId) throws CredentialsServiceException {
        Validate.notBlank(applicationId, "Invalid application ID provided!");
        Validate.notBlank(credentialsId, "Invalid credentials ID provided!");
        try {
            return this.credentialsDao.find(applicationId, credentialsId).map(Credentials::toDto);
        } catch (Exception cause) {
            String message = MessageFormat.format("[{0}] An unexpected exception occured while searching for credentials [{1}]", applicationId, credentialsId);
            LOG.error(message, cause);
            throw new CredentialsServiceException(cause);
        }
    }

    @Override
    public void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException {
        this.updateStatus(applicationId, credentialsId, CredentialsStatus.IN_USE);
    }

    @Override
    public void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException {
        this.updateStatus(applicationId, credentialsId, CredentialsStatus.REVOKED);
    }

    private void updateStatus(String applicationId, String credentialsId, CredentialsStatus status) throws CredentialsServiceException {
        Validate.notBlank(applicationId, "Invalid application ID provided!");
        Validate.notBlank(credentialsId, "Invalid credentials ID provided!");
        try {
            this.credentialsDao.updateStatus(applicationId, credentialsId, status);
        } catch (Exception cause) {
            String message = MessageFormat.format("[{0}] An unexpected exception occured while updating credentials [{1}]", applicationId, credentialsId);
            LOG.error(message, cause);
            throw new CredentialsServiceException(cause);
        }
    }
}
