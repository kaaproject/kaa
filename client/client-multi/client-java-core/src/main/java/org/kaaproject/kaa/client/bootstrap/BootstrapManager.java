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

package org.kaaproject.kaa.client.bootstrap;

import java.util.List;

import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;

/**
 * Bootstrap manager controls the list of available operation servers.
 *
 * @author Yaroslav Zeygerman
 *
 */
public interface BootstrapManager {

    /**
     * Receives the latest endpoint servers' list from the bootstrap server.
     *
     */
    void receiveOperationsServerList() throws TransportException;

    /**
     * Retrieves next (in the list) endpoint's properties and applies it for Channel manager.
     *
     * @param type the channel's type (i.e. HTTP channel, HTTP long poll channel, etc.).
     *
     */
    void useNextOperationsServer(ChannelType type);

    /**
     * Retrieves endpoint's properties by its DNS name and applies it for Channel manager.
     *
     * @param name endpoint's DNS name.
     *
     */
    void useNextOperationsServerByDnsName(String name);

    /**
     * Sets bootstrap transport object.
     *
     * @param transport object which is going to be set.
     *
     */
    void setTransport(BootstrapTransport transport);

    /**
     * Sets Channel manager.
     *
     * @param manager the channel manager which is going to be set.
     *
     */
    void setChannelManager(KaaChannelManager manager);

    /**
     * Updates the operation server list.
     *
     * @param list the operation server list.
     *
     */
    void onServerListUpdated(OperationsServerList list);

    /**
     *
     * @return current list of servers.
     *
     */
    List<OperationsServer> getOperationsServerList();
}
