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

package org.kaaproject.kaa.server.bootstrap.service.transport;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.bootstrap.BootstrapClientSync;
import org.kaaproject.kaa.server.sync.bootstrap.BootstrapServerSync;
import org.kaaproject.kaa.server.sync.bootstrap.ProtocolConnectionData;
import org.kaaproject.kaa.server.sync.platform.PlatformEncDec;
import org.kaaproject.kaa.server.sync.platform.PlatformEncDecException;
import org.kaaproject.kaa.server.sync.platform.PlatformLookup;
import org.kaaproject.kaa.server.transport.AbstractTransportService;
import org.kaaproject.kaa.server.transport.TransportService;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.MessageHandler;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Responsible for initialization and management of transport instances
 * 
 * @author Andrew Shvayka
 *
 */
@Service
public class BootstrapTransportService extends AbstractTransportService implements TransportService {

    /** Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(BootstrapTransportService.class);

    private static final int DEFAULT_THREAD_POOL_SIZE = 1;
    
    private static final String TRANSPORT_CONFIG_PREFIX = "bootstrap";

    @Value("#{properties[worker_thread_pool]}")
    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

    @Value("#{properties[support_unencrypted_connection]}")
    private boolean supportUnencryptedConnection;

    @Autowired
    private OperationsServerListService operationsServerListService;

    @Autowired
    private KeyStoreService bootstrapKeyStoreService;

    @Autowired
    private Properties properties;

    private BootstrapMessageHandler handler;

    public BootstrapTransportService() {
        super();
    }

    @Override
    protected String getTransportConfigPrefix() {
        return TRANSPORT_CONFIG_PREFIX;
    }
    
    @Override
    protected Properties getServiceProperties() {
        return properties;
    }

    @Override
    public void lookupAndInit() {
        LOG.info("Lookup platform protocols");
        Set<String> platformProtocols = PlatformLookup.lookupPlatformProtocols(PlatformLookup.DEFAULT_PROTOCOL_LOOKUP_PACKAGE_NAME);
        LOG.info("Initializing message handler with {} worker threads", threadPoolSize);
        handler = new BootstrapMessageHandler(operationsServerListService, Executors.newFixedThreadPool(threadPoolSize), platformProtocols,
                new KeyPair(bootstrapKeyStoreService.getPublicKey(), bootstrapKeyStoreService.getPrivateKey()), supportUnencryptedConnection);
        super.lookupAndInit();
    }

    @Override
    protected MessageHandler getMessageHandler() {
        return handler;
    }

    @Override
    protected PublicKey getPublicKey() {
        return bootstrapKeyStoreService.getPublicKey();
    }

    @Override
    public void stop() {
        super.stop();
        handler.stop();
    }

    public static class BootstrapMessageHandler implements MessageHandler {

        private final ExecutorService executor;
        private final Set<String> platformProtocols;
        private final KeyPair keyPair;
        private final boolean supportUnencryptedConnection;
        private final OperationsServerListService opsListService;

        private static final ThreadLocal<Map<Integer, PlatformEncDec>> platformEncDecMap = new ThreadLocal<>(); //NOSONAR
        private static final ThreadLocal<MessageEncoderDecoder> crypt = new ThreadLocal<>(); //NOSONAR

        public BootstrapMessageHandler(OperationsServerListService opsListService, ExecutorService executor, Set<String> platformProtocols,
                KeyPair keyPair, boolean supportUnencryptedConnection) {
            super();
            this.opsListService = opsListService;
            this.executor = executor;
            this.platformProtocols = platformProtocols;
            this.keyPair = keyPair;
            this.supportUnencryptedConnection = supportUnencryptedConnection;
        }

        @Override
        public void process(SessionAware message) {
            // Session messages are not processed
        }

        @Override
        public void process(final SessionInitMessage message) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    MessageEncoderDecoder crypt = getOrInitCrypt();
                    Map<Integer, PlatformEncDec> platformEncDecMap = getOrInitMap(platformProtocols);
                    try {
                        ClientSync request = decodeRequest(message, crypt, platformEncDecMap);
                        LOG.trace("Processing request {}", request);
                        BootstrapClientSync bsRequest = request.getBootstrapSync();
                        Set<ProtocolConnectionData> transports = opsListService.filter(bsRequest.getKeys());
                        BootstrapServerSync bsResponse = new BootstrapServerSync(bsRequest.getRequestId(), transports);
                        ServerSync response = new ServerSync();
                        response.setRequestId(request.getRequestId());
                        response.setStatus(SyncStatus.SUCCESS);
                        response.setBootstrapSync(bsResponse);
                        LOG.trace("Response {}", response);
                        encodeAndForward(message, crypt, platformEncDecMap, response);
                        LOG.trace("Response forwarded to specific transport {}", response);
                    } catch (Exception e) {
                        processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
                    }
                }

                private void encodeAndForward(final SessionInitMessage message, MessageEncoderDecoder crypt,
                        Map<Integer, PlatformEncDec> platformEncDecMap, ServerSync response) throws PlatformEncDecException,
                        GeneralSecurityException {
                    MessageBuilder converter = message.getMessageBuilder();
                    byte[] responseData = encodePlatformLevelData(platformEncDecMap, message.getPlatformId(), response);
                    Object[] objects;
                    if (message.isEncrypted()) {
                        byte[] responseSignature = crypt.sign(responseData);
                        responseData = crypt.encodeData(responseData);
                        LOG.trace("Response signature {}", responseSignature);
                        LOG.trace("Response data {}", responseData);
                        objects = converter.build(responseData, responseSignature, message.isEncrypted());
                    } else {
                        LOG.trace("Response data {}", responseData);
                        objects = converter.build(responseData, message.isEncrypted());
                    }

                    ChannelContext context = message.getChannelContext();
                    if (objects != null && objects.length > 0) {
                        for (Object object : objects) {
                            context.write(object);
                        }
                        context.flush();
                    }
                }

                private void processErrors(ChannelContext ctx, ErrorBuilder converter, Exception e) {
                    LOG.trace("Message processing failed", e);
                    Object[] responses = converter.build(e);
                    if (responses != null && responses.length > 0) {
                        for (Object response : responses) {
                            ctx.writeAndFlush(response);
                        }
                    } else {
                        ctx.fireExceptionCaught(e);
                    }
                }

                private ClientSync decodeRequest(SessionInitMessage message, MessageEncoderDecoder crypt,
                        Map<Integer, PlatformEncDec> platformEncDecMap) throws GeneralSecurityException, PlatformEncDecException {
                    ClientSync syncRequest = null;
                    if (message.isEncrypted()) {
                        syncRequest = decodeEncryptedRequest(message, crypt, platformEncDecMap);
                    } else if (supportUnencryptedConnection) {
                        syncRequest = decodeUnencryptedRequest(message, platformEncDecMap);
                    } else {
                        LOG.warn("Received unencrypted init message, but unencrypted connection forbidden by configuration.");
                        throw new GeneralSecurityException("Unencrypted connection forbidden by configuration.");
                    }
                    if (syncRequest.getBootstrapSync() == null) {
                        throw new IllegalArgumentException("Bootstrap sync message is missing");
                    }
                    return syncRequest;
                }

                private ClientSync decodeEncryptedRequest(SessionInitMessage message, MessageEncoderDecoder crypt,
                        Map<Integer, PlatformEncDec> platformEncDecMap) throws GeneralSecurityException, PlatformEncDecException {
                    byte[] requestRaw = crypt.decodeData(message.getEncodedMessageData(), message.getEncodedSessionKey());
                    LOG.trace("Request data decrypted");
                    ClientSync request = decodePlatformLevelData(platformEncDecMap, message.getPlatformId(), requestRaw);
                    LOG.trace("Request data deserialized");
                    return request;
                }

                private ClientSync decodeUnencryptedRequest(SessionInitMessage message, Map<Integer, PlatformEncDec> platformEncDecMap)
                        throws GeneralSecurityException, PlatformEncDecException {
                    byte[] requestRaw = message.getEncodedMessageData();
                    LOG.trace("Try to convert raw data to SynRequest object");
                    ClientSync request = decodePlatformLevelData(platformEncDecMap, message.getPlatformId(), requestRaw);
                    LOG.trace("Request data deserialized");
                    return request;
                }

                private byte[] encodePlatformLevelData(Map<Integer, PlatformEncDec> platformEncDecMap, int platformID, ServerSync sync)
                        throws PlatformEncDecException {
                    PlatformEncDec encDec = platformEncDecMap.get(platformID);
                    if (encDec != null) {
                        return platformEncDecMap.get(platformID).encode(sync);
                    } else {
                        throw new PlatformEncDecException(MessageFormat.format("Encoder for platform protocol [{0}] is not defined",
                                platformID));
                    }
                }

                private ClientSync decodePlatformLevelData(Map<Integer, PlatformEncDec> platformEncDecMap, Integer platformID,
                        byte[] requestRaw) throws PlatformEncDecException {
                    PlatformEncDec encDec = platformEncDecMap.get(platformID);
                    if (encDec != null) {
                        return platformEncDecMap.get(platformID).decode(requestRaw);
                    } else {
                        throw new PlatformEncDecException(MessageFormat.format("Decoder for platform protocol [{0}] is not defined",
                                platformID));
                    }
                }

                private MessageEncoderDecoder getOrInitCrypt() {
                    if (crypt.get() == null) {
                        crypt.set(new MessageEncoderDecoder(keyPair.getPrivate(), keyPair.getPublic()));
                    }
                    return crypt.get();
                }

                private Map<Integer, PlatformEncDec> getOrInitMap(Set<String> platformProtocols) {
                    if (platformEncDecMap.get() == null) {
                        platformEncDecMap.set(PlatformLookup.initPlatformProtocolMap(platformProtocols));
                    }
                    return platformEncDecMap.get();
                }
            });
        }

        public void stop() {
            executor.shutdown();
        }

    }

}
