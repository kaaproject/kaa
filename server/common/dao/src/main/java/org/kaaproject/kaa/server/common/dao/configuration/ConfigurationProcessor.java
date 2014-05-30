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

package org.kaaproject.kaa.server.common.dao.configuration;

import java.io.IOException;

import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericRecord;

/**
 * This interface is designed to contain functionality for processing
 * configuration.
 */
public interface ConfigurationProcessor {

    /**
     * Gets the root configuration.
     *
     * @return the root configuration
     * @throws ConfigurationProcessingException the configuration processing exception
     */
    GenericRecord getRootConfiguration() throws ConfigurationProcessingException;

    /**
     * Gets the root json configuration.
     *
     * @return the root json configuration
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ConfigurationProcessingException the configuration processing exception
     */
    String getRootJsonConfiguration() throws IOException, ConfigurationProcessingException;

    /**
     * Gets the root json bytes configuration.
     *
     * @return the root json bytes configuration
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ConfigurationProcessingException the configuration processing exception
     */
    byte[] getRootJsonBytesConfiguration() throws IOException, ConfigurationProcessingException;

    /**
     * Gets the root binary configuration.
     *
     * @return the root binary configuration
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ConfigurationProcessingException the configuration processing exception
     */
    byte [] getRootBinaryConfiguration() throws IOException, ConfigurationProcessingException;

    /**
     * Generate uuid fields.
     *
     * @param configurationBody the configuration body
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    byte[] generateUuidFields(byte[] configurationBody) throws IOException;

    /**
     * Generate uuid fields.
     *
     * @param container the container
     * @return the byte[]
     * @throws IOException Signals that an I/O exception has occurred.
     */
    byte[] generateUuidFields(GenericContainer container) throws IOException;

    /**
     * Gets the configuration by name.
     *
     * @param name the name
     * @param namespace the namespace
     * @return the configuration by name
     * @throws ConfigurationProcessingException the configuration processing exception
     */
    GenericRecord getConfigurationByName(String name, String namespace) throws ConfigurationProcessingException;
}
