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

package org.kaaproject.kaa.common.kaatcp;

import java.nio.ByteBuffer;

import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResolve;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.SupportedChannelType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.UnknownOperationsServerExceptions;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.BootstrapResponse.PublicKeyType;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck.ReturnCode;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.SyncResponse;

public class KaaTcpMessageTest {

    @Test
    public void testSyncResponseMessage() {
        final byte kaaSync[] = new byte [] { (byte) 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x14, (byte) 0xFF };
        SyncResponse message = new SyncResponse(new byte [] { (byte) 0xFF }, false, true);
        message.setMessageId(5);
        byte[] actual = message.getFrame().array();
        Assert.assertArrayEquals(kaaSync, actual);
    }

    @Test
    public void testSyncRequestMessage() {
        final byte kaaSync[] = new byte [] { (byte) 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x15, (byte) 0xFF };
        SyncRequest message = new SyncRequest(new byte [] { (byte) 0xFF }, false, true);
        message.setMessageId(5);
        byte[] actual = message.getFrame().array();
        Assert.assertArrayEquals(kaaSync, actual);
    }

    @Test
    public void testBootstrapResolveMessage() {
        final byte bootstrapResolve[] = new byte [] { (byte) 0xF0, 0x18, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x21, 'a','p','p','l','i','c','a','t','i','o','n','1' };
        final String applicationToken = "application1";
        BootstrapResolve message = new BootstrapResolve(applicationToken);
        message.setMessageId(5);
        byte[] actual = message.getFrame().array();
        Assert.assertArrayEquals(bootstrapResolve, actual);
    }

    @Test
    public void testBootstrapResponseMessage() {
        final byte bootstrapResponse[] = new byte [] {(byte)-16, -88, 2, 0, 6, 75, 97, 97, 116, 99, 112, 1, 0, 5, 32, 0, 0, 0, 2, 0, 0, 0, -120, 0, 0, 0, 7, 115, 101, 114, 118, 101, 114, 49, 0, 0, 0, 0, 10, 1, 0, 0, 16, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 0, 0, 3, 0, 0, 0, 25, 1, 21, 4, -68, 104, 111, 115, 116, 110, 97, 109, 101, 49, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 25, 2, 21, 4, -67, 104, 111, 115, 116, 110, 97, 109, 101, 49, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 25, 3, 21, 4, -66, 104, 111, 115, 116, 110, 97, 109, 101, 49, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, -120, 0, 0, 0, 8, 115, 101, 114, 118, 101, 114, 50, 50, 0, 0, 0, 20, 1, 0, 0, 16, 16, 17, 18, 19, 16, 17, 18, 19, 16, 17, 18, 19, 16, 17, 18, 19, 0, 0, 0, 3, 0, 0, 0, 25, 1, 21, 4, -68, 104, 111, 115, 116, 110, 97, 109, 101, 50, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 25, 2, 21, 4, -67, 104, 111, 115, 116, 110, 97, 109, 101, 50, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0, 0, 0, 0, 0, 26, 3, 22, 4, -66, 104, 111, 115, 116, 110, 97, 109, 101, 50, 50, 46, 101, 120, 97, 109, 112, 108, 101, 46, 99, 111, 109, 0, 0};
        BootstrapResponse message = new BootstrapResponse();
        final String operationServer1Name = "server1";
        final int operationServer1Priority = 10;
        final byte[]  operationServer1PublicKey = new byte [] { (byte)  0x00, 0x01, 0x02, 0x03,
                                                                        0x00, 0x01, 0x02, 0x03,
                                                                        0x00, 0x01, 0x02, 0x03,
                                                                        0x00, 0x01, 0x02, 0x03,};

        final String operationServer2Name = "server22";
        final int operationServer2Priority = 20;
        final byte[]  operationServer2PublicKey = new byte [] { (byte)  0x10, 0x11, 0x12, 0x13,
                                                                        0x10, 0x11, 0x12, 0x13,
                                                                        0x10, 0x11, 0x12, 0x13,
                                                                        0x10, 0x11, 0x12, 0x13,};

        message.addOperationsServer(operationServer1Name,
                operationServer1Priority,
                PublicKeyType.RSA_PKSC8,
                operationServer1PublicKey);

        try {
            message.addSupportedChannel(operationServer1Name, SupportedChannelType.HTTP, "hostname1.example.com", 1212);
            message.addSupportedChannel(operationServer1Name, SupportedChannelType.HTTPLP, "hostname1.example.com", 1213);
            message.addSupportedChannel(operationServer1Name, SupportedChannelType.KAATCP, "hostname1.example.com", 1214);
        } catch (UnknownOperationsServerExceptions e) {
            fail(e.toString());
        }

        message.addOperationsServer(operationServer2Name,
                operationServer2Priority,
                PublicKeyType.RSA_PKSC8,
                operationServer2PublicKey);

        try {
            message.addSupportedChannel(operationServer2Name, SupportedChannelType.HTTP, "hostname2.example.com", 1212);
            message.addSupportedChannel(operationServer2Name, SupportedChannelType.HTTPLP, "hostname2.example.com", 1213);
            message.addSupportedChannel(operationServer2Name, SupportedChannelType.KAATCP, "hostname22.example.com", 1214);
        } catch (UnknownOperationsServerExceptions e) {
            fail(e.toString());
        }

        message.setMessageId(5);

        byte[] actual = message.getFrame().array();
        Assert.assertArrayEquals(bootstrapResponse, actual);
    }

    @Test
    public void testConnectMessage() {
        final byte [] payload = new byte[] { (byte) 0xFF, 0x01, 0x02, 0x03 };

        final byte connectHeader[] = new byte [] { 0x10, 0x16, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, (byte) 0xf2, (byte) 0x91, (byte) 0xf2, (byte) 0xd4, 0x00, 0x00, 0x00, (byte) 0xC8 };

        Connect message = new Connect(200, 0xf291f2d4, null, payload, null);
        ByteBuffer frame = message.getFrame();

        byte [] headerCheck = new byte [20];
        frame.get(headerCheck);
        Assert.assertArrayEquals(connectHeader, headerCheck);

        byte [] payloadCheck = new byte [4];
        frame.get(payloadCheck);
        Assert.assertArrayEquals(payloadCheck, payload);
    }

    @Test
    public void testDisconnect() {
        final byte [] disconnect = new byte[] { (byte) 0xE0, 0x02, 0x00, 0x02 };
        Disconnect message = new Disconnect(DisconnectReason.INTERNAL_ERROR);
        Assert.assertArrayEquals(disconnect, message.getFrame().array());
    }

    @Test
    public void testConnack() {
        byte [] rawConnack = new byte[] { 0x20, 0x02, 0x00, 0x03 };
        ConnAck message = new ConnAck(ReturnCode.REFUSE_ID_REJECT);
        Assert.assertArrayEquals(rawConnack, message.getFrame().array());
    }

    @Test
    public void testPingRequest() {
        byte [] pingRequest = new byte[] { (byte) 0xC0, 0x00 };
        PingRequest message = new PingRequest();
        Assert.assertArrayEquals(pingRequest, message.getFrame().array());
    }

    @Test
    public void testPingResponse() {
        final byte [] pingResponse = new byte[] { (byte) 0xD0, 0x00 };
        PingResponse message = new PingResponse();
        Assert.assertArrayEquals(pingResponse, message.getFrame().array());
    }

}
