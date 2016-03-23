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
 * Internal sibling of {@link CredentialsService} that works in scope of application id 
 * @author Andrew Shvayka
 *
 */
public interface InternalCredentialsService {
    
    /**
     * Provision credentials entry.
     * 
     * @see CredentialsService#provisionCredentials(CredentialsDto)
     * 
     * @param applicationId - application id
     * @param credentials - credentials to provision
     * @throws UnsupportedOperationException - in case service implementation is read-only
     * @return provisioned credentials
     */
    CredentialsDto provisionCredentials(String applicationId, CredentialsDto credentials) throws CredentialsServiceException;

    /**
     * Lookup credentials entry by id.
     * 
     * @see CredentialsService#lookupCredentials(String)
     * 
     * @param applicationId - application id
     * @param credentialsId - credentials to provision
     * @return credentials or null if credentials are not found
     */
    CredentialsDto lookupCredentials(String applicationId, String credentialsId);
    
    /** 
     * Mark credentials in use
     * 
     * @see CredentialsService#markCredentialsInUse(String)
     * 
     * @param applicationId - application id
     * @param credentialsId - credentials id
     */
    void markCredentialsInUse(String applicationId, String credentialsId) throws CredentialsServiceException;
    
    /** 
     * Mark credentials revoked
     * 
     * @see CredentialsService#markCredentialsRevoked(String)
     * 
     * @param applicationId - application id
     * @param credentialsId - credentials id
     */
    void markCredentialsRevoked(String applicationId, String credentialsId) throws CredentialsServiceException;
}
