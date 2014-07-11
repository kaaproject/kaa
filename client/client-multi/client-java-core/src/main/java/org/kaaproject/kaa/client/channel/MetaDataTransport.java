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

package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

/**
 * Transport for general client's state.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface MetaDataTransport {

    /**
     * Creates a new Meta data request.
     *
     * @return new Meta data  request.
     *
     */
    SyncRequestMetaData createMetaDataRequest();

    /**
     * Sets the given client's properties to the current transport.
     *
     * @param properties the client's properties which are going to be set.
     *
     */
    void setClientProperties(KaaClientProperties properties);

    /**
     * Sets the given client's state to the current transport.
     *
     * @param state the client's state which is going to be set.
     *
     */
    void setClientState(KaaClientState state);

    /**
     * Sets the given public key hash to the current transport.
     *
     * @param hash the public key hash which is going to be set.
     *
     */
    void setEndpointPublicKeyhash(EndpointObjectHash hash);

    /**
     * Sets the given response timeout to the current transport.
     *
     * @param timeout the response timeout which is going to be set.
     *
     */
    void setTimeout(long timeout);

}
