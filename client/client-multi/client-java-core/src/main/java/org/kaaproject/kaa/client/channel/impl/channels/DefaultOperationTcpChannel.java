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

package org.kaaproject.kaa.client.channel.impl.channels;

import org.kaaproject.kaa.client.FailureListener;
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
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnAckListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.DisconnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingResponseListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.SyncResponseListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck.ReturnCode;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageFactory;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncResponse;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultOperationTcpChannel implements KaaDataChannel {

  public static final Logger LOG = LoggerFactory // NOSONAR
      .getLogger(DefaultOperationTcpChannel.class);

  private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<>();
  private static final int CHANNEL_TIMEOUT = 200;
  private static final int PING_TIMEOUT = CHANNEL_TIMEOUT / 2;
  private static final String CHANNEL_ID = "default_operation_tcp_channel";

  static {
    SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.BIDIRECTIONAL);
    SUPPORTED_TYPES.put(TransportType.LOGGING, ChannelDirection.BIDIRECTIONAL);
  }

  private final KaaClientState state;
  private final FailoverManager failoverManager;
  private final PingResponseListener pingResponseListener = new PingResponseListener() {

    @Override
    public void onMessage(PingResponse message) {
      LOG.info("PingResponse message received for channel [{}]", getId());
    }

  };
  private final MessageFactory messageFactory = new MessageFactory();
  private FailureListener failureListener;
  private IpTransportInfo currentServer;
  private ScheduledExecutorService executor;
  private volatile State channelState = State.CLOSED;
  private KaaDataDemultiplexer demultiplexer;
  private KaaDataMultiplexer multiplexer;
  private volatile Socket socket;
  private MessageEncoderDecoder encDec;
  private final SyncResponseListener kaaSyncResponseListener = new SyncResponseListener() {

    @Override
    public void onMessage(SyncResponse message) {
      LOG.info("KaaSync message (zipped={}, encrypted={}) received for channel [{}]",
              message.isZipped(), message.isEncrypted(), getId());
      byte[] resultBody = null;
      if (message.isEncrypted()) {
        synchronized (this) {
          try {
            resultBody = encDec.decodeData(message.getAvroObject());
          } catch (GeneralSecurityException ex) {
            LOG.error("Failed to decrypt message body for channel [{}]: {}", getId());
            LOG.error("Stack Trace: ", ex);
          }
        }
      } else {
        resultBody = message.getAvroObject();
      }
      if (resultBody != null) {
        try {
          demultiplexer.preProcess();
          demultiplexer.processResponse(resultBody);
          demultiplexer.postProcess();
        } catch (Exception ex) {
          LOG.error("Failed to process response for channel [{}]", getId(), ex);
        }

        synchronized (DefaultOperationTcpChannel.this) {
          channelState = State.OPENED;
        }
        failoverManager.onServerConnected(currentServer);
      }
    }
  };
  private volatile ConnectivityChecker connectivityChecker;
  private volatile Future<?> pingTaskFuture;
  private volatile Future<?> readTaskFuture;
  private volatile boolean isOpenConnectionScheduled;
  private final Runnable openConnectionTask = new Runnable() {
    @Override
    public void run() {
      openConnection();
    }
  };
  private final ConnAckListener connAckListener = new ConnAckListener() {

    @Override
    public void onMessage(ConnAck message) {
      LOG.info("ConnAck ({}) message received for channel [{}]", message.getReturnCode(), getId());

      if (message.getReturnCode() != ReturnCode.ACCEPTED) {
        LOG.error("Connection for channel [{}] was rejected: {}", getId(), message.getReturnCode());

        LOG.info("Cleaning client state");
        state.clean();

        if (message.getReturnCode() == ReturnCode.REFUSE_VERIFICATION_FAILED) {
          onServerFailed(FailoverStatus.ENDPOINT_VERIFICATION_FAILED);
        } else {
          onServerFailed();
        }
      }
    }

  };
  private final DisconnectListener disconnectListener = new DisconnectListener() {

    @Override
    public void onMessage(Disconnect message) {
      LOG.info("Disconnect message (reason={}) received for channel [{}]",
              message.getReason(), getId());
      switch (message.getReason()) {
        case NONE:
          closeConnection();
          break;
        case CREDENTIALS_REVOKED:
          LOG.error("Endpoint credentials been revoked");
          onServerFailed(FailoverStatus.ENDPOINT_CREDENTIALS_REVOKED);
          break;
        default:
          LOG.error("Server error occurred: {}", message.getReason());
          onServerFailed();
          break;
      }
    }
  };
  private final Runnable pingTask = new Runnable() {

    @Override
    public void run() {
      if (!Thread.currentThread().isInterrupted()) {
        try {
          LOG.info("Executing ping task for channel [{}]", getId());
          sendPingRequest();
          if (!Thread.currentThread().isInterrupted()) {
            schedulePingTask();
          } else {
            LOG.info("Can't schedule ping task for channel [{}]. Task was interrupted", getId());
          }
        } catch (IOException ex) {
          LOG.error("Failed to send ping request for channel [{}]. Stack trace: ", getId(), ex);
          onServerFailed();
        }
      } else {
        LOG.info("Can't execute ping task for channel [{}]. Task was interrupted", getId());
      }
    }
  };

  /**
   * All-args constructor.
   */
  public DefaultOperationTcpChannel(KaaClientState state, FailoverManager failoverManager,
                                    FailureListener failureListener) {
    this.state = state;
    this.failoverManager = failoverManager;
    this.failureListener = failureListener;
    messageFactory.registerMessageListener(connAckListener);
    messageFactory.registerMessageListener(kaaSyncResponseListener);
    messageFactory.registerMessageListener(pingResponseListener);
    messageFactory.registerMessageListener(disconnectListener);
  }

  private void sendFrame(MqttFrame frame) throws IOException {
    if (socket != null) {
      synchronized (socket) {
        socket.getOutputStream().write(frame.getFrame().array());
      }
    }
  }

  private void sendPingRequest() throws IOException {
    LOG.debug("Sending PinRequest from channel [{}]", getId());
    sendFrame(new PingRequest());
  }

  private void sendDisconnect() throws IOException {
    LOG.debug("Sending Disconnect from channel [{}]", getId());
    sendFrame(new Disconnect(DisconnectReason.NONE));
  }

  private void sendKaaSyncRequest(Map<TransportType, ChannelDirection> types) throws Exception {
    LOG.debug("Sending KaaSync from channel [{}]", getId());
    byte[] body = multiplexer.compileRequest(types);
    byte[] requestBodyEncoded = encDec.encodeData(body);
    sendFrame(new SyncRequest(requestBodyEncoded, false, true));
  }

  private void sendConnect() throws Exception {
    LOG.debug("Sending Connect to channel [{}]", getId());
    byte[] body = multiplexer.compileRequest(getSupportedTransportTypes());
    byte[] requestBodyEncoded = encDec.encodeData(body);
    byte[] sessionKey = encDec.getEncodedSessionKey();
    byte[] signature = encDec.sign(sessionKey);
    sendFrame(new Connect(CHANNEL_TIMEOUT, Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID_V2,
            sessionKey, requestBodyEncoded, signature));
  }

  private synchronized void closeConnection() {
    if (pingTaskFuture != null && !pingTaskFuture.isCancelled()) {
      pingTaskFuture.cancel(true);
    }

    if (readTaskFuture != null && !readTaskFuture.isCancelled()) {
      readTaskFuture.cancel(true);
    }

    if (socket != null) {
      LOG.info("Channel \"{}\": closing current connection", getId());
      try {
        sendDisconnect();
      } catch (IOException ex) {
        LOG.error("Failed to send Disconnect to server: {}", ex);
      } finally {
        try {
          socket.close();
        } catch (IOException ex) {
          LOG.error("Failed to close socket: {}", ex);
        }
        socket = null;
        messageFactory.getFramer().flush();
        if (channelState != State.SHUTDOWN) {
          channelState = State.CLOSED;
        }
      }
    }
  }

  protected Socket createSocket(String host, int port) throws UnknownHostException, IOException {
    return new Socket(host, port);
  }

  private synchronized void openConnection() {
    if (channelState == State.PAUSE || channelState == State.SHUTDOWN) {
      LOG.info("Can't open connection, as channel is in the {} state", channelState);
      return;
    }
    try {
      LOG.info("Channel [{}]: opening connection to server {}", getId(), currentServer);
      isOpenConnectionScheduled = false;
      socket = createSocket(currentServer.getHost(), currentServer.getPort());
      sendConnect();
      scheduleReadTask(socket);
      schedulePingTask();
    } catch (Exception ex) {
      LOG.error("Failed to create a socket for server {}:{}. Stack trace: ",
              currentServer.getHost(), currentServer.getPort(), ex);
      onServerFailed();
    }
  }

  private void onServerFailed() {
    this.onServerFailed(FailoverStatus.NO_CONNECTIVITY);
  }

  private void onServerFailed(FailoverStatus status) {
    LOG.info("[{}] has failed", getId());
    closeConnection();
    if (connectivityChecker != null && !connectivityChecker.checkConnectivity()) {
      LOG.warn("Loss of connectivity detected");

      FailoverDecision decision = failoverManager.onFailover(status);
      switch (decision.getAction()) {
        case NOOP:
          LOG.warn("No operation is performed according to failover strategy decision");
          break;
        case RETRY:
          long retryPeriod = decision.getRetryPeriod();
          LOG.warn("Attempt to reconnect will be made in {} ms "
                  + "according to failover strategy decision", retryPeriod);
          scheduleOpenConnectionTask(retryPeriod);
          break;
        case FAILURE:
          LOG.warn("Calling failure listener according to failover strategy decision!");
          failureListener.onFailure();
          break;
        default:
          break;
      }
    } else {
      failoverManager.onServerFailed(currentServer, status);
    }
  }

  private synchronized void scheduleOpenConnectionTask(long retryPeriod) {
    if (!isOpenConnectionScheduled) {
      if (executor != null) {
        LOG.info("Scheduling open connection task");
        executor.schedule(openConnectionTask, retryPeriod, TimeUnit.MILLISECONDS);
        isOpenConnectionScheduled = true;
      } else {
        LOG.info("Executor is null, can't schedule open connection task");
      }
    } else {
      LOG.info("Reconnect is already scheduled, ignoring the call");
    }
  }

  private void scheduleReadTask(Socket socket) {
    if (executor != null) {
      readTaskFuture = executor.submit(new SocketReadTask(socket));
      LOG.debug("Submitting a read task for channel [{}]", getId());
    } else {
      LOG.warn("Executor is null, can't submit read task");
    }
  }

  private void schedulePingTask() {
    if (executor != null) {
      LOG.debug("Scheduling a ping task ({} seconds) for channel [{}]", PING_TIMEOUT, getId());
      pingTaskFuture = executor.schedule(pingTask, PING_TIMEOUT, TimeUnit.SECONDS);
    } else {
      LOG.warn("Executor is null, can't schedule ping task");
    }
  }

  protected ScheduledExecutorService createExecutor() {
    LOG.info("Creating a new executor for channel [{}]", getId());
    return new ScheduledThreadPoolExecutor(2);
  }

  @Override
  public synchronized void sync(TransportType type) {
    sync(Collections.singleton(type));
  }

  @Override
  public synchronized void sync(Set<TransportType> types) {
    if (channelState == State.SHUTDOWN) {
      LOG.info("Can't sync. Channel [{}] is down", getId());
      return;
    }
    if (channelState == State.PAUSE) {
      LOG.info("Can't sync. Channel [{}] is paused", getId());
      return;
    }
    if (channelState != State.OPENED) {
      LOG.info("Can't sync. Channel [{}] is waiting for CONNACK message + KAASYNC message",
              getId());
      return;
    }
    if (multiplexer == null) {
      LOG.warn("Can't sync. Channel {} multiplexer is not set", getId());
      return;
    }
    if (demultiplexer == null) {
      LOG.warn("Can't sync. Channel {} demultiplexer is not set", getId());
      return;
    }
    if (currentServer == null || socket == null) {
      LOG.warn("Can't sync. Server is {}, socket is \"{}\"", currentServer, socket);
      return;
    }

    Map<TransportType, ChannelDirection> typeMap =
            new HashMap<>(getSupportedTransportTypes().size());
    for (TransportType type : types) {
      LOG.info("Processing sync {} for channel [{}]", type, getId());
      ChannelDirection direction = getSupportedTransportTypes().get(type);
      if (direction != null) {
        typeMap.put(type, direction);
      } else {
        LOG.error("Unsupported type {} for channel [{}]", type, getId());
      }
      for (Map.Entry<TransportType, ChannelDirection> typeIt : getSupportedTransportTypes()
              .entrySet()) {
        if (!typeIt.getKey().equals(type)) {
          typeMap.put(typeIt.getKey(), ChannelDirection.DOWN);
        }
      }
    }
    try {
      sendKaaSyncRequest(typeMap);
    } catch (Exception ex) {
      LOG.error("Failed to sync channel [{}]", getId(), ex);
    }
  }

  @Override
  public synchronized void syncAll() {
    if (channelState == State.SHUTDOWN) {
      LOG.info("Can't sync. Channel [{}] is down", getId());
      return;
    }
    if (channelState == State.PAUSE) {
      LOG.info("Can't sync. Channel [{}] is paused", getId());
      return;
    }
    if (channelState != State.OPENED) {
      LOG.info("Can't sync. Channel [{}] is waiting for CONNACK + KAASYNC message", getId());
      return;
    }
    LOG.info("Processing sync all for channel [{}]", getId());
    if (multiplexer != null && demultiplexer != null) {
      if (currentServer != null && socket != null) {
        try {
          sendKaaSyncRequest(getSupportedTransportTypes());
        } catch (Exception ex) {
          LOG.error("Failed to sync channel [{}]: {}", getId(), ex);
          onServerFailed();
        }
      } else {
        LOG.warn("Can't sync. Server is {}, socket is {}", currentServer, socket);
      }
    }
  }

  @Override
  public void syncAck(TransportType type) {
    LOG.info("Adding sync acknowledgement for type {} as a regular sync for channel [{}]",
            type, getId());
    syncAck(Collections.singleton(type));
  }

  @Override
  public void syncAck(Set<TransportType> types) {
    synchronized (this) {
      if (channelState != State.OPENED) {
        LOG.info("First KaaSync message received and processed for channel [{}]", getId());
        channelState = State.OPENED;
        failoverManager.onServerConnected(currentServer);
        LOG.debug("There are pending requests for channel [{}] -> starting sync", getId());
        syncAll();
      } else {
        LOG.debug("Acknowledgment is pending for channel [{}] -> starting sync", getId());
        if (types.size() == 1) {
          sync(types.iterator().next());
        } else {
          syncAll();
        }
      }
    }
  }

  @Override
  public synchronized void setDemultiplexer(KaaDataDemultiplexer demultiplexer) {
    if (demultiplexer != null) {
      this.demultiplexer = demultiplexer;
    }
  }

  @Override
  public synchronized void setMultiplexer(KaaDataMultiplexer multiplexer) {
    if (multiplexer != null) {
      this.multiplexer = multiplexer;
    }
  }

  @Override
  public TransportConnectionInfo getServer() {
    return currentServer;
  }

  @Override
  public synchronized void setServer(TransportConnectionInfo server) {
    LOG.info("Setting server [{}] for channel [{}]", server, getId());
    if (server == null) {
      LOG.warn("Server is null for Channel [{}].", getId());
      return;
    }
    if (channelState == State.SHUTDOWN) {
      LOG.info("Can't set server. Channel [{}] is down", getId());
      return;
    }
    IpTransportInfo oldServer = currentServer;
    this.currentServer = new IpTransportInfo(server);
    this.encDec = new MessageEncoderDecoder(state.getPrivateKey(), state.getPublicKey(),
            currentServer.getPublicKey());
    if (channelState != State.PAUSE) {
      if (executor == null) {
        executor = createExecutor();
      }
      if (oldServer == null
          || socket == null
          || !oldServer.getHost().equals(currentServer.getHost())
          || oldServer.getPort() != currentServer.getPort()) {
        LOG.info("New server's: {} host or ip is different from the old {}, reconnecting",
                currentServer, oldServer);
        closeConnection();
        scheduleOpenConnectionTask(0);
      }
    } else {
      LOG.info("Can't start new session. Channel [{}] is paused", getId());
    }
  }

  @Override
  public void setConnectivityChecker(ConnectivityChecker checker) {
    connectivityChecker = checker;
  }

  @Override
  public synchronized void shutdown() {
    LOG.info("Shutting down...");
    channelState = State.SHUTDOWN;
    closeConnection();
    destroyExecutor();
  }

  @Override
  public synchronized void pause() {
    if (channelState != State.PAUSE) {
      LOG.info("Pausing...");
      channelState = State.PAUSE;
      closeConnection();
      destroyExecutor();
    }
  }

  private synchronized void destroyExecutor() {
    if (executor != null) {
      executor.shutdownNow();
      isOpenConnectionScheduled = false;
      executor = null;
    }
  }

  @Override
  public synchronized void resume() {
    if (channelState == State.PAUSE) {
      LOG.info("Resuming...");
      channelState = State.CLOSED;
      if (executor == null) {
        executor = createExecutor();
      }
      scheduleOpenConnectionTask(0);
    }
  }

  @Override
  public String getId() {
    return CHANNEL_ID;
  }

  @Override
  public TransportProtocolId getTransportProtocolId() {
    return TransportProtocolIdConstants.TCP_TRANSPORT_ID;
  }

  @Override
  public ServerType getServerType() {
    return ServerType.OPERATIONS;
  }

  @Override
  public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
    return SUPPORTED_TYPES;
  }

  private enum State {
    SHUTDOWN, PAUSE, CLOSED, OPENED
  }

  private class SocketReadTask implements Runnable {
    private final Socket readTaskSocket;
    private final byte[] buffer;

    public SocketReadTask(Socket readTaskSocket) {
      this.readTaskSocket = readTaskSocket;
      this.buffer = new byte[1024];
    }

    @Override
    public void run() {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          LOG.info("Channel [{}] is reading data from stream using [{}] byte buffer",
                  getId(), buffer.length);

          int size = readTaskSocket.getInputStream().read(buffer);

          if (size > 0) {
            messageFactory.getFramer().pushBytes(Arrays.copyOf(buffer, size));
          } else if (size == -1) {
            LOG.info("Channel [{}] received end of stream ({})", getId(), size);
            onServerFailed();
          }

        } catch (IOException | KaaTcpProtocolException | RuntimeException ex) {
          if (Thread.currentThread().isInterrupted()) {
            if (channelState != State.SHUTDOWN) {
              LOG.warn("Socket connection for channel [{}] was interrupted: ", getId(), ex);
            } else {
              LOG.debug("Socket connection for channel [{}] was interrupted.", getId());
            }
          }

          if (readTaskSocket.equals(socket)) {
            onServerFailed();
          } else {
            LOG.debug("Stale socket: {} is detected, killing read task...", readTaskSocket);
          }
          break;
        }
      }
      LOG.info("Read Task is interrupted for channel [{}]", getId());
    }
  }
}
