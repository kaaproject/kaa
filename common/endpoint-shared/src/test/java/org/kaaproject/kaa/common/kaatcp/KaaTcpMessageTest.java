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

package org.kaaproject.kaa.common.kaatcp;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck;
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
        SyncResponse syncResponse = new SyncResponse();
        Assert.assertNotNull(syncResponse);
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
