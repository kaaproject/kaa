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

package org.kaaproject.kaa.client.connectivity;

import java.io.IOException;
import java.net.InetAddress;
import java.text.MessageFormat;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingConnectivityChecker implements ConnectivityChecker {
    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultBootstrapChannel.class);

    private static final int CONNECTION_TIMEOUT_MS = 3000;
    private static final String DEFAULT_HOST = "www.google.com";

    private final String host;

    public PingConnectivityChecker() {
        this(DEFAULT_HOST);
    }
    
    public PingConnectivityChecker(String host) {
        this.host = host;
    }

    @Override
    public boolean checkConnectivity() {
        try {
            return InetAddress.getByName(host).isReachable(CONNECTION_TIMEOUT_MS);
        } catch (IOException e) {
            LOG.warn(MessageFormat.format("Host {0} is unreachable", host), e);
            return false;
        }
    }
}
