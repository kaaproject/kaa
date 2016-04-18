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

package org.kaaproject.kaa.server.node.service.thrift;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.cli.server.BaseCliThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.node.KaaNodeThriftService;
import org.kaaproject.kaa.server.node.service.initialization.InitializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class KaaNodeThriftServiceImpl.<br>
 * Implementation of Kaa Node Thrift Interface.
 */
@Service
public class KaaNodeThriftServiceImpl extends BaseCliThriftService implements KaaNodeThriftService.Iface {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(KaaNodeThriftServiceImpl.class);
    
    /** The kaa node initialization service. */
    @Autowired
    private InitializationService kaaNodeInitializationService;

    @Override
    protected String getServerShortName() {
        return "kaa-node";
    }

    @Override
    protected void initServiceCommands() {
        // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kaaproject.kaa.server.common.thrift.gen.cli.CliThriftService
     * .Iface#shutdown()
     */
    @Override
    public void shutdown() throws TException {
        LOG.info("Received shutdown command.");

        Runnable shutdownCommmand = new Runnable() {
            @Override
            public void run() {
                LOG.info("Stopping Kaa Node Server Application...");
                kaaNodeInitializationService.stop();
            }
        };

        Thread shutdownThread = new Thread(shutdownCommmand);
        shutdownThread.setName("Kaa Node Server Shutdown Thread");
        shutdownThread.start();
    }
}
