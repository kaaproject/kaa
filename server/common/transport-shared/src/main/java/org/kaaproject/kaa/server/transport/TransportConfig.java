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
package org.kaaproject.kaa.server.transport;

import org.apache.avro.Schema;

/**
 * A configuration of particular {@link Transport}.
 * 
 * @author Andrew Shvayka
 *
 */
public interface TransportConfig {

    /**
     * Returns id of the transport. Transport id must be unique.
     * 
     * @return id of the transport
     */
    int getId();

    /**
     * Returns name of the transport. There is not strict restriction for this
     * name to be unique.
     * 
     * @return name of the transport
     */
    String getName();

    /**
     * Returns class name of the {@link Transport} implementation.
     * 
     * @return class name of the {@link Transport} implementation
     */
    String getTransportClass();

    /**
     * Returns avro schema of the {@link Transport} configuration.
     * 
     * @return avro schema of the {@link Transport} configuration
     */
    Schema getConfigSchema();

    /**
     * Returns file name of the configuration file. This configuration file may
     * be used by {@link TransportService} in order to initialize and configure
     * corresponding {@link Transport}
     * 
     * @return file name of the configuration file
     */
    String getConfigFileName();

}
