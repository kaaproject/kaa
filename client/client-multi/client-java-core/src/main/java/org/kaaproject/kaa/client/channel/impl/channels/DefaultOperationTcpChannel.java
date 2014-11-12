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

package org.kaaproject.kaa.client.channel.impl.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.KaaTcpServerInfo;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
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

public class DefaultOperationTcpChannel implements KaaDataChannel {

    public static final Logger LOG = LoggerFactory //NOSONAR
            .getLogger(DefaultOperationTcpChannel.class);

    private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<TransportType, ChannelDirection>();
    static {
        SUPPORTED_TYPES.put(TransportType.PROFILE, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.CONFIGURATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.NOTIFICATION, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.USER, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.BIDIRECTIONAL);
        SUPPORTED_TYPES.put(TransportType.LOGGING, ChannelDirection.BIDIRECTIONAL);
    }

    private static final int PING_TIMEOUT = 100;
    private static final int CHANNEL_TIMEOUT = 200;

    private static final String CHANNEL_ID = "default_operation_tcp_channel";

    private KaaTcpServerInfo currentServer;
    private final KaaClientState state;

    private ScheduledExecutorService executor;

    private volatile boolean isShutdown = false;
    private volatile boolean isPaused = false;
    private boolean isFirstResponseReceived = false;
    private boolean isPendingSyncRequest = false;

    private KaaDataDemultiplexer demultiplexer;
    private KaaDataMultiplexer multiplexer;

    private volatile Socket socket;
    private MessageEncoderDecoder encDec;

    private final KaaChannelManager channelManager;

    private final int RECONNECT_TIMEOUT = 5; // in sec
    private ConnectivityChecker connectivityChecker;


