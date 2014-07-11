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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;

public class DefaultBootstrapTransport extends AbstractKaaTransport implements BootstrapTransport {

    private BootstrapManager manager;
    private final String applicationToken;

    public DefaultBootstrapTransport(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    @Override
    public void sync() {
        syncByType(TransportType.BOOTSTRAP);
    }

    @Override
    public Resolve createResolveRequest() {
        if (clientState != null) {
            Resolve request = new Resolve();
            request.setApplicationToken(applicationToken);
            return request;
        }
        return null;
    }

    @Override
    public void onResolveResponse(OperationsServerList servers) {
        if (manager != null) {
            manager.onServerListUpdated(servers);
        }
    }

    @Override
    public void setBootstrapManager(BootstrapManager manager) {
        this.manager = manager;
    }

}
