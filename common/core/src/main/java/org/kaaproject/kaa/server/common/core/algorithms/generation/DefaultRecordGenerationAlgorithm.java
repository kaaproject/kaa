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

package org.kaaproject.kaa.server.common.core.algorithms.generation;

import java.io.IOException;

import org.apache.avro.generic.GenericRecord;
import org.kaaproject.kaa.server.common.core.configuration.KaaData;

/**
 * This interface is designed to contain functionality for processing
 * configuration.
 */
public interface DefaultRecordGenerationAlgorithm<T extends KaaData> {

    /**
     * Gets the root configuration.
     *
     * @return the root configuration
     * @throws ConfigurationGenerationException the configuration processing exception
     */
    GenericRecord getRootConfiguration() throws ConfigurationGenerationException;

    /**
     * Gets the root json configuration.
     *
     * @return the root json configuration
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ConfigurationGenerationException the configuration processing exception
     */
    T getRootData() throws IOException, ConfigurationGenerationException;

    /**
     * Gets the configuration by name.
     *
     * @param name the name
     * @param namespace the namespace
     * @return the configuration by name
     * @throws ConfigurationGenerationException the configuration processing exception
     */
    GenericRecord getConfigurationByName(String name, String namespace) throws ConfigurationGenerationException;
}
