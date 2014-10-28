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

package org.kaaproject.kaa.server.bootstrap.service.http.commands;

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

import org.apache.commons.codec.binary.Base64;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.kaaproject.kaa.server.common.server.http.CommandProcessor;

/**
 * ResolveCommand Class.
 * ResolveCommand - process bootstrap resolve request and return Operations Server List.
 * Bootstrap server is responsible for distribution of endpoint components across different
 * operations servers. Each endpoint component integrated into client application have predefined
 * list of bootstrap servers which will be used to discover available operations servers with
 * security credentials and priorities. Every bootstrap server build list of available
 * operations servers using ZooKepper service. Bootstrap server also analyze KPI reports from
 * Operations server nodes. This information is used by to detect less loaded Operations Server nodes and
 * rebalance the cluster in runtime. This is extremely useful when new Operations server nodes are added.
 *
 * Incoming attributes:
 * Application Token - integer - represent client application.
 * Operations Server list:
 * Field Name      Description
 * OperationsServer  OperationsServer DNS name
 * Priority        Number  Priority of Operations Server, less number higher priority
 * PublicKey       RSA Public Key  RSA Public Key with 2048bit length of Operations Server
 *
 * @author Andrey Panasenko
 *
 */
public class ResolveCommand extends CommandProcessor implements CommonBSConstants {

    static {
        COMMAND_NAME = BOOTSTRAP_RESOLVE_COMMAND;
        LOG.info("CommandName: " + COMMAND_NAME);
    }

    protected byte[] responseBody;
    protected byte[] requestData;
    protected byte[] responseSignature;
    protected String applicationToken;

    protected OperationsServerListService operationsServerListService;
    protected MessageEncoderDecoder crypt;

    protected AvroByteArrayConverter<Resolve> requestConverter;
    protected AvroByteArrayConverter<OperationsServerList> responseConverter;
    
    protected long processingStartTimestamp = 0;

    /**
     * Default constructor ResolveCommand()
     */
    public ResolveCommand() {
        super();
        requestConverter = new AvroByteArrayConverter<Resolve>(Resolve.class);
        responseConverter = new AvroByteArrayConverter<OperationsServerList>(OperationsServerList.class);
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#parse()
     */
    @Override
    public void parse() throws Exception {
        processingStartTimestamp = System.currentTimeMillis();
        LOG.trace("CommandName {}: parse", COMMAND_NAME);
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, getRequest());
        InterfaceHttpData data = decoder.getBodyHttpData(APPLICATION_TOKEN_ATTR_NAME);
        if (data != null) {
            Attribute attribute = (Attribute) data;
            requestData = attribute.get();
            LOG.trace("Name {}, type {} found, data size {}", data.getName(), data.getHttpDataType().name(), requestData.length);
        } else {
            LOG.error("HTTP Resolve request inccorect, {} attribute not found", APPLICATION_TOKEN_ATTR_NAME);
            throw new BadRequestException("HTTP Resolve request inccorect, " +
                    APPLICATION_TOKEN_ATTR_NAME +" attribute not found");
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#process()
     */
    @Override
    public void process() throws Exception {
        LOG.trace("CommandName {}: process...", COMMAND_NAME);
        if (requestData != null && requestData.length > 0) {
            Resolve r = requestConverter.fromByteArray(requestData);
            LOG.trace("CommandName {}, application token {}", COMMAND_NAME, r.getApplicationToken());
            applicationToken = r.getApplicationToken();
            OperationsServerList serverList  = operationsServerListService.getOpsServerList();
            responseBody = responseConverter.toByteArray(serverList);
            responseSignature = crypt.sign(responseBody);
            LOG.trace("CommandName {} response signed", COMMAND_NAME);
        }
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#getHttpResponse()
     */
    @Override
    public HttpResponse getResponse() {
//        logger.debug("Public Key: {}" + crypt.getPublicKey().getEncoded().length);
//        logger.trace(MessageEncoderDecoder.bytesToHex(crypt.getPublicKey().getEncoded()));
//        logger.debug("Signature size: {}" + responseSignature.length);
//        logger.trace(MessageEncoderDecoder.bytesToHex(responseSignature));
//        logger.debug("Body size: {}" + responseBody.length);
//        logger.trace(MessageEncoderDecoder.bytesToHex(responseBody));

        String signatureResponse = Base64.encodeBase64String(responseSignature);

        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(responseBody));

        httpResponse.headers().set(CONTENT_TYPE, CommonBSConstants.RESPONSE_CONTENT_TYPE);
        httpResponse.headers().set(CommonBSConstants.SIGNATURE_HEADER_NAME, signatureResponse);
        httpResponse.headers().set(CommonBSConstants.RESPONSE_TYPE, CommonBSConstants.RESPONSE_TYPE_BOOTSTRAP);
        httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
        if (isNeedConnectionClose()) {
            httpResponse.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
        }
        setSyncTime(System.currentTimeMillis() - processingStartTimestamp);
        return httpResponse;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#isNeedConnectionClose()
     */
    @Override
    public boolean isNeedConnectionClose() {
        return true;
    }

    /**
     * Return command name
     * @return String - command name
     */
    public static String getCommandName() {
        return COMMAND_NAME;
    }
}
