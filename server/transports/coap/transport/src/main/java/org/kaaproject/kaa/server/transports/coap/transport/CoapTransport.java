package org.kaaproject.kaa.server.transports.coap.transport;


//import org.kaaproject.kaa.server.transport.;

import org.kaaproject.kaa.server.transport.AbstractKaaTransport;
import org.kaaproject.kaa.server.transport.SpecificTransportContext;
import org.kaaproject.kaa.server.transport.TransportLifecycleException;
import org.kaaproject.kaa.server.transport.coap.config.gen.AvroCoapConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CoapTransport extends AbstractKaaTransport<AvroCoapConfig> {

  private static final Logger LOG = LoggerFactory.getLogger(CoapTransport.class);

  private CoapHandler server;

  /**
   * Initialize a transport instance with a particular configuration and
   * common transport properties that are accessible via the context. The configuration is an Avro
   * object. The serializaion/deserialization is done using the schema specified in
   * {@link KaaTransportConfig}.
   * @param context the transport initialization context
   * @throws TransportLifecycleException  is socket exception.
   */
  @Override
  protected void init(SpecificTransportContext<AvroCoapConfig> context) throws TransportLifecycleException {

    AvroCoapConfig configuration = context.getConfiguration();
    configuration.setBindInterface(replaceProperty(configuration.getBindInterface(), BIND_INTERFACE_PROP_NAME, context
            .getCommonProperties().getProperty(BIND_INTERFACE_PROP_NAME, LOCALHOST)));
    configuration.setPublicInterface(replaceProperty(configuration.getPublicInterface(), PUBLIC_INTERFACE_PROP_NAME, context
            .getCommonProperties().getProperty(PUBLIC_INTERFACE_PROP_NAME, LOCALHOST)));


    //configuration.getBindInterface(), configuration.getBindPort()
    try {
      // create server
      server = new CoapHandler(configuration.getBindPort());
      // add endpoints on all IP addresses
      //server.addEndpoints();


    } catch (SocketException exception) {
      System.err.println("Failed to initialize server: " + exception.getMessage());
    }

  }

  /**
   * Starts a transport instance. This method should block its caller thread
   * until the transport is started. This method should not block its caller
   * thread after the startup sequence is successfully completed.
   */
  @Override
  public void start() {

    LOG.info(" server is going to start");
    server.start();
    LOG.info(" server is started!");

  }

  /**
   * Stops the transport instance. This method should block its current thread
   * until the transport is stopped. The transport may be started again after it is
   * stopped.
   */
  @Override
  public void stop() {


    LOG.info(" server is going to stop");
    server.stop();

    LOG.info(" server is stopped");

  }

  /**
   * Returns a min version of the transport protocol that is supported by this transport.
   * Useful when a single transport instance needs to support multiple versions of the client protocol implementations.
   */
  @Override
  protected int getMinSupportedVersion() {
    // TODO Auto-generated method stub
    return 1;
  }

  /**
   * Returns a max version of the transport protocol that is supported by this transport.
   * Useful when a single transport instance needs to support multiple versions of the client protocol implementations.
   */
  @Override
  protected int getMaxSupportedVersion() {
    // TODO Auto-generated method stub
    return 1;
  }

  @Override
  public Class<AvroCoapConfig> getConfigurationClass() {
    // TODO Auto-generated method stub
    return AvroCoapConfig.class;
  }

  @Override
  protected List<byte[]> getSerializedConnectionInfoList() {
    byte[] interfaceData = toUtf8Bytes(context.getConfiguration().getPublicInterface());
    byte[] publicKeyData = context.getServerKey().getEncoded();
    ByteBuffer buf = ByteBuffer.wrap(
            new byte[SIZE_OF_INT * 3 + interfaceData.length + publicKeyData.length]);
    buf.putInt(publicKeyData.length);
    buf.put(publicKeyData);
    buf.putInt(interfaceData.length);
    buf.put(interfaceData);
    buf.putInt(context.getConfiguration().getPublicPort());
    List<byte[]> connectionInfoList = new ArrayList<>();
    connectionInfoList.add(buf.array());

    return connectionInfoList;
  }


}