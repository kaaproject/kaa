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

package org.kaaproject.kaa.server.transports.http.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.AbstractNettyServer;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.kaaproject.kaa.server.common.server.CommandFactory;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
        HttpObject httpObject = createHttpRequestMock(null, false, HttpRequest.class);
        requestDecoder.channelRead0(null, httpObject);
    }

    @Test
    public void httpPostRequestTest() throws Exception {
        HttpObject request = createHttpRequestMock(HttpMethod.POST, true, HttpRequest.class);
        requestDecoder.channelRead0(channelHandlerContext, request);
        verify(channelHandlerContext).fireChannelRead(any(Object.class));
    }

    @Test(expected = BadRequestException.class)
    public void invalidMethodRequestTest() throws Exception {
        HttpObject request = createHttpRequestMock(HttpMethod.GET, true, HttpRequest.class);
        requestDecoder.channelRead0(channelHandlerContext, request);
    }

    @Test
    public void nonHttpRequestObjectTest() throws Exception {
        HttpObject request = createHttpRequestMock(HttpMethod.POST, true, HttpObject.class);
        requestDecoder.channelRead0(channelHandlerContext, request);
        verify(channelHandlerContext, never()).fireChannelRead(any(Object.class));
    }

    private HttpObject createHttpRequestMock(HttpMethod httpMethod, boolean isSuccessfulResult, Class requestClazz) {
        DecoderResult result = mock(DecoderResult.class);
        when(result.isSuccess()).thenReturn(isSuccessfulResult);
        HttpObject httpRequest = (HttpObject) mock(requestClazz);
        when(httpRequest.getDecoderResult()).thenReturn(result);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.toString()).thenReturn("Some header");
        if (requestClazz.equals(HttpRequest.class)) {
            when(((HttpRequest) httpRequest).getMethod()).thenReturn(httpMethod);
            when(((HttpRequest) httpRequest).headers()).thenReturn(headers);
        }
        return httpRequest;
    }
}
