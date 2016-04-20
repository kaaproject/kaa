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

package org.kaaproject.kaa.server.transport;

/**
 * Represents a transport for communication between a Kaa server and endpoints.
 * 
 * @author Andrew Shvayka
 *
 */
public interface Transport {

    /**
     * Initialize a transport instance with a particular configuration and
     * common transport properties. The configuration is a serialized Avro
     * object. The serialization is done using the schema specified in
     * {@link KaaTransportConfig}.
     *
     * @param context
     *            the transport initialization context
     * @throws TransportLifecycleException
     */
    void init(GenericTransportContext context) throws TransportLifecycleException;

    /**
     * Retrieves the serialized connection data. This data will be used in an
     * endpoint sdk to set up a connection to this transport instance.
     * 
     * @return the serialized connection data.
     */
    TransportMetaData getConnectionInfo();

    /**
     * Starts a transport instance. This method should block its caller thread
     * until the transport is started. This method should not block its caller
     * thread after startup sequence is successfully completed.
     */
    void start();

    /**
     * Stops the transport instance. This method should block its current thread
     * until transport is stopped. Transport may be started again after it is
     * stopped.
     */
    void stop();
}
