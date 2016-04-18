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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultExceptionHandlerTest {
    private ChannelHandlerContext ctx;
    private DefaultExceptionHandler defaultExceptionHandler;

    @Before
    public void before() {
        ctx = mock(ChannelHandlerContext.class);
        ChannelFuture channelFuture = mock(ChannelFuture.class);
        when(ctx.writeAndFlush(any(FullHttpResponse.class))).thenReturn(channelFuture);
        defaultExceptionHandler = new DefaultExceptionHandler();
    }

    @Test
    public void badRequestExceptionCaught() throws Exception {
        defaultExceptionHandler.exceptionCaught(ctx, new BadRequestException("Bad request"));
        ArgumentCaptor<FullHttpResponse> response = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(ctx).writeAndFlush(response.capture());
        Assert.assertEquals(response.getValue().getStatus(), HttpResponseStatus.BAD_REQUEST);
    }

    @Test
    public void nonBadRequestExceptionCaught() throws Exception {
        defaultExceptionHandler.exceptionCaught(ctx, new Exception("Non-bad request"));
        ArgumentCaptor<FullHttpResponse> response = ArgumentCaptor.forClass(FullHttpResponse.class);
        verify(ctx).writeAndFlush(response.capture());
        Assert.assertEquals(response.getValue().getStatus(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
}
