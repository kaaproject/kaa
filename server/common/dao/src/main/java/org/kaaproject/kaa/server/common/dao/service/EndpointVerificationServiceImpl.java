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

import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointCredentialsDto;
import org.kaaproject.kaa.server.common.dao.EndpointVerificationService;
import org.kaaproject.kaa.server.common.dao.exception.IncorrectParameterException;
import org.kaaproject.kaa.server.common.dao.impl.DaoUtil;
import org.kaaproject.kaa.server.common.dao.impl.EndpointCredentialsDao;
import org.kaaproject.kaa.server.common.dao.model.EndpointCredentials;
import org.springframework.stereotype.Service;

/**
 * The default implementation of the
 * {@link org.kaaproject.kaa.server.common.dao.EndpointVerificationService}
 * interface.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Service
public class EndpointVerificationServiceImpl implements EndpointVerificationService {

    private EndpointCredentialsDao<EndpointCredentials> endpointCredentialsDao;

    public void setEndpointCredentialsDao(EndpointCredentialsDao<EndpointCredentials> endpointCredentialsDao) {
        this.endpointCredentialsDao = endpointCredentialsDao;
    }

    @Override
    public List<EndpointCredentialsDto> getEndpointCredentialsByApplicationId(String applicationId) {
        return DaoUtil.convertDtoList(this.endpointCredentialsDao.findByApplicationId(applicationId));
    }

    @Override
    public EndpointCredentialsDto getEndpointCredentialsByEndpointKeyHash(byte[] endpointKeyHash) {
        Validator.validateHash(endpointKeyHash, "The endpoint public key hash provided is invalid!");
        EndpointCredentials endpointCredentials = this.endpointCredentialsDao.findByEndpointKeyHash(endpointKeyHash);
        return DaoUtil.getDto(endpointCredentials);
    }

    @Override
    public EndpointCredentialsDto saveEndpointCredentials(EndpointCredentialsDto endpointCredentials) {
        EndpointCredentialsDto result = null;
        if (Validator.isValidObject(endpointCredentials)) {
            EndpointCredentials databaseRecord = this.endpointCredentialsDao.findByEndpointKeyHash(endpointCredentials.getEndpointKeyHash());
            if (databaseRecord == null || databaseRecord.getId().equals(endpointCredentials.getId())) {
                result = DaoUtil.getDto(this.endpointCredentialsDao.save(endpointCredentials));
            } else {
                throw new IncorrectParameterException("The endpoint credentials already exist!");
            }
        }
        return result;
    }

    @Override
    public void removeEndpointCredentialsByEndpointKeyHash(byte[] endpointKeyHash) {
        this.endpointCredentialsDao.removeByEndpointKeyHash(endpointKeyHash);
    }
}
