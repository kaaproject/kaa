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

/**
 *
 */
package org.kaaproject.kaa.server.common.http.server;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.kaaproject.kaa.common.Constants;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.kaaproject.kaa.server.common.server.http.AbstractCommand;

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

/**
 * @author Andrey Panasenko
 */
public class CommandTestProcessor extends AbstractCommand {

    protected static String TEST_COMMAND_NAME = "testCommand";
    public static String COMMAND_NAME = TEST_COMMAND_NAME;

    private String id;
    private String requestRandom;
    private String requestBody;
    private String responseBody;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor#parse()
     */
    @Override
    public void parse() throws Exception {
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(factory, getRequest());
        InterfaceHttpData data = decoder.getBodyHttpData(NettyHttpServerIT.REQUEST_ID);
        if (data != null) {
            Attribute attribute = (Attribute) data;
            id = new String(attribute.get());
        } else {
            LOG.error("HTTP Resolve request inccorect, " + NettyHttpServerIT.REQUEST_ID + "  attribute not found");
            throw new BadRequestException("HTTP Resolve request inccorect, " + NettyHttpServerIT.REQUEST_ID + " attribute not found");
        }
        data = decoder.getBodyHttpData(NettyHttpServerIT.REQUEST_RANDOM);
        if (data != null) {
            Attribute attribute = (Attribute) data;
            requestRandom = new String(attribute.get());
        } else {
            LOG.error("HTTP Resolve request inccorect, " + NettyHttpServerIT.REQUEST_ID + "  attribute not found");
            throw new BadRequestException("HTTP Resolve request inccorect, " + NettyHttpServerIT.REQUEST_ID + " attribute not found");
        }
        data = decoder.getBodyHttpData(NettyHttpServerIT.REQUEST_DATA);
        if (data != null) {
            Attribute attribute = (Attribute) data;
            requestBody = new String(attribute.get());
        } else {
            LOG.error("HTTP Resolve request inccorect, " + NettyHttpServerIT.REQUEST_ID + "  attribute not found");
            throw new BadRequestException("HTTP Resolve request inccorect, " + NettyHttpServerIT.REQUEST_ID + " attribute not found");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor#process()
     */
    @Override
    public void process() throws Exception {
        responseBody = NettyHttpServerIT.getRandomString(NettyHttpServerIT.MAX_RESPONSE_BODY);
        NettyHttpServerIT.setHttpResponseData(id, requestBody, responseBody);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.kaaproject.kaa.server.common.http.server.CommandProcessor#getHttpResponse
     * ()
     */
    @Override
    public HttpResponse getResponse() {
        byte[] responseBodyBytes = responseBody.getBytes();
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer(responseBodyBytes));
        httpResponse.headers().set(NettyHttpServerIT.REQUEST_ID, id);
        httpResponse.headers().set(NettyHttpServerIT.REQUEST_RANDOM, requestRandom);
        httpResponse.headers().set(CONTENT_TYPE, "application/x-ssss");

        httpResponse.headers().set(CONTENT_LENGTH, httpResponse.content().readableBytes());
        if (isNeedConnectionClose()) {
            httpResponse.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);
        }
        return httpResponse;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kaaproject.kaa.server.common.http.server.CommandProcessor#
     * isNeedConnectionClose()
     */
    @Override
    public boolean isNeedConnectionClose() {
        return true;
    }

    public static String getCommandName() {
        return TEST_COMMAND_NAME;
    }

    @Override
    public int getNextProtocol() {
        return Constants.KAA_PLATFORM_PROTOCOL_AVRO_ID;
    }
}
