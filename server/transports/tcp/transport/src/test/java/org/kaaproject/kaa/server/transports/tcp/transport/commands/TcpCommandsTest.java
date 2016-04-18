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

package org.kaaproject.kaa.server.transports.tcp.transport.commands;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MqttFrame;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.transport.channel.ChannelContext;
import org.kaaproject.kaa.server.transport.channel.ChannelType;
import org.kaaproject.kaa.server.transport.session.SessionInfo;
import org.kaaproject.kaa.server.transports.tcp.transport.messages.NettyTcpConnectMessage;
import org.kaaproject.kaa.server.transports.tcp.transport.messages.NettyTcpSyncMessage;

import java.util.UUID;

import static org.mockito.Mockito.mock;

public class TcpCommandsTest {
    @Test
    public void kaaTcpCommandTest() {
        KaaTcpCommandFactory commandFactory = new KaaTcpCommandFactory();
        KaaTcpCommand command = (KaaTcpCommand)commandFactory.createCommandProcessor();
        Assert.assertNotNull(command);
        Assert.assertEquals(KaaTcpCommand.KAA_TCP, command.getName());
    }

    @Test
    public void kaaTcpCommandGetSetTest() {
        int id = 1;
        long syncTime = 1;
        MqttFrame mqttFrame = new Connect();
        KaaTcpCommand kaaTcpCommand = new KaaTcpCommand();
        kaaTcpCommand.setCommandId(id);
        kaaTcpCommand.setResponse(mqttFrame);
        kaaTcpCommand.setSyncTime(syncTime);
        Assert.assertEquals(id, kaaTcpCommand.getCommandId());
        Assert.assertEquals(mqttFrame, kaaTcpCommand.getResponse());
        Assert.assertEquals(syncTime, kaaTcpCommand.getSyncTime());
    }

    @Test
    public void kaaTcpCommandFactoryGetSetTest() {
        KaaTcpCommandFactory kaaTcpCommandFactory = new KaaTcpCommandFactory();
        Assert.assertEquals(KaaTcpCommand.KAA_TCP, kaaTcpCommandFactory.getCommandName());
    }

    @Test
    public void nettyKaaTcpSyncMessageGetSetTest() {
        Connect connect = new Connect(10, 10, new byte[10], null, null);
        NettyTcpConnectMessage nettyTcpConnectMessage = new NettyTcpConnectMessage(null, null, connect, null, null, null, null);
        Assert.assertEquals(connect.isEncrypted(), nettyTcpConnectMessage.isEncrypted());
        Assert.assertEquals(connect.getKeepAlive(), nettyTcpConnectMessage.getKeepAlive());
    }

    @Test
    public void nettyTcpSyncMessageGetSetTest() {
        SyncRequest command = new SyncRequest(new byte[10], true, true);
        SessionInfo sessionInfo = new SessionInfo(UUID.randomUUID(), 10, mock(ChannelContext.class), ChannelType.ASYNC,
                mock(MessageEncoderDecoder.CipherPair.class), EndpointObjectHash.fromString("hash"), "appToken", "sdkToken", 100, true);
        NettyTcpSyncMessage nettyTcpSyncMessage = new NettyTcpSyncMessage(command, sessionInfo, null, null);
        Assert.assertEquals(command.isEncrypted(), nettyTcpSyncMessage.isEncrypted());
    }
}
