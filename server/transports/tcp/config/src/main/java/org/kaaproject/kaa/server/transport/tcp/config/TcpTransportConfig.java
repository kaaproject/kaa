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
package org.kaaproject.kaa.server.transport.tcp.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.transport.KaaTransportConfig;
import org.kaaproject.kaa.server.transport.TransportConfig;
import org.kaaproject.kaa.server.transport.tcp.config.gen.AvroTcpConfig;

/**
 * Configuration for TCP transport
 * @author Andrew Shvayka
 *
 */
@KaaTransportConfig
public class TcpTransportConfig implements TransportConfig {
    private static final int TCP_TRANSPORT_ID = 0x56c8ff92;
    private static final String TCP_TRANSPORT_NAME = "org.kaaproject.kaa.server.transport.tcp";
    private static final String TCP_TRANSPORT_CLASS = "org.kaaproject.kaa.server.transport.tcp.transport.TcpTransport";
    private static final String TCP_TRANSPORT_CONFIG = "tcp-transport.config";

    public TcpTransportConfig() {
        super();
    }

    @Override
    public int getId() {
        return TCP_TRANSPORT_ID;
    }

    @Override
    public String getName() {
        return TCP_TRANSPORT_NAME;
    }

    @Override
    public String getTransportClass() {
        return TCP_TRANSPORT_CLASS;
    }

    @Override
    public Schema getConfigSchema() {
        return AvroTcpConfig.getClassSchema();
    }

    @Override
    public String getConfigFileName() {
        return TCP_TRANSPORT_CONFIG;
    }

}
