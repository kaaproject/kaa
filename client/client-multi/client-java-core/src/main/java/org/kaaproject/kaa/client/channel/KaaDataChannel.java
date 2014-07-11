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

import java.util.Map;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;

/**
 * Channel is responsible for sending/receiving data to/from the endpoint server.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface KaaDataChannel {

    /**
     * Tells the channel to update the state of the specific service.
     *
     * @param type the transport type of the service.
     * @see TransportType
     */
    void sync(TransportType type);

    /**
     * Tells the channel to update the state of all supported services.
     *
     */
    void syncAll();

    /**
     * Retrieves the channel's id.
     * Id should be unique in scope of existing channels.
     *
     * @return the channel's id.
     *
     */
    String getId();

    /**
     * Retrieves the channel's type (i.e. HTTP, TCP, etc.).
     *
     * @return the channel's type.
     * @see ChannelType
     */
    ChannelType getType();

    /**
     * Sets the response demultiplexer for this channel.
     *
     * @param demultiplexer demultiplexer instance which is going to be set.
     * @see KaaDataDemultiplexer
     */
    void setDemultiplexer(KaaDataDemultiplexer demultiplexer);

    /**
     * Sets the request multiplexer for this channel.
     *
     * @param multiplexer multiplexer instance which is going to be set.
     * @see KaaDataMultiplexer
     */
    void setMultiplexer(KaaDataMultiplexer multiplexer);

    /**
     * Sets the server's parameters for the current channel.
     *
     * @param server server's parameters.
     * @see ServerInfo
     */
    void setServer(ServerInfo server);

    /**
     * Retrieves the map of transport types and their directions that are supported by this channel.
     *
     * @return the map of transport types.
     * @see TransportType
     */
    Map<TransportType, ChannelDirection> getSupportedTransportTypes();
}
