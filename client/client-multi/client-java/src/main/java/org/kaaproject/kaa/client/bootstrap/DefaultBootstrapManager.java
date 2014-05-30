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

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.kaaproject.kaa.client.transport.BootstrapTransport;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link BootstrapManager} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultBootstrapManager implements BootstrapManager {
    
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultBootstrapManager.class);

    private BootstrapTransport transport;
    private String applicationToken;
    private List<OperationsServer> operationsServerList;
    private Iterator<OperationsServer> operationsServerIterator;

    public DefaultBootstrapManager(String applicationToken) {
        this.applicationToken = applicationToken;
    }

    @Override
    public void receiveOperationsServerList() throws TransportException {
        Resolve resolveRequest = new Resolve();
        resolveRequest.setApplicationToken(applicationToken);
        LOG.debug("Sending Resolve request to the server " + resolveRequest.toString());
        operationsServerList = transport.sendResolveRequest(resolveRequest).getOperationsServerArray();
        if (operationsServerList != null && !operationsServerList.isEmpty()) {
            Collections.sort(operationsServerList, new Comparator<OperationsServer>() {

                @Override
                public int compare(OperationsServer o1, OperationsServer o2) {
                    return o1.getPriority().compareTo(o2.getPriority());
                }

            });
            operationsServerIterator = operationsServerList.iterator();
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
    }

    @Override
    public OperationsServerInfo getNextOperationsServer() {
        if (operationsServerList != null && !operationsServerList.isEmpty()) {
            if (!operationsServerIterator.hasNext()) {
                operationsServerIterator = operationsServerList.iterator();
            }
            OperationsServer server = operationsServerIterator.next();
            try {
                return new OperationsServerInfo(server.getDNSName(), server.getPublicKey().array());
            } catch (NoSuchAlgorithmException e) {
                throw new BootstrapRuntimeException(e.getMessage());
            } catch (InvalidKeySpecException e) {
                throw new BootstrapRuntimeException(e.getMessage());
            }
        } else {
            throw new BootstrapRuntimeException("Operations Server list is empty");
        }
    }

    @Override
    public void setTransport(BootstrapTransport transport) {
        this.transport = transport;
    }

    @Override
    public List<OperationsServer> getOperationsServerList() {
        return operationsServerList != null ? new ArrayList<OperationsServer>(operationsServerList) : null;
    }

    @Override
    public OperationsServerInfo getOperationsServerByDnsName(String name) {
        if (name != null) {
            if (operationsServerList != null && !operationsServerList.isEmpty()) {
                for (OperationsServer server : operationsServerList) {
                    if (server.getDNSName().equals(name)) {
                        try {
                            return new OperationsServerInfo(name, server.getPublicKey().array());
                        } catch (NoSuchAlgorithmException e) {
                            throw new BootstrapRuntimeException(e.getMessage());
                        } catch (InvalidKeySpecException e) {
                            throw new BootstrapRuntimeException(e.getMessage());
                        }
                    }
                }
            } else {
                throw new BootstrapRuntimeException("Operations Server list is empty");
            }
        }
        return null;
    }
}
