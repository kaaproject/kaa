package org.kaaproject.kaa.client.channel.impl.channels;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.IpTransportInfo;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.TransportProtocolIdConstants;

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: root
 * Date: 10/24/16
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class CoapChannel implements KaaDataChannel {

  public static final Logger LOG = LoggerFactory // NOSONAR
          .getLogger(CoapChannel.class);

  private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();

  static {
    SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
  }

  private static final String CHANNEL_ID = "default_coap_channel";
  private KaaDataDemultiplexer demultiplexer;
  private KaaDataMultiplexer multiplexer;
  //    private volatile State channelState = State.CLOSED;
  private IpTransportInfo currentServer;

  @Override
  public void sync(TransportType type) {
    sync(Collections.singleton(type));
  }

  @Override
  public void sync(Set<TransportType> types) {

    //        if (channelState == State.SHUTDOWN) {
    //            LOG.info("Can't sync. Channel [{}] is down", getId());
    //            return;
    //        }
    //        if (channelState == State.PAUSE) {
    //            LOG.info("Can't sync. Channel [{}] is paused", getId());
    //            return;
    //        }
    //        if (channelState != State.OPENED) {
    //            LOG.info("Can't sync. Channel [{}] is waiting for CONNACK message + KAASYNC message", getId());
    //            return;
    //        }
    if (multiplexer == null) {
      LOG.warn("Can't sync. Channel {} multiplexer is not set", getId());
      return;
    }
    if (demultiplexer == null) {
      LOG.warn("Can't sync. Channel {} demultiplexer is not set", getId());
      return;
    }
    //        if (currentServer == null || socket == null) {
    //            LOG.warn("Can't sync. Server is {}, socket is \"{}\"", currentServer, socket);
    //            return;
    //        }

    Map<TransportType, ChannelDirection> typeMap = new HashMap<>(getSupportedTransportTypes().size());
    for (TransportType type : types) {
      LOG.info("Processing sync {} for channel [{}]", type, getId());
      ChannelDirection direction = getSupportedTransportTypes().get(type);
      if (direction != null) {
        typeMap.put(type, direction);
      } else {
        LOG.error("Unsupported type {} for channel [{}]", type, getId());
      }
      for (Map.Entry<TransportType, ChannelDirection> typeIt : getSupportedTransportTypes().entrySet()) {
        if (!typeIt.getKey().equals(type)) {
          typeMap.put(typeIt.getKey(), ChannelDirection.DOWN);
        }
      }
    }
    //        try {
    //            sendKaaSyncRequest(typeMap);
    //        } catch (Exception e) {
    //            LOG.error("Failed to sync channel [{}]", getId(), e);
    //        }


  }

  @Override
  public void syncAll() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void syncAck(TransportType type) {
    LOG.info("Adding sync acknowledgement for type {} as a regular sync for channel [{}]", type, getId());
    syncAck(Collections.singleton(type));
  }

  @Override
  public void syncAck(Set<TransportType> type) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getId() {
    return CHANNEL_ID;
  }

  @Override
  public TransportProtocolId getTransportProtocolId() {
    return TransportProtocolIdConstants.COAP_TRANSPORT_ID;
  }

  @Override
  public ServerType getServerType() {
    return ServerType.OPERATIONS;
  }

  @Override
  public void setDemultiplexer(KaaDataDemultiplexer demultiplexer) {
    if (demultiplexer != null) {
      this.demultiplexer = demultiplexer;
    }
  }

  @Override
  public void setMultiplexer(KaaDataMultiplexer multiplexer) {
    if (multiplexer != null) {
      this.multiplexer = multiplexer;
    }
  }

  @Override
  public void setServer(TransportConnectionInfo server) {

    LOG.info("Setting server [{}] for channel [{}]", server, getId());
    if (server == null) {
      LOG.warn("Server is null for Channel [{}].", getId());
      return;
    }

    IpTransportInfo oldServer = currentServer;
    this.currentServer = new IpTransportInfo(server);
    //this.encDec = new MessageEncoderDecoder(state.getPrivateKey(), state.getPublicKey(), currentServer.getPublicKey());
    //uncompelete!

  }

  @Override
  public TransportConnectionInfo getServer() {
    return currentServer;

  }

  @Override
  public void setConnectivityChecker(ConnectivityChecker checker) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  public void shutdown() {
  }

  @Override
  public void pause() {
  }

  @Override
  public void resume() {
  }

  /**
   *
   * @param myUri  uri of source
   * @param method  CoAP method like POST,GET,...
   * @param payload Message Payload
   * @param msgType Confirmable or Non confirmable
   * @return  server response
   */
  public CoapResponse sendData(String myUri, String method, String payload, String msgType) {
    LOG.info("this is SendData function!");
    URI uri = null;

    if (myUri != null) {
      // input URI from command line arguments
      try {
        uri = new URI(myUri);
      } catch (URISyntaxException exception) {
        LOG.info("Invalid URI: " + exception.getMessage());
        // System.exit(-1);
      }

      CoapClient client = new CoapClient(uri);
      LOG.info(" new coap client is registerd");

      CoapResponse response;

      if (msgType == "NON") {
        client.useNONs();
      } else {
        client.useCONs();
      }

      if (method == "post") {
        response = client.post(payload, MediaTypeRegistry.TEXT_PLAIN);
      } else {
        response = client.get();
      }

      if (response != null) {

        LOG.info(String.valueOf(response.getCode()));
        LOG.info(String.valueOf(response.getOptions()));
        LOG.info(response.getResponseText());

        LOG.info("\nADVANCED\n");
        // access advanced API with access to more details through .advanced()
        LOG.info(Utils.prettyPrint(response));

      } else {
        LOG.info("No response received.");
      }

      return response;
    } else {
      // display help
      LOG.info("Californium (Cf) GET Client");
      LOG.info("(c) 2014, Institute for Pervasive Computing, ETH Zurich");
      String simpleName = CoapChannel.class.getSimpleName();
      LOG.info("Usage: " + simpleName + " URI");
      LOG.info("URI: The CoAP URI of the remote resource to GET");
      return null;
    }
  }
}
