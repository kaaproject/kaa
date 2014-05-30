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

import org.kaaproject.kaa.client.transport.BootstrapTransport;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;

/**
 * Interface for the client bootstrap manager.
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
     * Retrieves next (in the list) endpoint's properties.
     *
     * @return the endpoint server's info.
     *
     */
    OperationsServerInfo getNextOperationsServer();

    /**
     * Retrieves endpoint's properties by its DNS name.
     *
     * @param name endpoint's DNS name.
     * @return the endpoint server's info, or null if the server was not found in the list.
     *
     */
    OperationsServerInfo getOperationsServerByDnsName(String name);
    /**
     * Sets bootstrap transport object.
     *
     * @param transport object which is going to be set.
     *
     */
    void setTransport(BootstrapTransport transport);

    /**
     *
     * @return current list of servers.
     *
     */
    List<OperationsServer> getOperationsServerList();
}
