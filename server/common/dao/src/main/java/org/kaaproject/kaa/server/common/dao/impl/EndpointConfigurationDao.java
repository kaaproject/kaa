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

import org.kaaproject.kaa.common.dto.EndpointConfigurationDto;
import org.kaaproject.kaa.server.common.dao.model.EndpointConfiguration;

import java.nio.ByteBuffer;

/**
 * The interface Endpoint configuration dao.
 *
 * @param <T> the type parameter
 */
public interface EndpointConfigurationDao<T extends EndpointConfiguration> extends Dao<T, ByteBuffer> {

    /**
     * Save endpoint configuration
     *
     * @param dto endpoint configuration
     * @return saved endpoint configuration
     */
    T save(EndpointConfigurationDto dto);

    /**
     * Find endpoint configuration by hash.
     *
     * @param hash the hash of endpoint key
     * @return the endpoint configuration
     */
    T findByHash(byte[] hash);

    /**
     * Remove endpoint configuration by hash.
     *
     * @param hash the hash of endpoint key
     */
    void removeByHash(byte[] hash);
}
