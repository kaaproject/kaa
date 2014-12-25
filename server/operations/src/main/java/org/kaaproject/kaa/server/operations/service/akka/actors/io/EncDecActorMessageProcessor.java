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
package org.kaaproject.kaa.server.operations.service.akka.actors.io;

import io.netty.channel.ChannelHandlerContext;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.pojo.sync.ClientSync;
import org.kaaproject.kaa.server.operations.pojo.sync.RedirectServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.ServerSync;
import org.kaaproject.kaa.server.operations.pojo.sync.SyncStatus;
import org.kaaproject.kaa.server.operations.service.akka.actors.io.platform.PlatformEncDec;
import org.kaaproject.kaa.server.operations.service.akka.actors.io.platform.PlatformEncDecException;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ErrorBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.Request;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.ResponseBuilder;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAware;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionAwareRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.request.SessionInitRequest;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettySessionResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.SessionResponse;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.metrics.MeterClient;
import org.kaaproject.kaa.server.operations.service.metrics.MetricsService;
import org.kaaproject.kaa.server.operations.service.netty.NettySessionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class EncDecActorMessageProcessor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EncDecActorMessageProcessor.class);

    private final CacheService cacheService;

    private final MessageEncoderDecoder crypt;

    private final Map<Integer, PlatformEncDec> platformEncDecMap;

    private final Boolean supportUnencryptedConnection;

    /** The eps actor. */
    private final ActorRef opsActor;

    private final MeterClient sessionInitMeter;
    private final MeterClient sessionRequestMeter;
    private final MeterClient sessionResponseMeter;
    private final MeterClient redirectMeter;
    private final MeterClient errorMeter;

    protected EncDecActorMessageProcessor(ActorRef epsActor, MetricsService metricsService, CacheService cacheService, KeyPair serverKeys,
            Set<String> platformProtocols, Boolean supportUnencryptedConnection) {
        super();
        this.opsActor = epsActor;
        this.cacheService = cacheService;
        this.supportUnencryptedConnection = supportUnencryptedConnection;
        this.crypt = new MessageEncoderDecoder(serverKeys.getPrivate(), serverKeys.getPublic());
        this.platformEncDecMap = initPlatformProtocolMap(platformProtocols);
        this.sessionInitMeter = metricsService.createMeter("sessionInitMeter", Thread.currentThread().getName());
        this.sessionRequestMeter = metricsService.createMeter("sessionRequestMeter", Thread.currentThread().getName());
        this.sessionResponseMeter = metricsService.createMeter("sessionResponseMeter", Thread.currentThread().getName());
        this.redirectMeter = metricsService.createMeter("redirectMeter", Thread.currentThread().getName());
        this.errorMeter = metricsService.createMeter("errorMeter", Thread.currentThread().getName());
    }

    private Map<Integer, PlatformEncDec> initPlatformProtocolMap(Set<String> platformProtocols) {
        Map<Integer, PlatformEncDec> platformEncDecMap = new HashMap<>();
        for (String platformProtocol : platformProtocols) {
            try {
                Class<?> clazz = Class.forName(platformProtocol);
                PlatformEncDec protocol = (PlatformEncDec) clazz.newInstance();
                platformEncDecMap.put(protocol.getId(), protocol);
                LOG.info("Successfully initialized platform protocol {}", platformProtocol);
            } catch (ReflectiveOperationException e) {
                LOG.error("Error during instantiation of platform protocol", e);
            }
        }
        return platformEncDecMap;
    }

    void decodeAndForward(ActorContext context, SessionInitRequest message) {
        try {
            sessionInitMeter.mark();
            processSignedRequest(context, message);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void decodeAndForward(ActorContext context, SessionAwareRequest message) {
        try {
            sessionRequestMeter.mark();
            processSessionRequest(context, message);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void encodeAndReply(SessionResponse message) {
        try {
            sessionResponseMeter.mark();
            processSessionResponse(message);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorConverter(), e);
        }
    }

    public void forward(ActorContext context, SessionAware message) {
        LOG.debug("Forwarding session aware message: {}", message);
        this.opsActor.tell(message, context.self());
    }

    void redirect(RedirectionRule redirection, SessionInitRequest message) {
        try {
            redirectMeter.mark();
            ClientSync request = decodeRequest(message);
            ServerSync response = buildRedirectionResponse(redirection, request);

            EndpointObjectHash key = getEndpointObjectHash(request);
            String appToken = getAppToken(request);
            NettySessionInfo sessionInfo = new NettySessionInfo(message.getChannelUuid(), message.getPlatformId(),
                    message.getChannelContext(), message.getChannelType(), crypt.getSessionCipherPair(), key, appToken,
                    message.getKeepAlive(), message.isEncrypted());
            SessionResponse responseMessage = new NettySessionResponseMessage(sessionInfo, response, message.getResponseBuilder(),
                    message.getErrorBuilder());
            LOG.debug("Redirect Response: {}", response);
            processSessionResponse(responseMessage);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void redirect(RedirectionRule redirection, SessionAwareRequest message) {
        try {
            redirectMeter.mark();
            ClientSync request = decodeRequest(message);
            ServerSync response = buildRedirectionResponse(redirection, request);

            NettySessionInfo sessionInfo = message.getSessionInfo();
            SessionResponse responseMessage = new NettySessionResponseMessage(sessionInfo, response, message.getResponseBuilder(),
                    message.getErrorBuilder());
            LOG.debug("Redirect Response: {}", response);
            processSessionResponse(responseMessage);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    private ServerSync buildRedirectionResponse(RedirectionRule redirection, ClientSync request) {
        RedirectServerSync redirectSyncResponse = new RedirectServerSync(redirection.getDnsName());
        ServerSync response = new ServerSync();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncStatus.REDIRECT);
        response.setRedirectSync(redirectSyncResponse);
        return response;
    }

    private void processSignedRequest(ActorContext context, SessionInitRequest message) throws GeneralSecurityException,
            PlatformEncDecException {
        ClientSync request = decodeRequest(message);
        EndpointObjectHash key = getEndpointObjectHash(request);
        NettySessionInfo session = new NettySessionInfo(message.getChannelUuid(), message.getPlatformId(), message.getChannelContext(),
                message.getChannelType(), crypt.getSessionCipherPair(), key, request.getClientSyncMetaData().getApplicationToken(),
                message.getKeepAlive(), message.isEncrypted());
        message.onSessionCreated(session);
        forwardToOpsActor(context, session, request, message);
    }

    private void processSessionRequest(ActorContext context, SessionAwareRequest message) throws GeneralSecurityException,
            PlatformEncDecException {
        ClientSync request = decodeRequest(message);
        forwardToOpsActor(context, message.getSessionInfo(), request, message);
    }

    private void forwardToOpsActor(ActorContext context, NettySessionInfo session, ClientSync request, Request requestMessage) {
        SyncRequestMessage message = new SyncRequestMessage(session, request, requestMessage, context.self());
        this.opsActor.tell(message, context.self());
    }

    private void processSessionResponse(SessionResponse message) throws GeneralSecurityException, PlatformEncDecException {
        NettySessionInfo session = message.getSessionInfo();

        byte[] responseData = encodePlatformLevelData(message.getPlatformId(), message);
        LOG.trace("Response data serialized");
        if (session.isEncrypted()) {
            crypt.setSessionCipherPair(session.getCipherPair());
            responseData = crypt.encodeData(responseData);
            LOG.trace("Response data crypted");
        }
        ChannelHandlerContext context = message.getSessionInfo().getCtx();
        ResponseBuilder converter = message.getResponseConverter();
        Object[] objects = converter.build(responseData, session.isEncrypted());
        if (objects != null && objects.length > 0) {
            for (Object object : objects) {
                context.write(object);
            }
            context.flush();
        }
    }

    private ClientSync decodeRequest(SessionInitRequest message) throws GeneralSecurityException, PlatformEncDecException {
        ClientSync syncRequest = null;
        if (message.isEncrypted()) {
            syncRequest = decodeEncryptedRequest(message);
        } else if (supportUnencryptedConnection) {
            syncRequest = decodeUnencryptedRequest(message);
        } else {
            LOG.warn("Received unencripted init message, but unencrypted connection forbidden by configuration.");
            throw new GeneralSecurityException("Unencrypted connection forbidden by configuration.");
        }
        return syncRequest;
    }

    private ClientSync decodeEncryptedRequest(SessionInitRequest message) throws GeneralSecurityException, PlatformEncDecException {
        byte[] requestRaw = crypt.decodeData(message.getEncodedRequestData(), message.getEncodedSessionKey());
        LOG.trace("Request data decrypted");
        ClientSync request = decodePlatformLevelData(message.getPlatformId(), requestRaw);
        LOG.trace("Request data deserialized");
        PublicKey endpointKey = getPublicKey(request);
        if (endpointKey == null) {
            LOG.warn("Endpoint Key is null");
            throw new GeneralSecurityException("Endpoint Key is null");
        } else {
            LOG.trace("Public key extracted");
        }
        crypt.setRemotePublicKey(endpointKey);
        if (crypt.verify(message.getEncodedSessionKey(), message.getSessionKeySignature())) {
            LOG.trace("Request data verified");
        } else {
            LOG.warn("Request data verification failed");
            throw new GeneralSecurityException("Request data verification failed");
        }
        return request;
    }

    private ClientSync decodeUnencryptedRequest(SessionInitRequest message) throws GeneralSecurityException, PlatformEncDecException {
        byte[] requestRaw = message.getEncodedRequestData();
        LOG.trace("Try to convert raw data to SynRequest object");
        ClientSync request = decodePlatformLevelData(message.getPlatformId(), requestRaw);
        LOG.trace("Request data deserialized");
        PublicKey endpointKey = getPublicKey(request);
        if (endpointKey == null) {
            LOG.warn("Endpoint Key is null");
            throw new GeneralSecurityException("Endpoint Key is null");
        } else {
            LOG.trace("Public key extracted");
        }
        return request;
    }

    private ClientSync decodeEncryptedRequest(SessionAwareRequest message) throws GeneralSecurityException, PlatformEncDecException {
        NettySessionInfo session = message.getSessionInfo();
        crypt.setSessionCipherPair(session.getCipherPair());
        byte[] requestRaw = crypt.decodeData(message.getEncodedRequestData());
        LOG.trace("Request data decrypted");
        ClientSync request = decodePlatformLevelData(message.getPlatformId(), requestRaw);
        LOG.trace("Request data deserialized");
        return request;
    }

    private ClientSync decodeUnencryptedRequest(SessionAwareRequest message) throws PlatformEncDecException {
        byte[] requestRaw = message.getEncodedRequestData();
        ClientSync request = decodePlatformLevelData(message.getPlatformId(), requestRaw);
        LOG.trace("Request data deserialized");
        return request;
    }

    private byte[] encodePlatformLevelData(int platformID, SessionResponse message) throws PlatformEncDecException {
        PlatformEncDec encDec = platformEncDecMap.get(platformID);
        if (encDec != null) {
            return platformEncDecMap.get(platformID).encode(message.getResponse());
        } else {
            throw new PlatformEncDecException(MessageFormat.format("Encoder for platform protocol [{0}] is not defined", platformID));
        }
    }

    private ClientSync decodePlatformLevelData(Integer platformID, byte[] requestRaw) throws PlatformEncDecException {
        PlatformEncDec encDec = platformEncDecMap.get(platformID);
        if (encDec != null) {
            return platformEncDecMap.get(platformID).decode(requestRaw);
        } else {
            throw new PlatformEncDecException(MessageFormat.format("Decoder for platform protocol [{0}] is not defined", platformID));
        }
    }

    private ClientSync decodeRequest(SessionAwareRequest message) throws GeneralSecurityException, PlatformEncDecException {
        ClientSync syncRequest = null;
        if (message.isEncrypted()) {
            syncRequest = decodeEncryptedRequest(message);
        } else if (supportUnencryptedConnection) {
            syncRequest = decodeUnencryptedRequest(message);
        } else {
            LOG.warn("Received unencrypted aware message, but unencrypted connection forbidden by configuration.");
            throw new GeneralSecurityException("Unencrypted connection forbidden by configuration.");
        }
        return syncRequest;
    }

    private PublicKey getPublicKey(ClientSync request) throws GeneralSecurityException {
        PublicKey endpointKey = null;
        if (request.getProfileSync() != null && request.getProfileSync().getEndpointPublicKey() != null) {
            byte[] publicKeySrc = request.getProfileSync().getEndpointPublicKey().array();
            endpointKey = KeyUtil.getPublic(publicKeySrc);
        }
        if (endpointKey == null) {
            EndpointObjectHash hash = getEndpointObjectHash(request);
            endpointKey = cacheService.getEndpointKey(hash);
        }
        return endpointKey;
    }

    private void processErrors(ChannelHandlerContext ctx, ErrorBuilder converter, Exception e) {
        LOG.trace("Request processing failed", e);
        errorMeter.mark();
        Object[] responses = converter.build(e);
        if (responses != null && responses.length > 0) {
            for (Object response : responses) {
                ctx.writeAndFlush(response);
            }
        } else {
            ctx.fireExceptionCaught(e);
        }
    }

    private String getAppToken(ClientSync request) {
        return request.getClientSyncMetaData().getApplicationToken();
    }

    protected EndpointObjectHash getEndpointObjectHash(ClientSync request) {
        return EndpointObjectHash.fromBytes(request.getClientSyncMetaData().getEndpointPublicKeyHash().array());
    }
}
