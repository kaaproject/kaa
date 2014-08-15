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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.KaaTcpProtocolException;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnAckListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.ConnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.DisconnectListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.KaaSyncListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingRequestListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.listeners.PingResponseListener;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.ConnAck.ReturnCode;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Connect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.Disconnect.DisconnectReason;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.KaaSync;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.MessageFactory;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingRequest;
import org.kaaproject.kaa.common.channels.protocols.kaatcp.messages.PingResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;
import org.kaaproject.kaa.common.hash.SHA1HashUtils;
import org.mockito.Mockito;

public class MessageFactoryTest {

    @Test
    public void testConnackMessage() throws KaaTcpProtocolException {
        MessageFactory factory = new MessageFactory();
        byte [] rawConnack = new byte[] { 0x20, 0x02, 0x00, 0x03 };
        factory.getFramer().pushBytes(rawConnack);
        ConnAckListener idRejectListener = Mockito.spy(new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.REFUSE_ID_REJECT, message.getReturnCode());
            }
        });
        factory.registerMessageListener(idRejectListener);
        factory.getFramer().pushBytes(rawConnack);
        Mockito.verify(idRejectListener, Mockito.times(1)).onMessage(Mockito.any(ConnAck.class));

        byte [] rawConnackAccept = new byte[] { 0x20, 0x02, 0x00, 0x01 };
        ConnAckListener acceptListener = new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.ACCEPTED, message.getReturnCode());
            }
        };
        factory.registerMessageListener(acceptListener);
        factory.getFramer().pushBytes(rawConnackAccept);

        byte [] rawConnackBadProto = new byte[] { 0x20, 0x02, 0x00, 0x02 };
        ConnAckListener badProtoListener = new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.REFUSE_BAD_PROTOCOL, message.getReturnCode());
            }
        };
        factory.registerMessageListener(badProtoListener);
        factory.getFramer().pushBytes(rawConnackBadProto);


        byte [] rawConnackServerUnavailable = new byte[] { 0x20, 0x02, 0x00, 0x04 };
        ConnAckListener serverUnavailableListener = new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.REFUSE_SERVER_UNAVAILABLE, message.getReturnCode());
            }
        };
        factory.registerMessageListener(serverUnavailableListener);
        factory.getFramer().pushBytes(rawConnackServerUnavailable);

        byte [] rawConnackBadCredentials = new byte[] { 0x20, 0x02, 0x00, 0x05 };
        ConnAckListener badCredentialsListener = new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.REFUSE_BAD_CREDETIALS, message.getReturnCode());
            }
        };
        factory.registerMessageListener(badCredentialsListener);
        factory.getFramer().pushBytes(rawConnackBadCredentials);

        byte [] rawConnackNoAuth = new byte[] { 0x20, 0x02, 0x00, 0x06 };
        ConnAckListener noAuthListener = new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.REFUSE_NO_AUTH, message.getReturnCode());
            }
        };
        factory.registerMessageListener(noAuthListener);
        factory.getFramer().pushBytes(rawConnackNoAuth);

        byte [] rawConnackUndefined = new byte[] { 0x20, 0x02, 0x00, 0x07 };
        ConnAckListener undefinedListener = new ConnAckListener() {
            @Override
            public void onMessage(ConnAck message) {
                Assert.assertEquals(ReturnCode.UNDEFINED, message.getReturnCode());
            }
        };
        factory.registerMessageListener(undefinedListener);
        factory.getFramer().pushBytes(rawConnackUndefined);
    }


    @Test
    public void testConnectMessage() throws KaaTcpProtocolException, IOException, GeneralSecurityException {
        KeyPair clientPair = KeyUtil.generateKeyPair();
        KeyPair serverPair = KeyUtil.generateKeyPair();
        MessageEncoderDecoder crypt = new MessageEncoderDecoder(clientPair.getPrivate(), clientPair.getPublic(), serverPair.getPublic());
        AvroByteArrayConverter<SyncRequest> requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        SyncRequest request = new SyncRequest();

        request.setRequestId(42);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken("AppToken");
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(SHA1HashUtils.hashToBytes(clientPair.getPublic().getEncoded())));
        request.setSyncRequestMetaData(md);

        byte[] rawData = requestConverter.toByteArray(request);

        final byte [] payload = crypt.encodeData(rawData);
        final byte [] sessionKey = crypt.getEncodedSessionKey();
        final byte [] signature = crypt.sign(sessionKey);

        final byte connectHeader[] = new byte [] { 0x10, (byte)0xBE, 0x04, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0x11, 0x01, 0x00, (byte) 0xC8 };

        ByteBuffer connectBuffer = ByteBuffer.allocate(sessionKey.length + signature.length + payload.length + 17);
        connectBuffer.put(connectHeader);
        connectBuffer.put(sessionKey);
        connectBuffer.put(signature);
        connectBuffer.put(payload);
        connectBuffer.position(0);

        MessageFactory factory = new MessageFactory();
        factory.getFramer().pushBytes(connectBuffer.array());
        ConnectListener listener = Mockito.spy(new ConnectListener() {

            @Override
            public void onMessage(Connect message) {
                Assert.assertEquals(200, message.getKeepAlive());
                Assert.assertArrayEquals(signature, message.getSignature());
                Assert.assertArrayEquals(sessionKey, message.getAesSessionKey());
                Assert.assertArrayEquals(payload, message.getSyncRequest());
            }
        });
        factory.registerMessageListener(listener);
        factory.getFramer().pushBytes(connectBuffer.array());
        Mockito.verify(listener, Mockito.times(1)).onMessage(Mockito.any(Connect.class));
    }

    @Test
    public void testConnectMessageWithoutKey() throws KaaTcpProtocolException, IOException, GeneralSecurityException {
        KeyPair clientPair = KeyUtil.generateKeyPair();
        AvroByteArrayConverter<SyncRequest> requestConverter = new AvroByteArrayConverter<>(SyncRequest.class);
        SyncRequest request = new SyncRequest();

        request.setRequestId(42);
        SyncRequestMetaData md = new SyncRequestMetaData();
        md.setApplicationToken("AppToken");
        md.setEndpointPublicKeyHash(ByteBuffer.wrap(SHA1HashUtils.hashToBytes(clientPair.getPublic().getEncoded())));
        request.setSyncRequestMetaData(md);

        final byte[] rawData = requestConverter.toByteArray(request);

        final byte connectHeader[] = new byte [] { 0x10, 0x37, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x02, 0x00, 0x00, 0x00, (byte) 0xC8 };

        ByteBuffer connectBuffer = ByteBuffer.allocate(rawData.length + 16);
        connectBuffer.put(connectHeader);
        connectBuffer.put(rawData);
        connectBuffer.position(0);

        MessageFactory factory = new MessageFactory();
        factory.getFramer().pushBytes(connectBuffer.array());
        ConnectListener listener = Mockito.spy(new ConnectListener() {

            @Override
            public void onMessage(Connect message) {
                Assert.assertEquals(200, message.getKeepAlive());
                Assert.assertArrayEquals(rawData, message.getSyncRequest());
            }
        });
        factory.registerMessageListener(listener);
        factory.getFramer().pushBytes(connectBuffer.array());
        Mockito.verify(listener, Mockito.times(1)).onMessage(Mockito.any(Connect.class));
    }

    @Test
    public void testKaaSync() throws KaaTcpProtocolException {
        final byte kaaSync[] = new byte [] { (byte) 0xF0, 0x0D, 0x00, 0x06, 'K', 'a', 'a', 't', 'c', 'p', 0x01, 0x00, 0x05, 0x04, (byte) 0xFF };

        MessageFactory factory = new MessageFactory();
        factory.getFramer().pushBytes(kaaSync);
        KaaSyncListener listener = Mockito.spy(new KaaSyncListener() {

            @Override
            public void onMessage(KaaSync message) {
                Assert.assertEquals(1, message.getAvroObject().length);
                Assert.assertEquals(0xFF, message.getAvroObject()[0] & 0xFF);
                Assert.assertEquals(5, message.getMessageId());
                Assert.assertEquals(false, message.isZipped());
                Assert.assertEquals(true, message.isEncrypted());
                Assert.assertEquals(false, message.isRequest());
            }
        });
        factory.registerMessageListener(listener);
        factory.getFramer().pushBytes(kaaSync);
        Mockito.verify(listener, Mockito.times(1)).onMessage(Mockito.any(KaaSync.class));
    }

    @Test
    public void testPingRequest() throws KaaTcpProtocolException {
        final byte [] pingRequest = new byte[] { (byte) 0xC0, 0x00 };

        MessageFactory factory = new MessageFactory();
        factory.getFramer().pushBytes(pingRequest);
        PingRequestListener listener = Mockito.mock(PingRequestListener.class);
        factory.registerMessageListener(listener);
        factory.getFramer().pushBytes(pingRequest);
        Mockito.verify(listener, Mockito.times(1)).onMessage(Mockito.any(PingRequest.class));
    }

    @Test
    public void testPingResponse() throws KaaTcpProtocolException {
        final byte [] pingResponse = new byte[] { (byte) 0xD0, 0x00 };

        MessageFactory factory = new MessageFactory();
        factory.getFramer().pushBytes(pingResponse);
        PingResponseListener listener = Mockito.mock(PingResponseListener.class);
        factory.registerMessageListener(listener);
        factory.getFramer().pushBytes(pingResponse);
        Mockito.verify(listener, Mockito.times(1)).onMessage(Mockito.any(PingResponse.class));
    }

    @Test
    public void testDisconnect() throws KaaTcpProtocolException {
        final byte [] disconnect = new byte[] { (byte) 0xE0, 0x02, 0x00, 0x02 };

        MessageFactory factory = new MessageFactory();
        factory.getFramer().pushBytes(disconnect);
        DisconnectListener listener = Mockito.spy(new DisconnectListener() {

            @Override
            public void onMessage(Disconnect message) {
                Assert.assertEquals(DisconnectReason.INTERNAL_ERROR, message.getReason());
            }
        });
        factory.registerMessageListener(listener);
        factory.getFramer().pushBytes(disconnect);
        Mockito.verify(listener, Mockito.times(1)).onMessage(Mockito.any(Disconnect.class));
    }
}
