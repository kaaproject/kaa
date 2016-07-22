/*
 * Copyright 2014-2016 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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


    private final String applicationToken;

    /** Supports only configuration resync delta encoded using base schema. */
    private final boolean resyncOnly;


    private final EndpointObjectHash configurationHash;


    private EndpointProfileDto endpointProfile;


    public GetDeltaRequest(String applicationToken) {
        this(applicationToken, null, true);
    }


    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash){
        this(applicationToken, configurationHash, true);
    }


    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash, boolean resyncOnly) {
        super();
        this.applicationToken = applicationToken;
        this.configurationHash = configurationHash;
        this.resyncOnly = resyncOnly;
    }


    public String getApplicationToken() {
        return applicationToken;
    }


    public EndpointObjectHash getConfigurationHash() {
        return configurationHash;
    }


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


    public void setEndpointProfile(EndpointProfileDto endpointProfile) {
        this.endpointProfile = endpointProfile;
    }


    public boolean isFirstRequest() {
        return getConfigurationHash() == null || getConfigurationHash().getData() == null || getConfigurationHash().getData().length == 0;
    }
}
