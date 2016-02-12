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

package org.kaaproject.kaa.server.operations.pojo;

import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

/**
 * The Class for modeling of delta request. It is used to communicate with
 * {@link org.kaaproject.kaa.server.operations.service.delta.DeltaService
 * DeltaService}
 *
 * @author ashvayka
 */
public class GetDeltaRequest {

    /** The application token. */
    private final String applicationToken;

    /** Supports only configuration resync delta encoded using base schema. */
    private final boolean resyncOnly;

    /** The configuration hash. */
    private final EndpointObjectHash configurationHash;

    /** The endpoint profile. */
    private EndpointProfileDto endpointProfile;
    
    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken
     *            the application token
     */
    public GetDeltaRequest(String applicationToken) {
        this(applicationToken, null, true);
    }
    
    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken
     *            the application token
     * @param configurationHash
     *            the configuration hash
     */
    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash){
        this(applicationToken, configurationHash, true);
    }

    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken
     *            the application token
     * @param configurationHash
     *            the configuration hash
     * @param resyncOnly
     *            the resync only
     */
    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash, boolean resyncOnly) {
        super();
        this.applicationToken = applicationToken;
        this.configurationHash = configurationHash;
        this.resyncOnly = resyncOnly;
    }

    /**
     * Gets the application token.
     *
     * @return the application token
     */
    public String getApplicationToken() {
        return applicationToken;
    }

    /**
     * Gets the configuration hash.
     *
     * @return the configuration hash
     */
    public EndpointObjectHash getConfigurationHash() {
        return configurationHash;
    }

    /**
     * Gets the endpoint profile.
     *
     * @return the endpoint profile
     */
    public EndpointProfileDto getEndpointProfile() {
        return endpointProfile;
    }

    /**
     * Checks if this request represent client that is interested in
     * configuration resyncs based on base schema.
     *
     * @return true, if is fetch schema
     */
    public boolean isResyncOnly() {
        return resyncOnly;
    }

    /**
     * Sets the endpoint profile.
     *
     * @param endpointProfile
     *            the new endpoint profile
     */
    public void setEndpointProfile(EndpointProfileDto endpointProfile) {
        this.endpointProfile = endpointProfile;
    }

    /**
     * Checks if is first request.
     *
     * @return true, if is first request
     */
    public boolean isFirstRequest() {
        return getConfigurationHash() == null || getConfigurationHash().getData() == null || getConfigurationHash().getData().length == 0;
    }
}
