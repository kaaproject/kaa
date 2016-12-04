package org.kaaproject.kaa.server.transports.coap.transport;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 10/22/16
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class CoapHandler extends CoapServer {

  private static int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
  private static final Logger LOG = LoggerFactory.getLogger(CoapHandler.class);

  /**
   * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
   */
  private void addEndpoints() {
    for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
      // only binds to IPv4 addresses and localhost
      if (addr instanceof Inet4Address || addr.isLoopbackAddress()) {
        InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
        addEndpoint(new CoapEndpoint(bindToAddress));
      }
    }
  }

  /**
   * Handler add endpoints and resources.
   * @param port  listen port of CoAP server
   * @throws SocketException  socket exception
   */
  public CoapHandler(int port) throws SocketException {
    // provide an instance of a Hello-World resource
    add(new HelloWorldResource());
    COAP_PORT = port;
    addEndpoints();
  }

  /*
   * Definition of the Hello-World Resource
   */
  class HelloWorldResource extends CoapResource {

    public HelloWorldResource() {

      // set resource identifier
      super("kaaCoAP");

      // set display name
      getAttributes().setTitle("Kaa-Coap Resource");
    }


    @Override
    public void handleGET(CoapExchange exchange) {
      //sample respond to the request is like this:

      // exchange.respond("Hello World! This is a response from Kaa Coap Server!");

    }


    @Override
    public void handlePOST(CoapExchange exchange) {

      //sample respond to the request is like this:

      // exchange.respond("Hello World! This is a response from Kaa Coap Server!");


    }
  }

}
