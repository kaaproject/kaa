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

package org.kaaproject.kaa.server.bootstrap.service.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.kaaproject.kaa.server.common.thrift.util.ThriftExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BootstrapThriftServiceImpl Class.
 * CLI command implementation.
 * Operations Servers List update method
 *
 * @author Andrey Panasenko
 */
@Service
public class BootstrapThriftServiceImpl extends BaseCliThriftService implements
    BootstrapThriftService.Iface {

    @Autowired
    BootstrapInitializationService bootstrapInitializationService;

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapThriftServiceImpl.class);

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService#getServerShortName()
     */
    @Override
    protected String getServerShortName() {
        return "bootstrap";
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService#initServiceCommands()
     */
    @Override
    protected void initServiceCommands() {
    }

    /**
     * Thrift method to receive new list of Operations servers
     * @param operationsServersList List<ThriftOperationsServer>
     */
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService.Iface#onOperationsServerListUpdate(java.util.List)
     */
    @Override
    public void onOperationsServerListUpdate(List<ThriftOperationsServer> operationsServersList) throws TException {
        LOG.info("Operations server list update recived: now {} servers online:", operationsServersList.size());
        //TODO: add usage of ops list priority in future releases.
    }

    @Override
    public void shutdown() throws TException {
        LOG.info("Received shutdown command.");

        Runnable shutdownCommmand = new Runnable() {
            @Override
            public void run() {
                LOG.info("Stopping Bootstrap Server Application...");
                bootstrapInitializationService.stop();
                ThriftExecutor.shutdown();
                LOG.info("Stopped Bootstrap Server Application...");
            }
        };

        Thread shutdownThread = new Thread(shutdownCommmand);
        shutdownThread.setName("Bootstrap Server Shutdown Thread");
        shutdownThread.start();
    }
}
