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

package org.kaaproject.kaa.server.transport.http.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.utils.CRC32Util;
import org.kaaproject.kaa.server.transport.KaaTransportConfig;
import org.kaaproject.kaa.server.transport.TransportConfig;
import org.kaaproject.kaa.server.transport.http.config.gen.AvroHttpConfig;

/**
 * Configuration for HTTP transport
 * 
 * @author Andrew Shvayka
 *
 */
@KaaTransportConfig
public class HttpTransportConfig implements TransportConfig {

    private static final String HTTP_TRANSPORT_NAME = "org.kaaproject.kaa.server.transport.http";
    private static final int HTTP_TRANSPORT_ID = CRC32Util.crc32(HTTP_TRANSPORT_NAME);
    private static final String HTTP_TRANSPORT_CLASS = "org.kaaproject.kaa.server.transports.http.transport.HttpTransport";
    private static final String HTTP_TRANSPORT_CONFIG = "http-transport.config";

    public HttpTransportConfig() {
        super();
    }

    @Override
    public int getId() {
        return HTTP_TRANSPORT_ID;
    }

    @Override
    public String getName() {
        return HTTP_TRANSPORT_NAME;
    }

    @Override
    public String getTransportClass() {
        return HTTP_TRANSPORT_CLASS;
    }

    @Override
    public Schema getConfigSchema() {
        return AvroHttpConfig.getClassSchema();
    }

    @Override
    public String getConfigFileName() {
        return HTTP_TRANSPORT_CONFIG;
    }
}
