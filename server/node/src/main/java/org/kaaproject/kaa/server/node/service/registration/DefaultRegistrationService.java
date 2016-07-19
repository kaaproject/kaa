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

package org.kaaproject.kaa.server.node.service.registration;

import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.EndpointRegistrationService;
import org.kaaproject.kaa.server.common.dao.exception.EndpointRegistrationServiceException;
import org.springframework.stereotype.Service;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
@Service
public final class DefaultRegistrationService implements RegistrationService {

    private EndpointRegistrationService endpointRegistrationService;

    public void setEndpointRegistrationService(EndpointRegistrationService endpointRegistrationService) {
        this.endpointRegistrationService = endpointRegistrationService;
    }

    @Override
    public EndpointRegistrationDto saveEndpointRegistration(EndpointRegistrationDto endpointRegistration) throws EndpointRegistrationServiceException {
        return this.endpointRegistrationService.saveEndpointRegistration(endpointRegistration);
    }

    @Override
    public Optional<EndpointRegistrationDto> findEndpointRegistrationByEndpointId(String endpointId) throws EndpointRegistrationServiceException {
        return this.endpointRegistrationService.findEndpointRegistrationByEndpointId(endpointId);
    }

    @Override
    public Optional<EndpointRegistrationDto> findEndpointRegistrationByCredentialsId(String credentialsId) throws EndpointRegistrationServiceException {
        return this.endpointRegistrationService.findEndpointRegistrationByCredentialsId(credentialsId);
    }

    @Override
    public void removeEndpointRegistrationByEndpointId(String endpointId) throws EndpointRegistrationServiceException {
        this.endpointRegistrationService.removeEndpointRegistrationByEndpointId(endpointId);
    }
}