    private final List<TransportType> ackTypes = new ArrayList<TransportType>();

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
                onServerFailed();
            }
        }

    };

    private final PingResponseListener pingResponseListener = new PingResponseListener() {

        @Override
        public void onMessage(PingResponse message) {
            LOG.info("PingResponse message received for channel [{}]", getId());
        }

    };

    private final SyncResponseListener kaaSyncResponseListener = new SyncResponseListener() {

        @Override
        public void onMessage(SyncResponse message) {
            LOG.info("KaaSync message (zipped={}, encrypted={}) received for channel [{}]", message.isZipped(), message.isEncrypted(), getId());
            byte [] resultBody = null;
            if (message.isEncrypted()) {
                synchronized (this) {
                    try {
                        resultBody = encDec.decodeData(message.getAvroObject());
                    } catch (GeneralSecurityException e) {
                        LOG.error("Failed to decrypt message body for channel [{}]: {}", getId());
                        LOG.error("Stack Trace: ", e);
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
                } catch (Exception e) {
                    LOG.error("Failed to process response for channel [{}]", getId());
                    LOG.error("Stack Trace: ", e);
                }
            }
            synchronized (this) {
                if(!isFirstResponseReceived){
                    LOG.info("First KaaSync message received and processed for channel [{}]", getId());
                    isFirstResponseReceived = true;
                    if(isPendingSyncRequest){
                        LOG.debug("There are pending requests for channel [{}] -> starting sync", getId());
                        syncAll();
                    }
                } else if (ackTypes.size() > 0) {
                    LOG.debug("Acknowledgment is pending for channel [{}] -> starting sync", getId());
                    if (ackTypes.size() > 1) {
                        syncAll();
                    } else {
                        sync(ackTypes.get(0));
                    }
                    ackTypes.clear();
                }
            }
        }
    };

    private final DisconnectListener disconnectListener = new DisconnectListener() {

        @Override
        public void onMessage(Disconnect message) {
            LOG.info("Disconnect message (reason={}) received for channel [{}]", message.getReason(), getId());
            if (!message.getReason().equals(DisconnectReason.NONE)) {
                LOG.error("Server error occurred: {}", message.getReason());
                onServerFailed();
            } else {
                closeConnection();
            }
        }
    };

    private final Runnable readTask =  new Runnable() {
        byte [] buffer = new byte[1024];

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    LOG.info("Channel [{}] is reading data from stream using [{}] byte buffer", getId(), buffer.length);
                    int size = socket.getInputStream().read(buffer);
                    LOG.info("Channel [{}] is read data {} bytes from stream", getId(), size);
                    if (size > 0) {
                        messageFactory.getFramer().pushBytes(Arrays.copyOf(buffer, size));
                    }else if(size == -1){
                        LOG.info("Channel [{}] received end of stream", getId(), size);
                        break;
                    }
                } catch (IOException | KaaTcpProtocolException e) {
                    if (!isShutdown && !isPaused) {
                        LOG.error("Failed to read from the socket for channel [{}]: {}", getId());
                        LOG.error("Stack trace: ", e);
                        onServerFailed();
                    }
                } catch (RuntimeException e) {
                    if (!Thread.currentThread().isInterrupted() && !isShutdown && !isPaused) {
                        LOG.error("Failed to read from the socket for channel [{}]: {}", getId());
                        LOG.error("Stack trace: ", e);
                        onServerFailed();
                    } else {
                        LOG.warn("Socket connection for channel was interrupted", e);
                        LOG.info("Socket connection for channel [{}] was interrupted, shutdown [{}]", getId(), isShutdown);
                    }
                }
            }
            LOG.info("Read Task is interrupted for channel [{}]", getId());
        }
    };

    private final Runnable pingTask = new Runnable() {

        @Override
        public void run() {
            if (!Thread.currentThread().isInterrupted()) {
                try {
                    sendPingRequest();
                    if (!Thread.currentThread().isInterrupted()) {
                        schedulePingTask();
                    } else {
                        LOG.info("Can't schedule ping task for channel [{}]. Task was interrupted", getId());
                    }
                } catch (IOException e) {
                    LOG.error("Failed to send ping request for channel [{}]: {}", getId());
                    LOG.error("Stack trace: ", e);
                    onServerFailed();
                }
            } else {
                LOG.info("Can't execute ping task for channel [{}]. Task was interrupted", getId());
            }
        }
    };

    private final MessageFactory messageFactory = new MessageFactory();

    private volatile Future<?> readTaskFuture;
    private volatile Future<?> pingTaskFuture;

    public DefaultOperationTcpChannel(KaaClientState state, KaaChannelManager channelManager) {
        this.state = state;
        this.channelManager = channelManager;
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
        byte [] body = multiplexer.compileRequest(types);
        byte[] requestBodyEncoded = encDec.encodeData(body);
        sendFrame( new SyncRequest(requestBodyEncoded, false, true));
    }

    private void sendConnect() throws Exception {
        LOG.debug("Sending Connect from channel [{}]", getId());
        byte [] body = multiplexer.compileRequest(getSupportedTransportTypes());
        byte [] requestBodyEncoded = encDec.encodeData(body);
        byte [] sessionKey = encDec.getEncodedSessionKey();
        byte [] signature = encDec.sign(sessionKey);
        sendFrame(new Connect(CHANNEL_TIMEOUT, sessionKey, requestBodyEncoded, signature));
    }

    private synchronized void closeConnection() {
        if (socket != null) {
            LOG.info("Channel \"{}\": closing current connection", getId());
            if (pingTaskFuture != null) {
                pingTaskFuture.cancel(true);
                pingTaskFuture = null;
            }
            if (readTaskFuture != null) {
                readTaskFuture.cancel(true);
                readTaskFuture = null;
            }
            try {
                sendDisconnect();
            } catch (IOException e) {
                LOG.error("Failed to send Disconnect to server: {}", e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.error("Failed to close socket: {}", e);
                }
                socket = null;
                messageFactory.getFramer().flush();
                isFirstResponseReceived = false;
            }
        }
    }

    protected Socket createSocket(String host, int port) throws UnknownHostException, IOException {
        return new Socket(host, port);
    }

    private synchronized void openConnection() {
        try {
            LOG.info("Channel [{}]: openning connection to server {}", getId(), currentServer);
            this.socket = createSocket(currentServer.getHost(), currentServer.getPort());
            readTaskFuture = executor.submit(readTask);
            sendConnect();
            schedulePingTask();
        } catch (Exception e) {
            LOG.error("Failed to create a socket for server {}:{}", currentServer.getHost(), currentServer.getPort());
            LOG.error("Stack trace: ", e);
            onServerFailed();
            socket = null;
        }
    }

    private void onServerFailed() {
        closeConnection();
        if (connectivityChecker != null && !connectivityChecker.checkConnectivity()) {
            LOG.warn("Loss of connectivity. Attempt to reconnect will be made in {} sec", RECONNECT_TIMEOUT);
            executor.schedule(openConnectionTask, RECONNECT_TIMEOUT, TimeUnit.SECONDS);
            return;
        }

        channelManager.onServerFailed(currentServer);
    }

    private void schedulePingTask() {
        if (executor != null) {
            LOG.debug("Scheduling a ping task ({} seconds) for channel [{}]", PING_TIMEOUT, getId());
            pingTaskFuture = executor.schedule(pingTask, PING_TIMEOUT, TimeUnit.SECONDS);
        }
    }

    protected ScheduledExecutorService createExecutor() {
        LOG.info("Creating a new executor for channel [{}]", getId());
        return new ScheduledThreadPoolExecutor(2);
    }

    @Override
    public synchronized void sync(TransportType type) {
        if (isShutdown) {
            LOG.info("Can't sync. Channel [{}] is down", getId());
            return;
        }
        if (isPaused) {
            LOG.info("Can't sync. Channel [{}] is paused", getId());
            return;
        }
        if (!isFirstResponseReceived) {
            LOG.info("Can't sync. Channel [{}] is waiting for CONNACK message + KAASYNC message", getId());
            isPendingSyncRequest = true;
            return;
        }
        LOG.info("Processing sync {} for channel [{}]", type, getId());
        if (multiplexer != null && demultiplexer != null) {
            if (currentServer != null && socket != null) {
                ChannelDirection direction = getSupportedTransportTypes().get(type);
                if (direction != null) {
                    Map<TransportType, ChannelDirection> typeMap = new HashMap<>(getSupportedTransportTypes().size());
                    typeMap.put(type, direction);
                    for (Map.Entry<TransportType, ChannelDirection> typeIt : getSupportedTransportTypes().entrySet()) {
                        if (!typeIt.getKey().equals(type)) {
                            typeMap.put(typeIt.getKey(), ChannelDirection.DOWN);
                        }
                    }
                    try {
                        sendKaaSyncRequest(typeMap);
                    } catch (Exception e) {
                        LOG.error("Failed to sync channel [{}]", getId());
                        LOG.error("Stack trace: ", e);
                        onServerFailed();
                    }
                } else {
                    LOG.error("Unsupported type {} for channel [{}]", type, getId());
                }
            } else {
                LOG.warn("Can't sync. Server is {}, socket is \"{}\"", currentServer, socket);
            }
        }
    }

    @Override
    public synchronized void syncAll() {
        if (isShutdown) {
            LOG.info("Can't sync. Channel [{}] is down", getId());
            return;
        }
        if (isPaused) {
            LOG.info("Can't sync. Channel [{}] is paused", getId());
            return;
        }
        if (!isFirstResponseReceived) {
            LOG.info("Can't sync. Channel [{}] is waiting for CONNACK + KAASYNC message", getId());
            isPendingSyncRequest = true;
            return;
        }
        LOG.info("Processing sync all for channel [{}]", getId());
        if (multiplexer != null && demultiplexer != null) {
            if (currentServer != null && socket != null) {
                try {
                    sendKaaSyncRequest(getSupportedTransportTypes());
                } catch (Exception e) {
                    LOG.error("Failed to sync channel [{}]: {}", getId(), e);
                    onServerFailed();
                }
            } else {
                LOG.warn("Can't sync. Server is {}, socket is {}", currentServer, socket);
            }
        }
    }

    @Override
    public void syncAck(TransportType type) {
        LOG.info("Adding sync acknowledgement for type {} as a regular sync for channel [{}]", type, getId());
        ackTypes.add(type);
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
    public synchronized void setServer(ServerInfo server) {
        if (server == null) {
            LOG.warn("Server is null for Channel [{}].", getId());
            return;
        }
        if (isShutdown) {
            LOG.info("Can't set server. Channel [{}] is down", getId());
            return;
        }
        this.currentServer = (KaaTcpServerInfo) server;
        this.encDec = new MessageEncoderDecoder(state.getPrivateKey(), state.getPublicKey(), currentServer.getPublicKey());
        if (!isPaused) {
            if (executor == null) {
                executor = createExecutor();
            }
            closeConnection();
            executor.submit(openConnectionTask);
        } else {
            LOG.info("Can't start new session. Channel [{}] is paused", getId());
        }
    }

    @Override
    public void setConnectivityChecker(ConnectivityChecker checker) {
        connectivityChecker = checker;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        closeConnection();
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public synchronized void pause() {
        if (!isPaused) {
            isPaused = true;
            closeConnection();
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
        }
    }

    @Override
    public synchronized void resume() {
        if (isPaused) {
            isPaused = false;
            if (executor == null) {
                executor = createExecutor();
            }
            executor.submit(openConnectionTask);
        }
    }

    @Override
    public String getId() {
        return CHANNEL_ID;
    }

    @Override
    public ChannelType getType() {
        return ChannelType.KAATCP;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.OPERATIONS;
    }

    @Override
    public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
        return SUPPORTED_TYPES;
    }
}
