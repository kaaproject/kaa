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

package org.kaaproject.kaa.client.connectivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.kaaproject.kaa.client.channel.AbstractServerInfo;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.connectivity.PingServerStorage;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingConnectivityChecker implements ConnectivityChecker {
    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultBootstrapChannel.class);

    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private final PingServerStorage serverStorage;

    public PingConnectivityChecker(PingServerStorage serverStorage) {
        this.serverStorage = serverStorage;
    }

    @Override
    public boolean checkConnectivity() {
        AbstractServerInfo si = serverStorage.getCurrentPingServer();
        LOG.info("Connectivity check will be performed on {}:{}", si.getHost(), si.getPort());
        InetSocketAddress addr = new InetSocketAddress(si.getHost(), si.getPort());

        try (Socket sock = new Socket()) {
            sock.connect(addr, CONNECTION_TIMEOUT_MS);
            LOG.info("Connection to the network exists");
            return true;
        } catch (IOException e) {}

        return false;
    }
}
