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

package org.kaaproject.kaa.server.operations.service.cache;

import java.io.Serializable;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.core.algorithms.delta.RawBinaryDelta;

/**
 * The Class DeltaCacheEntry is used to model cache entry for delta calculation.
 * Contains hash object, result configuration itself and delta.
 *
 * @author ashvayka
 */
public class ConfigurationCacheEntry implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The configuration. */
    private final byte[] configuration;

    /** The delta. */
    private final RawBinaryDelta delta;

    /** The hash. */
    private final EndpointObjectHash hash;

    /** The hash. */
    private final EndpointObjectHash userConfigurationHash;

    /**
     * Instantiates a new delta cache entry.
     *
     * @param configuration the configuration
     * @param delta the delta
     * @param hash the hash
     * @param userConfigurationHash  the user configuration hash
     */
    public ConfigurationCacheEntry(byte[] configuration, RawBinaryDelta delta, EndpointObjectHash hash, EndpointObjectHash userConfigurationHash) {
        super();
        this.configuration = configuration;
        this.delta = delta;
        this.hash = hash;
        this.userConfigurationHash = userConfigurationHash;
    }

    /**
     * Gets the configuration.
     *
     * @return the configuration
     */
    public byte[] getConfiguration() {
        return configuration;
    }

    /**
     * Gets the delta.
     *
     * @return the delta
     */
    public RawBinaryDelta getDelta() {
        return delta;
    }

    /**
     * Gets the hash.
     *
     * @return the hash
     */
    public EndpointObjectHash getHash() {
        return hash;
    }

    /**
     * Gets the hash.
     *
     * @return the hash
     */
    public EndpointObjectHash getUserConfigurationHash() {
        return userConfigurationHash;
    }
}
