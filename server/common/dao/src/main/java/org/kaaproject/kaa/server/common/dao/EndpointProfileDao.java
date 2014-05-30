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

package org.kaaproject.kaa.server.common.dao;

/**
 * The interface Endpoint profile dao.
 *
 * @param <T> the type parameter
 */
public interface EndpointProfileDao<T> extends Dao<T> {

    /**
     * Find endpoint profile by key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     * @return the endpoint profile object
     */
    T findByKeyHash(byte[] endpointKeyHash);


    /**
     * Gets the count of endpoint profile by key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     * @return the count of endpoint profile
     */
    long getCountByKeyHash(byte[] endpointKeyHash);

    /**
     * Remove endpoint profile by key hash.
     *
     * @param endpointKeyHash the endpoint key hash
     */
    void removeByKeyHash(byte[] endpointKeyHash);

    /**
     * This method remove endpoint profile by application id.
     * @param appId application id
     */
    void removeByAppId(String appId);
}
