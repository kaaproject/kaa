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
import org.kaaproject.kaa.common.dto.credentials.CredentialsStatus;

/**
 * This interface is used to communicate with external systems in order to manage credentials information 
 *
 * @author Bohdan Khablenko
 * @author Andrew Shvayka
 *
 * @since v0.9.0
 */
public interface CredentialsService {
    
    /**
     * Provision credentials entry.
     * @param credentials - credentials to provision
     * @throws UnsupportedOperationException - in case service implementation is read-only
     * @return provisioned credentials
     */
    CredentialsDto provisionCredentials(CredentialsDto credentials) throws CredentialsServiceException;

    /**
     * Lookup credentials entry by id.
     * @param credentialsId - credentials to provision
     * @return credentials or null if credentials are not found
     */
    CredentialsDto lookupCredentials(String credentialsId);
    
    /**
     * Update credentials status to {@link CredentialsStatus#IN_USE}
     * @param credentialsId
     * @throws CredentialsServiceException in case credentials are not in {@link CredentialsStatus#AVAILABLE}
     */
    void markCredentialsInUse(String credentialsId) throws CredentialsServiceException;
    
    /**
     * Update credentials status to {@link CredentialsStatus#REVOKED}
     * @param credentialsId
     * @throws UnsupportedOperationException - in case the service implementation does not allow revocation of credentials
     */
    void markCredentialsRevoked(String credentialsId) throws CredentialsServiceException;
    
}
