package org.kaaproject.kaa.server.transport.coap.config;

import org.apache.avro.Schema;
import org.kaaproject.kaa.server.common.utils.Crc32Util;
import org.kaaproject.kaa.server.transport.KaaTransportConfig;
import org.kaaproject.kaa.server.transport.Transport;
import org.kaaproject.kaa.server.transport.TransportConfig;
import org.kaaproject.kaa.server.transport.TransportService;
import org.kaaproject.kaa.server.transport.coap.config.gen.AvroCoapConfig;


/**
 * All-args constructor.
 */
@KaaTransportConfig
public class CoapTransportConfig implements TransportConfig {

  private static final String COAP_TRANSPORT_NAME = "org.kaaproject.kaa.server.transport.coap";
  private static final int COAP_TRANSPORT_ID = Crc32Util.crc32(COAP_TRANSPORT_NAME);
  private static final String COAP_TRANSPORT_CLASS = "org.kaaproject.kaa.server.transports.coap.transport.CoapTransport";
  private static final String COAP_TRANSPORT_CONFIG = "coap-transport.config";

  public CoapTransportConfig() {
      super();
  }

  /**
   * Returns the transport id. The transport id must be unique.
   *
   * @return the transport id
   */
  @Override
  public int getId() {
    return COAP_TRANSPORT_ID;
  }

  /**
   * Returns the transport name. There is no strict rule for this
   * name to be unique.
   *
   * @return the transport name
   */
  @Override
  public String getName() {
    return COAP_TRANSPORT_NAME;
  }

  /**
   * Returns the class name of the {@link Transport} implementation.
   *
   * @return the class name of the {@link Transport} implementation
   */
  @Override
  public String getTransportClass() {
    return COAP_TRANSPORT_CLASS;
  }

  /**
   * Returns the avro schema of the {@link Transport} configuration.
   *
   * @return the avro schema of the {@link Transport} configuration
   */
  @Override
  public Schema getConfigSchema() {
    return AvroCoapConfig.getClassSchema();
  }

  /**
   * Returns the configuration file name. This configuration file may
   * be used by {@link TransportService} to initialize and configure
   * the corresponding {@link Transport}.
   *
   * @return the configuration file name
   */
  @Override
  public String getConfigFileName() {
    return COAP_TRANSPORT_CONFIG;
  }
}