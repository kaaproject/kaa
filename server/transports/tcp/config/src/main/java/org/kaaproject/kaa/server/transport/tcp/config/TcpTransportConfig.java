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
