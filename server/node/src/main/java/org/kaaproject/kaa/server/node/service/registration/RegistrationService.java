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

import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationInfoDto;

/**
 * This service represent the Kaa platform capabilities to provision, lookup and
 * revoke registration information. The information that is managed using this
 * service should be stored in an NoSQL database. Registration information is
 * represented with instances of the {@link EndpointRegistrationInfoDto} class
 * and contains server-side endpoint profile version and body.
 * 
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public interface RegistrationService {

    EndpointRegistrationInfoDto provisonEndpointRegistrationInfo(EndpointRegistrationInfoDto endpointRegistrationInfoDto)
            throws RegistrationServiceException;

    EndpointRegistrationInfoDto lookupEndpointRegistrationInfoByEndpointId(String endpointId) throws RegistrationServiceException;

    EndpointRegistrationInfoDto lookupEndpointRegistrationInfoByCredentialsId(String credentialsId) throws RegistrationServiceException;

    EndpointRegistrationInfoDto revokeEndpointRegistrationInfo(String endpointId) throws RegistrationServiceException;
}
