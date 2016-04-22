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

package org.kaaproject.kaa.server.common.dao.impl;

import java.nio.ByteBuffer;
import java.util.List;

import org.kaaproject.kaa.common.dto.EndpointProfileBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesBodyDto;
import org.kaaproject.kaa.common.dto.EndpointProfilesPageDto;
import org.kaaproject.kaa.common.dto.PageLinkDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointProfile;

/**
 * The interface Endpoint profile dao.
 *
 * @param <T> the type parameter
 */
public interface EndpointProfileDao<T extends EndpointProfile> extends Dao<T, ByteBuffer> {

    /**
     * Saves the given endpoint profiles.
     *
     * @param dto The endpoint profile to save
     *
     * @return The endpoint profile saved
     */
    T save(EndpointProfileDto dto);

    /**
     * Find endpoint profile by key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     * @return the endpoint profile object
     */
    T findByKeyHash(byte[] endpointKeyHash);

    /**
     * Find endpoint profile by endpoint group id.
     *
     * @param pageLink the page link dto
     * @return the endpoint profiles page dto
     */
    EndpointProfilesPageDto findByEndpointGroupId(PageLinkDto pageLink);

    /**
     * Find endpoint profile body by endpoint group id.
     *
     * @param pageLink the page link dto
     * @return the endpoint profiles page dto
     */
    EndpointProfilesBodyDto findBodyByEndpointGroupId(PageLinkDto pageLink);

    /**
     * Find endpoint profile body by endpoint key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     * @return the endpoint profile body dto
     */
    EndpointProfileBodyDto findBodyByKeyHash(byte[] endpointKeyHash);

    /**
     * Gets the count of endpoint profile by key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     * @return the count of endpoint profile
     */
    T findEndpointIdByKeyHash(byte[] endpointKeyHash);

    /**
     * Remove endpoint profile by key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     */
    void removeByKeyHash(byte[] endpointKeyHash);

    /**
     * This method remove endpoint profile by application id.
     *
     * @param appId application id
     */
    void removeByAppId(String appId);

    /**
     * Find endpoint profile by endpointAccessToken.
     *
     * @param endpointAccessToken the endpoint access token
     * @return the endpoint profile object
     */
    T findByAccessToken(String endpointAccessToken);


    /**
     * Find endpoint profiles by endpoint user id.
     *
     * @param endpointUserId the endpoint user id
     * @return the list of endpoint profiles
     */
    List<T> findByEndpointUserId(String endpointUserId);

    /**
     * Checks whether there are any endpoint profiles with the given SDK token.
     *
     * @param   sdkToken An SDK token
     *
     * @return  <code>true</code> if there is at least one endpoint profile with
     *          the given SDK token, <code>false</code> otherwise
     */
    boolean checkSdkToken(String sdkToken);

    /**
     * Update endpoint profile with given keyHash, server profile schema version and given server profile.
     *
     * @param keyHash       the endpoint profile key hash.
     * @param version      the given server profile schema version.
     * @param serverProfile the given server profile data.
     * @return the updated endpoint profile with.
     */
    T updateServerProfile(byte[] keyHash, int version, String serverProfile);
}
