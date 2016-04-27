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

package org.kaaproject.kaa.server.control.service.loadmgmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Panasenko
 *
 */
public class BootstrapThriftServiceImpl implements BootstrapThriftService.Iface {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapThriftServiceImpl.class);

    private Map<String, ThriftOperationsServer> opServerMap;

    private final Object sync;

    public BootstrapThriftServiceImpl() {
        opServerMap = null;
        sync = new Object();
    }

    public Map<String, ThriftOperationsServer> getOperatonsServerMap() {
        synchronized (sync) {
            if (opServerMap == null) {
                try {
                    sync.wait(60000);
                } catch (InterruptedException e) {

                }
            }
        }

        return opServerMap;
    }

    public void reset() {
        synchronized (sync) {
            opServerMap = null;
            sync.notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService
     * .Iface#onOperationsServerListUpdate(java.util.List)
     */
    @Override
    public void onOperationsServerListUpdate(List<ThriftOperationsServer> operationsServersList) throws TException {
        synchronized (sync) {
            if (operationsServersList.size() > 0 && opServerMap == null) {
                opServerMap = new HashMap<String, ThriftOperationsServer>();

                for (ThriftOperationsServer thriftServer : operationsServersList) {
                    LOG.info("onOperationsServerListUpdate: ThriftOperationsServer {} ", thriftServer.toString());
                    opServerMap.put(thriftServer.getId(), thriftServer);
                }

                sync.notify();
            }
        }
    }
}
