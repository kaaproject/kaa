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
package org.kaaproject.kaa.server.bootstrap.service.tcp.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPLPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.KaaTCPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResolve;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.PublicKeyType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.SupportedChannelRecord;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.SupportedChannelType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.mockito.Mock;

/**
 * @author Andrey Panasenko
 *
 */
public class KaaTcpCommandTest {
    
    private static OperationsServerListService opListMock;
    
    private static final String hostName = "localhost";
    private static final int port = 1000;
    private static byte[] key = new byte[] {10,20,30,40,50,60,70,80,90,100};

    @BeforeClass
    public static void init() {
        opListMock = mock(OperationsServerListService.class);
        OperationsServerList osl = new OperationsServerList();
        List<OperationsServer> value = new ArrayList<>();
        List<SupportedChannel> supportedChannelsArray = new ArrayList<>();
        KaaTCPComunicationParameters kaaTCPComunicationParameters = new KaaTCPComunicationParameters(hostName, port);
        SupportedChannel supChannel = new SupportedChannel(ChannelType.KAATCP, kaaTCPComunicationParameters);
        supportedChannelsArray.add(supChannel );
        HTTPLPComunicationParameters httpLPComunicationParameters = new HTTPLPComunicationParameters(hostName, port);
        SupportedChannel supChannel1 = new SupportedChannel(ChannelType.HTTP_LP, httpLPComunicationParameters);
        supportedChannelsArray.add(supChannel1 );
        HTTPLPComunicationParameters unusedLPComunicationParameters = new HTTPLPComunicationParameters(hostName, port);
        SupportedChannel supChannel2 = new SupportedChannel(ChannelType.BOOTSTRAP, unusedLPComunicationParameters);
        supportedChannelsArray.add(supChannel2 );
        OperationsServer opServer = new OperationsServer("Name1", 10, ByteBuffer.wrap(key), supportedChannelsArray );
        value.add(opServer );
        osl.setOperationsServerArray(value );
        when(opListMock.getOpsServerList()).thenReturn(osl );
    }
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand#KaaTcpCommand(org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService)}.
     */
    @Test
    public void testKaaTcpCommand() {
        KaaTcpCommand command = new KaaTcpCommand(opListMock);
        assertNotNull(command);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand#getName()}.
     */
    @Test
    public void testGetName() {
        KaaTcpCommand command = new KaaTcpCommand(opListMock);
        assertNotNull(command);
        assertEquals("KaaTcp", command.getName());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand#call()}.
     */
    @Test
    public void testCallError1() {
        KaaTcpCommand command = new KaaTcpCommand(opListMock);
        assertNotNull(command);
        try {
            command.call();
            assertNotNull(command.getResponse());
            if (command.getResponse().getMessageType() != MessageType.DISCONNECT) {
                fail("Incorect response message type");
            }
            Disconnect disconnect = (Disconnect) command.getResponse();
            if (disconnect.getReason() != DisconnectReason.BAD_REQUEST) {
                fail("Bad Disconnect reason");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand#call()}.
     */
    @Test
    public void testCallError2() {
        KaaTcpCommand command = new KaaTcpCommand(opListMock);
        assertNotNull(command);
        Connect connect = new Connect(10, key, key, key);
        command.setRequest(connect);
        assertNotNull(command.getRequest());
        try {
            command.call();
            assertNotNull(command.getResponse());
            if (command.getResponse().getMessageType() != MessageType.DISCONNECT) {
                fail("Incorect response message type");
            }
            Disconnect disconnect = (Disconnect) command.getResponse();
            if (disconnect.getReason() != DisconnectReason.BAD_REQUEST) {
                fail("Bad Disconnect reason");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand#call()}.
     */
    @Test
    public void testCallError3() {
        KaaTcpCommand command = new KaaTcpCommand(opListMock);
        assertNotNull(command);
        SyncRequest request = new SyncRequest(key, true, true);
        command.setRequest(request);
        assertNotNull(command.getRequest());
        try {
            command.call();
            assertNotNull(command.getResponse());
            if (command.getResponse().getMessageType() != MessageType.DISCONNECT) {
                fail("Incorect response message type");
            }
            Disconnect disconnect = (Disconnect) command.getResponse();
            if (disconnect.getReason() != DisconnectReason.BAD_REQUEST) {
                fail("Bad Disconnect reason");
            }
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    
    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.tcp.commands.KaaTcpCommand#call()}.
     */
    @Test
    public void testCall() {
        KaaTcpCommand command = new KaaTcpCommand(opListMock);
        assertNotNull(command);
        BootstrapResolve request = new BootstrapResolve("11111");
        request.setMessageId(111);
        command.setRequest(request);
        assertNotNull(command.getRequest());
        try {
            command.call();
            assertNotNull(command.getResponse());
            if (command.getResponse().getMessageType() == MessageType.DISCONNECT) {
                fail("Incorect response message type");
            }
            
            BootstrapResponse response = (BootstrapResponse)command.getResponse();
            
            assertEquals(111,response.getMessageId());
            
            assertNotNull(response.getOperationsServers());
            
            assertEquals(1,response.getOperationsServers().size());
            
            assertNotNull(response.getOperationsServers().get("Name1"));
            
            assertEquals(10,response.getOperationsServers().get("Name1").priority);
            
            assertArrayEquals(key, response.getOperationsServers().get("Name1").publicKey);
            
            assertEquals(PublicKeyType.RSA_PKSC8, response.getOperationsServers().get("Name1").publicKeyType);
            
            for(SupportedChannelRecord record : response.getOperationsServers().get("Name1").supportedChannelsList) {
                if (record.supportedChannelType == SupportedChannelType.HTTP
                        || record.supportedChannelType == SupportedChannelType.HTTPLP
                        || record.supportedChannelType == SupportedChannelType.KAATCP) {
                    assertEquals(hostName, record.hostName);
                    assertEquals(port, record.port);
                } else if (record.supportedChannelType == SupportedChannelType.UNUSED) {
                    assertEquals("", record.hostName);
                    assertEquals(0, record.port);
                } else {
                    fail("unknown channel type");
                }
            }
            
        } catch (Exception e) {
            fail(e.toString());
        }
    }

}
