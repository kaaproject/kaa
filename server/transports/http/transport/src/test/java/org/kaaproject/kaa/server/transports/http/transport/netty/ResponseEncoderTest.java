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
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.AbstractNettyServer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ResponseEncoderTest {
    private ResponseEncoder responseEncoder = new ResponseEncoder();
    private ChannelHandlerContext channelHandlerContext = mock(ChannelHandlerContext.class);
    private AbstractCommand abstractCommand = mock(AbstractCommand.class);
    private ChannelPromise promise = mock(ChannelPromise.class);
    private HttpResponse response = mock(HttpResponse.class);

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        Channel channel = mock(Channel.class);
        Attribute attribute = mock(Attribute.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(response.headers()).thenReturn(headers);
        when(response.getProtocolVersion()).thenReturn(new HttpVersion("HTTP/1.1", true));
        when(channelHandlerContext.channel()).thenReturn(channel);
        when(headers.get(any(CharSequence.class))).thenReturn(null);
        when(channel.attr(AbstractNettyServer.UUID_KEY)).thenReturn(attribute);
        when(abstractCommand.getResponse()).thenReturn(response);
    }

    @Test
    public void validHttpResponseWriteTest() throws Exception {
        responseEncoder.write(channelHandlerContext, abstractCommand, promise);
        verify(channelHandlerContext).writeAndFlush(response, promise);
    }

    @Test
    public void invalidHttpResponseWriteTest() throws Exception {
        responseEncoder.write(channelHandlerContext, new Object(), promise);
        verify(channelHandlerContext, never()).writeAndFlush(response, promise);
    }
}
