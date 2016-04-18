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

package org.kaaproject.kaa.server.operations.service.transport;

import java.security.PublicKey;
import java.util.Properties;

import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;
import org.kaaproject.kaa.server.transport.AbstractTransportService;
import org.kaaproject.kaa.server.transport.TransportService;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link OperationsTransportService} that extends {@link AbstractTransportService}.
 * 
 * @author Andrew Shvayka
 *
 */
@Service
public class OperationsTransportService extends AbstractTransportService implements TransportService {

    private static final String TRANSPORT_CONFIG_PREFIX = "operations";
    
    @Autowired
    private AkkaService akkaService;
    
    @Autowired
    private KeyStoreService operationsKeyStoreService;


    @Autowired
    private Properties properties;

    public OperationsTransportService() {
        super();
    }

    @Override
    protected String getTransportConfigPrefix() {
        return TRANSPORT_CONFIG_PREFIX;
    }

    @Override
    protected MessageHandler getMessageHandler(){
        return akkaService;
    }

    @Override
    protected Properties getServiceProperties() {
        return properties;
    }

    @Override
    protected PublicKey getPublicKey() {
        return operationsKeyStoreService.getPublicKey();
    }

}
