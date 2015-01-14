package org.kaaproject.kaa.server.transport.http.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.transport.KaaTransportConfig;
import org.kaaproject.kaa.server.transport.TransportConfig;
import org.kaaproject.kaa.server.transport.http.config.gen.AvroHttpConfig;

/**
 * Configuration for HTTP transport
 * @author Andrew Shvayka
 *
 */
@KaaTransportConfig
public class HttpTransportConfig implements TransportConfig {

    private static final int HTTP_TRANSPORT_ID = 0xfb9a3cf0;
    private static final String HTTP_TRANSPORT_NAME = "org.kaaproject.kaa.server.transport.http";
    private static final String HTTP_TRANSPORT_CLASS = "org.kaaproject.kaa.server.transport.http.transport.HttpTransport";
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
