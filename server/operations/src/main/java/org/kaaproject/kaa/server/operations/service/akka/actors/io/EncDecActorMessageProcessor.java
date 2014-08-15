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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;

import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.RedirectSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
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

    private final AvroByteArrayConverter<SyncRequest> requestConverter;

    private final AvroByteArrayConverter<SyncResponse> responseConverter;

    /** The eps actor. */
    private final ActorRef opsActor;

    protected EncDecActorMessageProcessor(ActorRef epsActor, CacheService cacheService, KeyPair serverKeys) {
        super();
        this.opsActor = epsActor;
        this.cacheService = cacheService;
        this.crypt = new MessageEncoderDecoder(serverKeys.getPrivate(), serverKeys.getPublic());
        this.requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        this.responseConverter = new AvroByteArrayConverter<>(SyncResponse.class);
    }

    void decodeAndForward(ActorContext context, SessionInitRequest message) {
        try {
            processSignedRequest(context, message);
        } catch (GeneralSecurityException | IOException e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void decodeAndForward(ActorContext context, SessionAwareRequest message) {
        try {
            processSessionRequest(context, message);
        } catch (GeneralSecurityException | IOException e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void encodeAndReply(SessionResponse message) {
        try {
            processSessionResponse(message);
        } catch (GeneralSecurityException | IOException e) {
            processErrors(message.getChannelContext(), message.getErrorConverter(), e);
        }
    }

    public void forward(ActorContext context, SessionAware message) {
        LOG.debug("Forwarding session aware message: {}", message);
        this.opsActor.tell(message, context.self());
    }

    void redirect(RedirectionRule redirection, SessionInitRequest message) {
        try {
            SyncRequest request = decodeRequest(message);
            SyncResponse response = buildRedirectionResponse(redirection, request);

            EndpointObjectHash key = getEndpointObjectHash(request);
            String appToken = getAppToken(request);
            NettySessionInfo sessionInfo = new NettySessionInfo(message.getChannelUuid(), message.getChannelContext(), message.getChannelType(), crypt.getDecodedSessionKey(), key, appToken, message.getKeepAlive());
            SessionResponse responseMessage = new NettySessionResponseMessage(sessionInfo, response, message.getResponseBuilder(), message.getErrorBuilder());
            LOG.debug("Redirect Response: {}", response);
            processSessionResponse(responseMessage);
        } catch (GeneralSecurityException | IOException e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    void redirect(RedirectionRule redirection, SessionAwareRequest message) {
        try {
            SyncRequest request = decodeRequest(message);
            SyncResponse response = buildRedirectionResponse(redirection, request);

            NettySessionInfo sessionInfo = message.getSessionInfo();
            SessionResponse responseMessage = new NettySessionResponseMessage(sessionInfo, response, message.getResponseBuilder(), message.getErrorBuilder());
            LOG.debug("Redirect Response: {}", response);
            processSessionResponse(responseMessage);
        } catch (GeneralSecurityException | IOException e) {
            processErrors(message.getChannelContext(), message.getErrorBuilder(), e);
        }
    }

    private SyncResponse buildRedirectionResponse(RedirectionRule redirection, SyncRequest request) {
        RedirectSyncResponse redirectSyncResponse = new RedirectSyncResponse(redirection.getDnsName());
        SyncResponse response = new SyncResponse();
        response.setRequestId(request.getRequestId());
        response.setStatus(SyncResponseResultType.REDIRECT);
        response.setRedirectSyncResponse(redirectSyncResponse);
        return response;
    }

    private void processSignedRequest(ActorContext context, SessionInitRequest message) throws GeneralSecurityException,
            IOException {
        SyncRequest request = decodeRequest(message);
        EndpointObjectHash key = getEndpointObjectHash(request);
        NettySessionInfo session = new NettySessionInfo(message.getChannelUuid(), message.getChannelContext(), message.getChannelType(), crypt.getDecodedSessionKey(),
                key, request.getSyncRequestMetaData().getApplicationToken(), message.getKeepAlive());
        message.onSessionCreated(session);
        forwardToOpsActor(context, session, request, message);
    }

    private void processSessionRequest(ActorContext context, SessionAwareRequest message) throws GeneralSecurityException,
            IOException {
        SyncRequest request = decodeRequest(message);
        forwardToOpsActor(context, message.getSessionInfo(), request, message);
    }

    private void forwardToOpsActor(ActorContext context, NettySessionInfo session, SyncRequest request, Request requestMessage) {
        SyncRequestMessage message = new SyncRequestMessage(session, request, requestMessage, context.self());
        this.opsActor.tell(message, context.self());
    }

    private void processSessionResponse(SessionResponse message) throws GeneralSecurityException, IOException {
        NettySessionInfo session = message.getSessionInfo();
        crypt.setDecodedSessionKey(session.getSessionKey());
        byte[] response = responseConverter.toByteArray(message.getResponse());
        LOG.trace("Response data serialized");
        byte[] encodedResponse = crypt.encodeData(response);
        LOG.trace("Response data crypted");
        ChannelHandlerContext context = message.getSessionInfo().getCtx();
        ResponseBuilder converter = message.getResponseConverter();
        Object[] objects = converter.build(encodedResponse);
        if(objects != null && objects.length > 0){
            for(Object object : objects){
                context.write(object);
            }
            context.flush();
        }
    }

    private SyncRequest decodeRequest(SessionInitRequest message) throws GeneralSecurityException, IOException {
        byte[] requestRaw = crypt.decodeData(message.getEncodedRequestData(), message.getEncodedSessionKey());
        LOG.trace("Request data decrypted");
        SyncRequest request = requestConverter.fromByteArray(requestRaw);
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

    private SyncRequest decodeRequest(SessionAwareRequest message) throws GeneralSecurityException, IOException {
        NettySessionInfo session = message.getSessionInfo();
        crypt.setDecodedSessionKey(session.getSessionKey());
        byte[] requestRaw = crypt.decodeData(message.getEncodedRequestData());
        LOG.trace("Request data decrypted");
        SyncRequest request = requestConverter.fromByteArray(requestRaw);
        LOG.trace("Request data deserialized");
        return request;
    }

    private PublicKey getPublicKey(SyncRequest request) throws GeneralSecurityException {
        PublicKey endpointKey = null;
        if (request.getProfileSyncRequest() != null && request.getProfileSyncRequest().getEndpointPublicKey() != null) {
            byte[] publicKeySrc = request.getProfileSyncRequest().getEndpointPublicKey().array();
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
        Object[] responses = converter.build(e);
        if(responses != null && responses.length > 0){
            for(Object response : responses){
                ctx.writeAndFlush(response);
            }
        }else{
            ctx.fireExceptionCaught(e);
        }
    }

    private String getAppToken(SyncRequest request) {
        return request.getSyncRequestMetaData().getApplicationToken();
    }

    protected EndpointObjectHash getEndpointObjectHash(SyncRequest request) {
        return EndpointObjectHash.fromBytes(request.getSyncRequestMetaData().getEndpointPublicKeyHash().array());
    }
}
