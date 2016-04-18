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

package org.kaaproject.kaa.server.bootstrap.service.thrift;

import java.util.List;

import org.apache.thrift.TException;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService;
import org.kaaproject.kaa.server.common.thrift.gen.bootstrap.ThriftOperationsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * BootstrapThriftServiceImpl Class.
 * CLI command implementation.
 * Operations Servers List update method
 *
 * @author Andrey Panasenko
 */
@Service
public class BootstrapThriftServiceImpl implements
    BootstrapThriftService.Iface {

    private static final Logger LOG = LoggerFactory.getLogger(BootstrapThriftServiceImpl.class);

    /**
     * Thrift method to receive new list of Operations servers
     * @param operationsServersList the list of type ThriftOperationsServer
     */
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.thrift.gen.bootstrap.BootstrapThriftService.Iface#onOperationsServerListUpdate(java.util.List)
     */
    @Override
    public void onOperationsServerListUpdate(List<ThriftOperationsServer> operationsServersList) throws TException {
        LOG.info("Operations server list update recived: now {} servers online:", operationsServersList.size());
        //TODO: add usage of ops list priority in future releases.
    }

}
