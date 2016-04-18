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
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;

import java.nio.ByteBuffer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class KaaTcpEncoderTest {
    private KaaTcpEncoder encoder = new KaaTcpEncoder();
    private ChannelHandlerContext ctx;
    private ChannelPromise promise;
    private ChannelFuture future;

    @Before
    public void setUp() {
        ctx = mock(ChannelHandlerContext.class);
        future = mock(ChannelFuture.class);
        when(ctx.writeAndFlush(any(Object.class), any(ChannelPromise.class))).thenReturn(future);
        promise = mock(ChannelPromise.class);
    }

    @Test
    public void incorrectMessageWriteTest() throws Exception {
        Object msg = new Object();
        encoder.write(ctx, msg, promise);
        verify(ctx, never()).writeAndFlush(any(Object.class), any(ChannelPromise.class));
    }

    @Test
    public void writeMessageNoCloseConnectionTest() throws Exception {
        MqttFrame msg = createMqttFrameMock(false);
        encoder.write(ctx, msg, promise);
        verify(future, never()).addListener(ChannelFutureListener.CLOSE);
    }

    @Test
    public void writeMessageAndCloseConnectionTest() throws Exception {
        MqttFrame msg = createMqttFrameMock(true);
        encoder.write(ctx, msg, promise);
        verify(future).addListener(ChannelFutureListener.CLOSE);
    }

    private MqttFrame createMqttFrameMock(boolean closeConnection) {
        MqttFrame msg = mock(MqttFrame.class);
        when(msg.getFrame()).thenReturn(ByteBuffer.allocate(10));
        when(msg.isNeedCloseConnection()).thenReturn(closeConnection);
        return msg;
    }
}
