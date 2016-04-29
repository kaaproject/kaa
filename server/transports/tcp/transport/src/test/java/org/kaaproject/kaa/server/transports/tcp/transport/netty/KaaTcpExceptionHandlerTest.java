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

package org.kaaproject.kaa.server.transports.tcp.transport.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.server.common.server.BadRequestException;
import org.mockito.ArgumentCaptor;


import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KaaTcpExceptionHandlerTest {
    private KaaTcpExceptionHandler kaaTcpExceptionHandler = new KaaTcpExceptionHandler();
    private ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

    @Before
    public void before() {
        ChannelFuture future = mock(ChannelFuture.class);
        when(ctx.writeAndFlush(any(Object.class))).thenReturn(future);
    }

    @Test
    public void badRequestExceptionTest() throws Exception {
        kaaTcpExceptionHandler.exceptionCaught(ctx, new BadRequestException("Bad request"));
        verify(ctx).writeAndFlush(any(Object.class));
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(ctx).writeAndFlush(argumentCaptor.capture());
        Assert.assertTrue(Arrays.equals(argumentCaptor.getValue(), getMessageByteArrayForReason(DisconnectReason.BAD_REQUEST)));
        verify(ctx).close();
    }

    @Test
    public void internalErrorExceptionTest() throws  Exception {
        kaaTcpExceptionHandler.exceptionCaught(ctx, new Exception("Internal error occurred"));
        verify(ctx).writeAndFlush(any(Object.class));
        ArgumentCaptor<byte[]> argumentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(ctx).writeAndFlush(argumentCaptor.capture());
        Assert.assertTrue(Arrays.equals(argumentCaptor.getValue(), getMessageByteArrayForReason(DisconnectReason.INTERNAL_ERROR)));
        verify(ctx).close();
    }

    private byte[] getMessageByteArrayForReason(DisconnectReason reason) {
        return new Disconnect(reason).getFrame().array();
    }
}
