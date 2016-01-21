/*
 * Copyright 2014 CyberVision, Inc.
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

    /** The application token. */
    private final String applicationToken;

    /** Supports only configuration resync delta encoded using base schema. */
    private final boolean resyncOnly;

    /** The configuration hash. */
    private final EndpointObjectHash configurationHash;

    /** The endpoint profile. */
    private EndpointProfileDto endpointProfile;

    /** The sequence number. */
    private final int sequenceNumber;

    /** The fetch schema. */
    private boolean fetchSchema;

    private boolean userConfigurationChanged;

    private byte[] userConfigurationHash;

    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken  the application token
     * @param sequenceNumber    the sequence number
     * @param resyncOnly        the resyncOnly
     */
    public GetDeltaRequest(String applicationToken, int sequenceNumber, boolean resyncOnly) {
        this(applicationToken, null, sequenceNumber, resyncOnly);
    }

    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken
     *            the application token
     * @param sequenceNumber
     *            the sequence number
     */
    public GetDeltaRequest(String applicationToken, int sequenceNumber) {
        this(applicationToken, null, sequenceNumber, false);
    }

    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken
     *            the application token
     * @param configurationHash
     *            the configuration hash
     * @param sequenceNumber
     *            the sequence number
     */
    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash, int sequenceNumber) {
        this(applicationToken, configurationHash, sequenceNumber, false, false);
    }

    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken  the application token
     * @param configurationHash the configuration hash
     * @param sequenceNumber    the sequence number
     * @param resyncOnly        the resyn only
     */
    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash, int sequenceNumber, boolean resyncOnly) {
        this(applicationToken, configurationHash, sequenceNumber, false, resyncOnly);
    }

    /**
     * Instantiates a new delta request.
     *
     * @param applicationToken
     *            the application token
     * @param configurationHash
     *            the configuration hash
     * @param sequenceNumber
     *            the sequence number
     * @param fetchSchema
     *            the fetch schema
     * @param resyncOnly
     *            the resync only
     */
    public GetDeltaRequest(String applicationToken, EndpointObjectHash configurationHash, int sequenceNumber, boolean fetchSchema,
            boolean resyncOnly) {
        super();
        this.applicationToken = applicationToken;
        this.configurationHash = configurationHash;
        this.sequenceNumber = sequenceNumber;
        this.fetchSchema = fetchSchema;
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
     * Gets the sequence number.
     *
     * @return the sequence number
     */
    public int getSequenceNumber() {
        return sequenceNumber;
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
     * Checks if is fetch schema.
     *
     * @return true, if is fetch schema
     */
    public boolean isFetchSchema() {
        return fetchSchema;
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
     * Sets the fetch schema.
     *
     * @param fetchSchema
     *            the new fetch schema
     */
    @Deprecated
    public void setFetchSchema(boolean fetchSchema) {
        this.fetchSchema = fetchSchema;
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

    public void setUserConfigurationChanged(boolean userConfigurationChanged) {
        this.userConfigurationChanged = userConfigurationChanged;
    }

    public boolean isUserConfigurationChanged() {
        return userConfigurationChanged;
    }

    public byte[] getUserConfigurationHash() {
        return userConfigurationHash;
    }

    public void setUserConfigurationHash(byte[] userConfigurationHash) {
        this.userConfigurationHash = userConfigurationHash;
    }
}
