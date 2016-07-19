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

import java.util.Optional;

import org.kaaproject.kaa.common.dto.credentials.EndpointRegistrationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointRegistration;

/**
 * @author Andrew Shvayka
 * @author Bohdan Khablenko
 *
 * @since v0.9.0
 */
public interface EndpointRegistrationDao<T extends EndpointRegistration> extends Dao<T, String> {

    /**
     * Saves the given endpoint registration.
     *
     * @param endpointRegistration The endpoint registration to save
     *
     * @return The endpoint registration saved
     */
    T save(EndpointRegistrationDto endpointRegistration);

    /**
     * Returns the endpooint registration by endpoint ID.
     *
     * @param endpointId The endpoint ID
     *
     * @return The endpoint registration found
     */
    Optional<T> findByEndpointId(String endpointId);

    /**
     * Returns the endpoint registration by credentials ID.
     *
     * @param credentialsId The credentials ID
     *
     * @return The endpoint registration found
     */
    Optional<T> findByCredentialsId(String credentialsId);

    /**
     * Removes the endpoint registration by endpoint ID.
     *
     * @param endpointId The endpoint ID
     */
    void removeByEndpointId(String endpointId);
}
