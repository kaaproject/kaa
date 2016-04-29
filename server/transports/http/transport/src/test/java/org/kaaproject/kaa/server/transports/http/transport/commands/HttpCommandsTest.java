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

package org.kaaproject.kaa.server.transports.http.transport.commands;

import io.netty.handler.codec.http.HttpRequest;
import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.server.transports.http.transport.messages.NettyHttpSyncMessage;
import org.kaaproject.kaa.server.transports.http.transport.netty.AbstractCommand;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.mock;

public class HttpCommandsTest {
    @Test
    public void abstractHttpSyncCommandGetSetTest() {
        AbstractHttpSyncCommand abstractHttpSyncCommand = mock(AbstractHttpSyncCommand.class, Mockito.CALLS_REAL_METHODS);

        byte[] responseBody = new byte[] {1, 2, 3, 4, 5};
        byte[] responseSignature = new byte[] {5, 6, 8, 9, 10};
        abstractHttpSyncCommand.setResponseBody(responseBody);
        Assert.assertArrayEquals(responseBody, abstractHttpSyncCommand.getResponseBody());
        abstractHttpSyncCommand.setResponseSignature(responseSignature);
        Assert.assertArrayEquals(responseSignature, abstractHttpSyncCommand.getResponseSignature());
    }

    @Test
    public void abstractCommandGetSetTest() {
        AbstractCommand abstractCommand = mock(AbstractCommand.class, Mockito.CALLS_REAL_METHODS);
        UUID uuid = UUID.randomUUID();
        abstractCommand.setSessionUuid(uuid);
        Assert.assertEquals(uuid, abstractCommand.getSessionUuid());
        long syncTime = 100342L;
        abstractCommand.setSyncTime(syncTime);
        Assert.assertEquals(syncTime, abstractCommand.getSyncTime());
        int commandId = 1;
        abstractCommand.setCommandId(commandId);
        Assert.assertEquals(commandId, abstractCommand.getCommandId());
        HttpRequest httpRequest = mock(HttpRequest.class);
        abstractCommand.setRequest(httpRequest);
        Assert.assertEquals(httpRequest, abstractCommand.getRequest());
    }

    @Test
    public void nettyHttpSyncMessageTest() {
        NettyHttpSyncMessage nettyHttpSyncMessage = mock(NettyHttpSyncMessage.class, Mockito.CALLS_REAL_METHODS);
        Assert.assertEquals(0, nettyHttpSyncMessage.getKeepAlive());
        Assert.assertTrue(nettyHttpSyncMessage.isEncrypted());
    }

    @Test
    public void longSyncCommandTest() {
        Assert.assertEquals(CommonEPConstans.LONG_SYNC_COMMAND, LongSyncCommand.getCommandName());
    }
}
