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

import org.kaaproject.kaa.server.operations.pojo.sync.EndpointVersionInfo;


/**
 * The Class for modeling request of profile registration. It is used to
 * communicate with
 * {@link org.kaaproject.kaa.server.operations.service.profile.ProfileService
 * ProfileService}
 *
 * @author ashvayka
 */
public class RegisterProfileRequest {
    /** The application token. */
    private final String appToken;

    /** The endpoint key. */
    private final byte[] endpointKey;

    /** The profile. */
    private final byte[] profile;

    /** The conf schema version. */
    private final EndpointVersionInfo versionInfo;

    private final String accessToken;

    /**
     * Instantiates a new register profile request.
     *
     * @param appToken            the app token
     * @param endpointKey            the endpoint key
     * @param versionInfo the version info
     * @param profile            the profile body
     */
    public RegisterProfileRequest(String appToken, byte[] endpointKey, EndpointVersionInfo versionInfo, byte[] profile) {
        this(appToken, endpointKey, versionInfo, profile, null);
    }

    /**
     * Instantiates a new register profile request.
     *
     * @param appToken            the app token
     * @param endpointKey            the endpoint key
     * @param versionInfo the version info
     * @param profile            the profile body
     */
    public RegisterProfileRequest(String appToken, byte[] endpointKey, EndpointVersionInfo versionInfo, byte[] profile, String accessToken) {
        super();
        this.appToken = appToken;
        this.endpointKey = Arrays.copyOf(endpointKey, endpointKey.length);
        this.versionInfo = versionInfo;
        this.profile = Arrays.copyOf(profile, profile.length);
        this.accessToken = accessToken;
    }

    /**
     * Gets the endpoint key.
     *
     * @return the endpoint key
     */
    public byte[] getEndpointKey() {
        return Arrays.copyOf(endpointKey, endpointKey.length);
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
    public String getAppToken() {
        return appToken;
    }

    /**
     * Gets the version info.
     *
     * @return the version info
     */
    public EndpointVersionInfo getVersionInfo() {
        return versionInfo;
    }

    /**
     * Gets the endpoint access token.
     *
     * @return the endpoint access token
     */
    public String getAccessToken() {
        return accessToken;
    }
}
