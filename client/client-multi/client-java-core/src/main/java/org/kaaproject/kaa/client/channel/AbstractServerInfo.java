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

import java.security.PublicKey;

public abstract class AbstractServerInfo implements ServerInfo {

    private final String host;
    private final int port;
    private final PublicKey publicKey;

    AbstractServerInfo(String host, int port, PublicKey publicKey) {
        this.host = host;
        this.port = port;
        this.publicKey = publicKey;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getURL() {
        return "http://" + host + ":" + port;
    }

    @Override
    public String toString() {
        return "AbstractServerInfo [host=" + host + ", port=" + port + "]";
    }
}
