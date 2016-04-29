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

package org.netty.http.server.common.server;

import io.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.kaaproject.kaa.server.common.server.NettyChannelContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NettyChannelContextTest {
    @Test
    public void getSetTest() {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        NettyChannelContext nettyChannelContext = new NettyChannelContext(context);
        Object o1 = new Object();
        nettyChannelContext.writeAndFlush(o1);
        verify(context).writeAndFlush(o1);
        Exception e = new Exception();
        nettyChannelContext.fireExceptionCaught(e);
        verify(context).fireExceptionCaught(e);
        Object o2 = new Object();
        nettyChannelContext.writeAndFlush(o2);
        verify(context).writeAndFlush(o2);
        nettyChannelContext.flush();
        verify(context).flush();
    }
}
