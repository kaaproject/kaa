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

import java.util.Arrays;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.sync.EndpointVersionInfo;


/**
 * The Class for modeling request of profile update. It is used to communicate
 * with
 * {@link org.kaaproject.kaa.server.operations.service.profile.ProfileService
 * ProfileService}
 *
 * @author ashvayka
 */
public class UpdateProfileRequest {

    /** The endpoint key hash. */
    private final EndpointObjectHash endpointKeyHash;

    /** The profile. */
    private final byte[] profile;

    /** The app token. */
    private final String appToken;

    /** The version info. */
    private final EndpointVersionInfo versionInfo;

    private final String accessToken;

    /**
     * Instantiates a new update profile request.
     *
     * @param appToken
     *            the app token
     * @param endpointKeyHash
     *            the endpoint key hash
     * @param profile
     *            the profile
     * @param versionInfo
     *            the version info
     */
    public UpdateProfileRequest(String appToken, EndpointObjectHash endpointKeyHash, String accessToken, byte[] profile, EndpointVersionInfo versionInfo) {
        super();
        this.appToken = appToken;
        this.endpointKeyHash = endpointKeyHash;
        this.accessToken = accessToken;
        this.profile = Arrays.copyOf(profile, profile.length);
        this.versionInfo = versionInfo;
    }

    /**
     * Gets the endpoint key hash.
     *
     * @return the endpoint key hash
     */
    public EndpointObjectHash getEndpointKeyHash() {
        return endpointKeyHash;
    }

    /**
     * Gets the profile.
     *
     * @return the profile
     */
    public byte[] getProfile() {
        return Arrays.copyOf(profile, profile.length);
    }

    /**
     * Gets the application token.
     *
     * @return the application token
     */
    public String getApplicationToken() {
        return appToken;
    }

    /**
     * Gets the access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Gets the version info.
     *
     * @return the version info
     */
    public EndpointVersionInfo getVersionInfo() {
        return versionInfo;
    }
}
