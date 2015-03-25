/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.server.transports.http.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.kaaproject.kaa.server.common.server.CommandFactory;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RequestDecoderTest {
    private static RequestDecoder requestDecoder;
    private ChannelHandlerContext channelHandlerContext;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        channelHandlerContext = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        Attribute attribute = mock(Attribute.class);
        UUID uuid = new UUID(234L, 1234L);
        when(attribute.get()).thenReturn(uuid);
        when(channel.attr(AbstractNettyServer.UUID_KEY)).thenReturn(attribute);
        when(channelHandlerContext.channel()).thenReturn(channel);
    }

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void before() throws Exception {
        CommandFactory commandFactory = mock(CommandFactory.class);
        AbstractCommand abstractCommand = mock(AbstractCommand.class);
        when(commandFactory.getCommandProcessor(any(String.class))).thenReturn(abstractCommand);
        requestDecoder = new RequestDecoder(commandFactory);
    }

    @Test(expected = BadRequestException.class)
    public void noSuccessResult() throws Exception {
        HttpObject httpObject = createHttpRequestMock(null, false);
        requestDecoder.channelRead0(null, httpObject);
    }

    @Test
    public void httpPostRequestTest() throws Exception {
        HttpRequest request = createHttpRequestMock(HttpMethod.POST, true);
        requestDecoder.channelRead0(channelHandlerContext, request);
        verify(channelHandlerContext).fireChannelRead(any(Object.class));
    }

    @Test(expected = BadRequestException.class)
    public void invalidMethodRequestTest() throws Exception {
        HttpRequest request = createHttpRequestMock(HttpMethod.GET, true);
        requestDecoder.channelRead0(channelHandlerContext, request);
    }

    private HttpRequest createHttpRequestMock(HttpMethod httpMethod, boolean isSuccessfulResult) {
        DecoderResult result = mock(DecoderResult.class);
        when(result.isSuccess()).thenReturn(isSuccessfulResult);
        HttpRequest httpRequest = mock(HttpRequest.class);
        when(httpRequest.getDecoderResult()).thenReturn(result);
        when(httpRequest.getMethod()).thenReturn(httpMethod);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.toString()).thenReturn("Some header");
        when(httpRequest.headers()).thenReturn(headers);
        return httpRequest;
    }
}
