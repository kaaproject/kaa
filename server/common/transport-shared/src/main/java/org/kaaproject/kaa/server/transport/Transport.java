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


/**
 * A communication transport between Kaa server and endpoint.
 * 
 * @author Andrew Shvayka
 *
 */
public interface Transport {

    /**
     * Initialize transport instance with particular configuration and common
     * transport properties. Configuration is serialized Avro object.
     * Serialization is done using schema specified in
     * {@link KaaTransportConfig}.
     *
     * @param context
     *            transport initialization context
     * @throws TransportLifecycleException
     */
    void init(GenericTransportContext context) throws TransportLifecycleException;

    /**
     * retrieves serialized connection info data. This data will be used in
     * endpoint sdk in order to setup connection to this transport instance
     * 
     * @return serialized data that contains connection info.
     */
    TransportMetaData getConnectionInfo();

    /**
     * Starts transport instance. This method should block until transport is
     * started. This method should not block it's caller thread after startup
     * sequence successfully completed.
     */
    void start();

    /**
     * Stops transport instance. This method should block until transport is
     * stopped. Service may be started again after it is stopped.
     */
    void stop();
}
