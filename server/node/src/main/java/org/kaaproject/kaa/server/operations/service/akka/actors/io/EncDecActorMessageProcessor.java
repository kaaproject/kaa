/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.akka.actors.io;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.SyncRequestMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.NettySessionResponseMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.response.SessionResponse;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.metrics.MeterClient;
import org.kaaproject.kaa.server.operations.service.metrics.MetricsService;
import org.kaaproject.kaa.server.sync.ClientSync;
import org.kaaproject.kaa.server.sync.ClientSyncMetaData;
import org.kaaproject.kaa.server.sync.RedirectServerSync;
import org.kaaproject.kaa.server.sync.ServerSync;
import org.kaaproject.kaa.server.sync.SyncStatus;
import org.kaaproject.kaa.server.sync.platform.PlatformEncDec;
import org.kaaproject.kaa.server.sync.platform.PlatformEncDecException;
import org.kaaproject.kaa.server.sync.platform.PlatformLookup;
import org.kaaproject.kaa.server.transport.InvalidSDKTokenException;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.message.ErrorBuilder;
import org.kaaproject.kaa.server.transport.message.Message;
import org.kaaproject.kaa.server.transport.message.MessageBuilder;
import org.kaaproject.kaa.server.transport.message.SessionAwareMessage;
import org.kaaproject.kaa.server.transport.message.SessionInitMessage;
import org.kaaproject.kaa.server.transport.session.SessionAware;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
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

    protected EncDecActorMessageProcessor(ActorRef epsActor, AkkaContext context, Set<String> platformProtocols) {
        super();
        this.opsActor = epsActor;
        this.cacheService = context.getCacheService();
        this.supportUnencryptedConnection = context.getSupportUnencryptedConnection();
        this.crypt = new MessageEncoderDecoder(context.getKeyStoreService().getPrivateKey(), context.getKeyStoreService().getPublicKey());
        this.platformEncDecMap = PlatformLookup.initPlatformProtocolMap(platformProtocols);
        MetricsService metricsService = context.getMetricsService();
        this.sessionInitMeter = metricsService.createMeter("sessionInitMeter", Thread.currentThread().getName());
        this.sessionRequestMeter = metricsService.createMeter("sessionRequestMeter", Thread.currentThread().getName());
        this.sessionResponseMeter = metricsService.createMeter("sessionResponseMeter", Thread.currentThread().getName());
        this.redirectMeter = metricsService.createMeter("redirectMeter", Thread.currentThread().getName());
        this.errorMeter = metricsService.createMeter("errorMeter", Thread.currentThread().getName());
    }

    void decodeAndForward(ActorContext context, SessionInitMessage message) {
        try {
            sessionInitMeter.mark();
            processSignedRequest(context, message);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void decodeAndForward(ActorContext context, SessionAwareMessage message) {
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
            if(message.getError() == null){
                processSessionResponse(message);
            }else{
                processErrors(message.getChannelContext(), message.getErrorBuilder(), message.getError());
            }
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    public void forward(ActorContext context, SessionAware message) {
        if (isSDKTokenValid(message.getSessionInfo().getSdkToken())) {
            LOG.debug("Forwarding session aware message: {}", message);
            this.opsActor.tell(message, context.self());
        } else {
            LOG.debug("Session aware message ignored. Reason: message {} has invalid sdk token", message);
        }
    }

    void redirect(RedirectionRule redirection, SessionInitMessage message) {
        try {
            redirectMeter.mark();
            ClientSync request = decodeRequest(message);
            ServerSync response = buildRedirectionResponse(redirection, request);
            EndpointObjectHash key = getEndpointObjectHash(request);
            String sdkToken = getSdkToken(request);
            String appToken = getAppToken(sdkToken);
            SessionInfo sessionInfo = new SessionInfo(message.getChannelUuid(), message.getPlatformId(), message.getChannelContext(),
                    message.getChannelType(), crypt.getSessionCipherPair(), key, appToken, sdkToken, message.getKeepAlive(), message.isEncrypted());
            SessionResponse responseMessage = new NettySessionResponseMessage(sessionInfo, response, message.getMessageBuilder(),
                    message.getErrorBuilder());
            LOG.debug("Redirect Response: {}", response);
            processSessionResponse(responseMessage);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void redirect(RedirectionRule redirection, SessionAwareMessage message) {
        try {
            LOG.trace("Redirecting {} SessionAwareMessage", message);
            redirectMeter.mark();
            ClientSync request = decodeRequest(message);
            ServerSync response = buildRedirectionResponse(redirection, request);
            SessionInfo sessionInfo = message.getSessionInfo();
            SessionResponse responseMessage = new NettySessionResponseMessage(sessionInfo, response, message.getMessageBuilder(),
                        message.getErrorBuilder());
            LOG.debug("Redirect Response: {}", response);
            processSessionResponse(responseMessage);
        } catch (Exception e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    private ServerSync buildRedirectionResponse(RedirectionRule redirection, ClientSync request) {
        RedirectServerSync redirectSyncResponse = new RedirectServerSync(redirection.getAccessPointId());
        ServerSync response = new ServerSync();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncStatus.REDIRECT);
        response.setRedirectSync(redirectSyncResponse);
        return response;
    }

    private void processSignedRequest(ActorContext context, SessionInitMessage message) throws GeneralSecurityException,
            PlatformEncDecException, InvalidSDKTokenException {
        ClientSync request = decodeRequest(message);
        EndpointObjectHash key = getEndpointObjectHash(request);
        String sdkToken = getSdkToken(request);
        if (isSDKTokenValid(sdkToken)) {
            String appToken = getAppToken(sdkToken);
            SessionInfo session = new SessionInfo(message.getChannelUuid(), message.getPlatformId(), message.getChannelContext(),
                    message.getChannelType(), crypt.getSessionCipherPair(), key, appToken, sdkToken,
                    message.getKeepAlive(), message.isEncrypted());
            message.onSessionCreated(session);
            forwardToOpsActor(context, session, request, message);
        } else {
            LOG.info("Invalid sdk token received: {}", sdkToken);
            throw new InvalidSDKTokenException();
        }
    }

    private void processSessionRequest(ActorContext context, SessionAwareMessage message) throws GeneralSecurityException,
            PlatformEncDecException, InvalidSDKTokenException {
        ClientSync request = decodeRequest(message);
        if (isSDKTokenValid(message.getSessionInfo().getSdkToken())) {
            forwardToOpsActor(context, message.getSessionInfo(), request, message);
        } else {
            LOG.info("Invalid sdk token received: {}", message.getSessionInfo().getSdkToken());
            throw new InvalidSDKTokenException();
        }
    }

    private void forwardToOpsActor(ActorContext context, SessionInfo session, ClientSync request, Message requestMessage) {
        SyncRequestMessage message = new SyncRequestMessage(session, request, requestMessage, context.self());
        this.opsActor.tell(message, context.self());
    }

    private void processSessionResponse(SessionResponse message) throws GeneralSecurityException, PlatformEncDecException {
        SessionInfo session = message.getSessionInfo();

        byte[] responseData = encodePlatformLevelData(message.getPlatformId(), message);
        LOG.trace("Response data serialized");
        if (session.isEncrypted()) {
            crypt.setSessionCipherPair(session.getCipherPair());
            responseData = crypt.encodeData(responseData);
            LOG.trace("Response data crypted");
        }
        ChannelContext context = message.getSessionInfo().getCtx();
        MessageBuilder converter = message.getMessageBuilder();
        Object[] objects = converter.build(responseData, session.isEncrypted());
        if (objects != null && objects.length > 0) {
            for (Object object : objects) {
                context.write(object);
            }
            context.flush();
        }
    }

    private ClientSync decodeRequest(SessionInitMessage message) throws GeneralSecurityException, PlatformEncDecException {
        ClientSync syncRequest = null;
        if (message.isEncrypted()) {
            syncRequest = decodeEncryptedRequest(message);
        } else if (supportUnencryptedConnection) {
            syncRequest = decodeUnencryptedRequest(message);
        } else {
            LOG.warn("Received unencrypted init message, but unencrypted connection forbidden by configuration.");
            throw new GeneralSecurityException("Unencrypted connection forbidden by configuration.");
        }
        return syncRequest;
    }

    private ClientSync decodeEncryptedRequest(SessionInitMessage message) throws GeneralSecurityException, PlatformEncDecException {
        byte[] requestRaw = crypt.decodeData(message.getEncodedMessageData(), message.getEncodedSessionKey());
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

    private ClientSync decodeUnencryptedRequest(SessionInitMessage message) throws GeneralSecurityException, PlatformEncDecException {
        byte[] requestRaw = message.getEncodedMessageData();
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

    private ClientSync decodeEncryptedRequest(SessionAwareMessage message) throws GeneralSecurityException, PlatformEncDecException {
        SessionInfo session = message.getSessionInfo();
        crypt.setSessionCipherPair(session.getCipherPair());
        byte[] requestRaw = crypt.decodeData(message.getEncodedMessageData());
        LOG.trace("Request data decrypted");
        ClientSync request = decodePlatformLevelData(message.getPlatformId(), requestRaw);
        LOG.trace("Request data deserialized");
        return request;
    }

    private ClientSync decodeUnencryptedRequest(SessionAwareMessage message) throws PlatformEncDecException {
        byte[] requestRaw = message.getEncodedMessageData();
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
            ClientSync syncRequest = platformEncDecMap.get(platformID).decode(requestRaw);
            addAppTokenToClientSyncMetaData(syncRequest.getClientSyncMetaData());
            return syncRequest;
        } else {
            throw new PlatformEncDecException(MessageFormat.format("Decoder for platform protocol [{0}] is not defined", platformID));
        }
    }

    private ClientSync decodeRequest(SessionAwareMessage message) throws GeneralSecurityException, PlatformEncDecException {
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

    private void processErrors(ChannelContext ctx, ErrorBuilder converter, Exception e) {
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

    private void addAppTokenToClientSyncMetaData(ClientSyncMetaData clientSyncMetaData) {
        clientSyncMetaData.setApplicationToken(getAppToken(clientSyncMetaData.getSdkToken()));
    }

    private String getSdkToken(ClientSync request) {
        return request.getClientSyncMetaData().getSdkToken();
    }

    private String getAppToken(String sdkToken) {
        return cacheService.getAppTokenBySdkToken(sdkToken);
    }

    private boolean isSDKTokenValid(String sdkToken) {
        return sdkToken != null && getAppToken(sdkToken) != null;
    }

    protected EndpointObjectHash getEndpointObjectHash(ClientSync request) {
        return EndpointObjectHash.fromBytes(request.getClientSyncMetaData().getEndpointPublicKeyHash().array());
    }
}
