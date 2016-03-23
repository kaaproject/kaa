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
import org.springframework.stereotype.Service;

/**
 * The default implementation of the {@link InternalCredentialsService}
 * interface.
 *
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Service
public class DefaultInternalCredentialsService implements InternalCredentialsService {

    @Override
    public CredentialsDto provisionCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<CredentialsDto> lookupCredentials(String applicationId, String credentialsId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException {
        // TODO Auto-generated method stub
    }

    @Override
    public void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException {
        // TODO Auto-generated method stub
    }
}
