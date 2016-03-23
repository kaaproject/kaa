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

import org.kaaproject.kaa.common.dto.credentials.CredentialsDto;

/**
 * Bridge between {@link CredentialsService} and {@link InternalCredentialsService} interfaces
 * @author ashvayka
 *
 */
public class InternalCredentialsServiceAdapter implements CredentialsService {

    private final String applicationId;
    private final InternalCredentialsService service;

    public InternalCredentialsServiceAdapter(String applicationId, InternalCredentialsService service) {
        super();
        this.applicationId = applicationId;
        this.service = service;
    }

    @Override
    public CredentialsDto provisionCredentials(CredentialsDto credentials) throws CredentialsServiceException {
        return service.provisionCredentials(applicationId, credentials);
    }

    @Override
    public CredentialsDto lookupCredentials(String credentialsId) {
        return service.lookupCredentials(applicationId, credentialsId);
    }

    @Override
    public void markCredentialsInUse(String credentialsId) throws CredentialsServiceException {
        service.markCredentialsInUse(applicationId, credentialsId);
    }

    @Override
    public void markCredentialsRevoked(String credentialsId) throws CredentialsServiceException {
        service.markCredentialsRevoked(applicationId, credentialsId);
    }


}
