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

package org.kaaproject.kaa.server.operations.service.http.commands;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.server.common.http.server.BadRequestException;
import org.kaaproject.kaa.server.common.http.server.CommandProcessor;
import org.kaaproject.kaa.server.common.http.server.NettyHttpServer;
import org.kaaproject.kaa.server.operations.pojo.exceptions.GetDeltaException;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.config.NettyHttpServiceChannelConfig;
import org.kaaproject.kaa.server.operations.service.config.OperationsServerConfig;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;


/**
 * The Class AbstractOperationsCommand.
 *
 * @param <T>
 *            the generic type
 * @param <R>
 *            the generic type
 */
public abstract class AbstractOperationsCommand<T extends SpecificRecordBase, R extends SpecificRecordBase>
        extends CommandProcessor {

    /** The signature. */
    protected byte[] signature;

    /** The requestkey. */
    protected byte[] requestkey;

    /** The request data. */
    protected byte[] requestData;

    /** The response body. */
    protected byte[] responseBody;

    /** The response signature. */
    protected byte[] responseSignature;

    /** The server public. */
    protected PublicKey serverPublic;

    /** The server private. */
    protected PrivateKey serverPrivate;

    /** The crypt. */
    protected MessageEncoderDecoder crypt;

    /** The conf. */
    protected OperationsServerConfig conf;

    /** The operations service. */
    protected OperationsService operationsService;

    /** The cache service. */
    protected CacheService cacheService;

    /** The request converter. */
    protected AvroByteArrayConverter<T> requestConverter;

    /** The response converter. */
    protected AvroByteArrayConverter<R> responseConverter;

    /** The request. */
    protected T request;

    /** The response. */
    protected R response;

    /**
     * Gets the request converter class.
     *
     * @return the request converter class
     */
    protected abstract Class<T> getRequestConverterClass();

    /**
     * Gets the response converter class.
     *
     * @return the response converter class
     */
    protected abstract Class<R> getResponseConverterClass();

    /**
     * Gets the type of channel that issued this command.
     *
     * @return the response converter class
     */
    public abstract ChannelType getChannelType();

    /**
     * Gets the public key.
     *
     * @param request
     *            the request
     * @return the public key
     * @throws GeneralSecurityException
     *             the general security exception
     */
    protected abstract PublicKey getPublicKey(T request)
            throws GeneralSecurityException;

    /**
     * Process parsed request.
     *
     * @param epRequest
     *            the ep request
     * @return the r
     * @throws GetDeltaException
     *             the get delta exception
     */
    protected abstract R processParsedRequest(T epRequest)
            throws GetDeltaException;

    /**
     * Instantiates a new abstract operations command.
     */
    public AbstractOperationsCommand() {
        super();
        requestConverter = new AvroByteArrayConverter<T>(
                getRequestConverterClass());
        responseConverter = new AvroByteArrayConverter<R>(
                getResponseConverterClass());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor#setServer
     * (org.kaaproject.kaa.server.common.http.server.NettyHttpServer)
     */
    @Override
    public void setServer(NettyHttpServer server) {
        super.setServer(server);
        conf = ((NettyHttpServiceChannelConfig) getServer().getConf()).getOperationServerConfig();
        operationsService = conf.getOperationsBootstrapService()
                .getOperationsService();
        cacheService = conf.getOperationsBootstrapService().getCacheService();

        KeyStoreService keyStoreService = conf.getOperationsBootstrapService()
                .getKeyStoreService();
        serverPrivate = keyStoreService.getPrivateKey();
        serverPublic = keyStoreService.getPublicKey();
        crypt = new MessageEncoderDecoder(serverPrivate, serverPublic, null);
        LOG.trace("CommandName: " + COMMAND_NAME
                + ": ..keyStoreService set.");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor#parse()
     */
    @Override
    public void parse() throws Exception {
        LOG.trace("CommandName: " + COMMAND_NAME + ": Parse..");
        HttpDataFactory factory = new DefaultHttpDataFactory(
                DefaultHttpDataFactory.MINSIZE);
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory,
                getHttpRequest());
        if (decoder.isMultipart()) {
            LOG.trace("Chunked: "
                    + HttpHeaders.isTransferEncodingChunked(getHttpRequest()));
            LOG.trace(": Multipart..");
            List<InterfaceHttpData> datas = decoder.getBodyHttpDatas();
            if (!datas.isEmpty()) {
                for (InterfaceHttpData data : datas) {
                    LOG.trace("Multipart1 name " + data.getName() + " type "
                            + data.getHttpDataType().name());
                    if (data.getHttpDataType() == HttpDataType.Attribute) {
                        Attribute attribute = (Attribute) data;
                        if (data.getName().equals(
                                CommonEPConstans.REQUEST_SIGNATURE_ATTR_NAME)) {
                            signature = attribute.get();
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Multipart name " + data.getName()
                                        + " type "
                                        + data.getHttpDataType().name()
                                        + " Signature set. size: "
                                        + signature.length);
                                LOG.trace(MessageEncoderDecoder
                                        .bytesToHex(signature));
                            }

                        } else if (data.getName().equals(
                                CommonEPConstans.REQUEST_KEY_ATTR_NAME)) {
                            requestkey = attribute.get();
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Multipart name " + data.getName()
                                        + " type "
                                        + data.getHttpDataType().name()
                                        + " requestKey set. size: "
                                        + requestkey.length);
                                LOG.trace(MessageEncoderDecoder
                                        .bytesToHex(requestkey));
                            }
                        } else if (data.getName().equals(
                                CommonEPConstans.REQUEST_DATA_ATTR_NAME)) {
                            requestData = attribute.get();
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Multipart name " + data.getName()
                                        + " type "
                                        + data.getHttpDataType().name()
                                        + " requestData set. size: "
                                        + requestData.length);
                                LOG.trace(MessageEncoderDecoder
                                        .bytesToHex(requestData));
                            }
                        }
                    }
                }
            } else {
                LOG.error("Multipart.. size 0");
                throw new BadRequestException(
                        "HTTP Request inccorect, multiprat size is 0");
            }
        }
    }

    private static byte[] crcrlfFix(byte[] data){
        if(data.length >= 2 && data[data.length-1] == 0x0D &&  data[data.length-2] == 0x0D){
            return crcrlfFix(Arrays.copyOf(data, data.length - 1));
        }else{
            return data;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor#Process ()
     */
    @Override
    public void process() throws BadRequestException, GeneralSecurityException,
            IOException, GetDeltaException {
        decode();

        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. EndpointService resolved");
        response = processParsedRequest(request);
        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. DB request processed");

        encode(response);
    }

    /**
     * Decode.
     *
     * @return the t
     * @throws BadRequestException the bad request exception
     * @throws GeneralSecurityException the general security exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public T decode() throws BadRequestException, GeneralSecurityException,
            IOException {
        LOG.trace("CommandName: " + COMMAND_NAME + ": Process..");
        if (signature == null || requestkey == null || requestData == null) {
            throw new BadRequestException(
                    "Incorrect NewEndpointRegistration request");
        }

        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. signature, requestkey and requestData fild....");

        byte[] data = crypt.decodeData(requestData, requestkey);
        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. data decrypted");
        request = requestConverter.fromByteArray(data);
        PublicKey endpointKey = getPublicKey(request);
        if(endpointKey == null){
            LOG.trace("Endpoint Key is null!");
            throw new BadRequestException("Endpoint key is null!");
        }
        crypt.setRemotePublicKey(endpointKey);
        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. RemotePublicKey extracted");

        if (crypt.verify(requestData, signature)) {
            LOG.trace("CommandName: " + COMMAND_NAME
                    + ": Process.. signature verified");
        } else {
            throw new BadRequestException("Data verification failed");
        }

        return request;
    }

    /**
     * Encode.
     *
     * @param response the response
     * @throws GeneralSecurityException the general security exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void encode(R response) throws GeneralSecurityException, IOException {
        byte[] encodedReponse = responseConverter.toByteArray(response);

        responseBody = crypt.encodeData(encodedReponse);
        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. response encrypted");
        responseSignature = crypt.sign(responseBody);
        LOG.trace("CommandName: " + COMMAND_NAME
                + ": Process.. response signed");

        LOG.trace("CommandName: " + COMMAND_NAME + ": Process.. Complete");
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#
     * getHttpResponse()
     */
    @Override
    public HttpResponse getHttpResponse() {
        LOG.trace("CommandName: " + COMMAND_NAME + ": getHttpResponse..");

        String signatureResponse = Base64.encodeBase64String(responseSignature);

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1,
                OK, Unpooled.copiedBuffer(responseBody));

        httpResponse.headers().set(CONTENT_TYPE,
                CommonEPConstans.RESPONSE_CONTENT_TYPE);
        httpResponse.headers().set(CommonEPConstans.SIGNATURE_HEADER_NAME,
                signatureResponse);
        httpResponse.headers().set(CONTENT_LENGTH,
                httpResponse.content().readableBytes());
        httpResponse.headers().set(CommonEPConstans.RESPONSE_TYPE,
                CommonEPConstans.RESPONSE_TYPE_OPERATION);
        if (isNeedConnectionClose()) {
            httpResponse.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
        } else {
            if (HttpHeaders.isKeepAlive(getHttpRequest())) {
                httpResponse.headers().set(CONNECTION,
                        HttpHeaders.Values.KEEP_ALIVE);
            } else {
                httpResponse.headers()
                        .set(CONNECTION, HttpHeaders.Values.CLOSE);
            }
        }
        return httpResponse;
    }

    public static String getCommandName() {
        return COMMAND_NAME;
    }

}
