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

package org.kaaproject.kaa.client.transport;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;
import org.kaaproject.kaa.common.endpoint.CommonEPConstans;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProfileUpdateRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.security.MessageEncoderDecoder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.eq;

public class HttpTransportTest {

    class TransportTest {
        private MessageEncoderDecoder messageEncDec;
        private StatusLine statusLine;
        private CloseableHttpClient httpClient;
        private byte[] serializedResponse;

        <T extends SpecificRecordBase>
        TransportTest(T avroResponse, Class<T> avroResponseClass) throws GeneralSecurityException, ClientProtocolException, IOException {
            AvroByteArrayConverter<T> converter = new AvroByteArrayConverter<>(avroResponseClass);
            serializedResponse = converter.toByteArray(avroResponse);
            messageEncDec = mock(MessageEncoderDecoder.class);
            when(messageEncDec.verify(any(byte[].class), any(byte[].class))).thenReturn(true);
            when(messageEncDec.getEncodedSessionKey()).thenReturn(new String("encoded key").getBytes("UTF-8"));
            when(messageEncDec.encodeData(any(byte[].class))).thenReturn(new String("encoded data").getBytes("UTF-8"));
            when(messageEncDec.decodeData(any(byte[].class))).thenReturn(serializedResponse);
            when(messageEncDec.sign(any(byte[].class))).thenReturn(new String("signature").getBytes("UTF-8"));

            statusLine = mock(StatusLine.class);
            when(statusLine.getStatusCode()).thenReturn(200);
            CloseableHttpResponse response = mock(CloseableHttpResponse.class);
            when(response.getStatusLine()).thenReturn(statusLine);
            when(response.getFirstHeader(eq(CommonEPConstans.SIGNATURE_HEADER_NAME))).thenReturn(mock(Header.class));
            when(response.getEntity()).thenReturn(mock(HttpEntity.class));

            httpClient = mock(CloseableHttpClient.class);
            when(httpClient.execute(any(HttpPost.class))).thenReturn(response);
        }

        void setMocksToTransport(AbstractHttpTransport transport) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, IOException, GeneralSecurityException {
            Class<AbstractHttpTransport> transportClass = AbstractHttpTransport.class;
            Field field = transportClass.getDeclaredField("httpClient");
            field.setAccessible(true);
            field.set(transport, httpClient);
            Field encoderDecoderField = transportClass.getDeclaredField("messageEncDec");
            encoderDecoderField.setAccessible(true);
            encoderDecoderField.set(transport, messageEncDec);
        }

        void setStatusCode(int code) {
            when(statusLine.getStatusCode()).thenReturn(code);
        }

        void setSignatureVerificationResult(boolean result) throws GeneralSecurityException {
            when(messageEncDec.verify(any(byte[].class), any(byte[].class))).thenReturn(result);
        }

        CloseableHttpClient getHttpClient() {
            return httpClient;
        }
    }

    static HttpOperationsTransport createEndpointTransport() throws NoSuchAlgorithmException {
        String host = "localhost:9889";
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        KeyPair clientKeyPair = clientKeyGen.genKeyPair();
        PrivateKey privateKey = clientKeyPair.getPrivate();
        PublicKey publicKey = clientKeyPair.getPublic();
        PublicKey remotePublicKey = clientKeyPair.getPublic();

        return new HttpOperationsTransport(host, privateKey, publicKey, remotePublicKey);
    }

    @Test
    public void testSyncRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setResponseType(SyncResponseStatus.NO_DELTA);
        TransportTest transportTest = new TransportTest(syncResponse, SyncResponse.class);

        HttpOperationsTransport transport = createEndpointTransport();
        transportTest.setMocksToTransport(transport);

        SyncRequest request = new SyncRequest();
        request.setApplicationToken("1234567");
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setProfileHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setAppStateSeqNumber(0);
        request.setConfigurationHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setTopicListHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        SyncResponse result = transport.sendSyncRequest(request);
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(syncResponse, result);
        transport.close();
    }

    @Test
    public void testLongSyncRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setResponseType(SyncResponseStatus.NO_DELTA);
        TransportTest transportTest = new TransportTest(syncResponse, SyncResponse.class);

        HttpOperationsTransport transport = createEndpointTransport();
        transportTest.setMocksToTransport(transport);

        SyncRequest request = new SyncRequest();
        request.setApplicationToken("1234567");
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setProfileHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setAppStateSeqNumber(0);
        request.setConfigurationHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setTopicListHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        SyncResponse result = transport.sendLongSyncRequest(new LongSyncRequest(request, 5l));
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(syncResponse, result);
    }

    @Test
    public void testUpdateRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setResponseType(SyncResponseStatus.NO_DELTA);
        TransportTest transportTest = new TransportTest(syncResponse, SyncResponse.class);

        HttpOperationsTransport transport = createEndpointTransport();
        transportTest.setMocksToTransport(transport);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setApplicationToken("1234567");
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setProfileBody(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setVersionInfo(new EndpointVersionInfo(1, 1, 1, 1));
        SyncResponse result = transport.sendUpdateCommand(request);
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(syncResponse, result);
    }

    @Test
    public void testRegisterRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setResponseType(SyncResponseStatus.NO_DELTA);
        TransportTest transportTest = new TransportTest(syncResponse, SyncResponse.class);

        HttpOperationsTransport transport = createEndpointTransport();
        transportTest.setMocksToTransport(transport);

        EndpointRegistrationRequest request = new EndpointRegistrationRequest();
        request.setApplicationToken("1234567");
        request.setEndpointPublicKey(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setProfileBody(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setVersionInfo(new EndpointVersionInfo(1, 1, 1, 1));
        SyncResponse result = transport.sendRegisterCommand(request);
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(syncResponse, result);
    }

    @Test(expected = TransportException.class)
    public void testBadStatusCode() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setResponseType(SyncResponseStatus.NO_DELTA);
        TransportTest transportTest = new TransportTest(syncResponse, SyncResponse.class);

        HttpOperationsTransport transport = createEndpointTransport();
        transportTest.setStatusCode(500);
        transportTest.setMocksToTransport(transport);

        SyncRequest request = new SyncRequest();
        request.setApplicationToken("1234567");
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setProfileHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setAppStateSeqNumber(0);
        request.setConfigurationHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setTopicListHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        SyncResponse result = transport.sendSyncRequest(request);
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(syncResponse, result);
    }

    @Test(expected = TransportException.class)
    public void testBadSignature() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setResponseType(SyncResponseStatus.NO_DELTA);
        TransportTest transportTest = new TransportTest(syncResponse, SyncResponse.class);

        HttpOperationsTransport transport = createEndpointTransport();
        transportTest.setSignatureVerificationResult(false);
        transportTest.setMocksToTransport(transport);

        SyncRequest request = new SyncRequest();
        request.setApplicationToken("1234567");
        request.setEndpointPublicKeyHash(ByteBuffer.wrap(new byte [] {1, 2, 3}));
        request.setProfileHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setAppStateSeqNumber(0);
        request.setConfigurationHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        request.setTopicListHash(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        SyncResponse result = transport.sendSyncRequest(request);
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(syncResponse, result);
    }

    class HttpBootstrapTransportTest extends HttpBootstrapTransport {
        private OperationsServerList response;

        public HttpBootstrapTransportTest(OperationsServerList response, String host, PublicKey remotePublicKey) {
            super(host, remotePublicKey);
            this.response = response;
        }

        @Override
        protected byte [] getResponseBody(HttpResponse response) throws IOException, GeneralSecurityException {
            AvroByteArrayConverter<OperationsServerList> converter = new AvroByteArrayConverter<>(OperationsServerList.class);
            return converter.toByteArray(this.response);
        }
    }

    @Test
    public void testResolveRequest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, TransportException, ClientProtocolException, IOException, GeneralSecurityException, NoSuchMethodException {
        String host = "localhost:9999";
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        KeyPair clientKeyPair = clientKeyGen.genKeyPair();
        PublicKey remotePublicKey = clientKeyPair.getPublic();

        List<OperationsServer> endpointsList = new LinkedList<OperationsServer>();
        endpointsList.add(new OperationsServer("localhost:9889", 1, ByteBuffer.wrap(new byte[] {1, 2, 3})));
        OperationsServerList response = new OperationsServerList(endpointsList);
        TransportTest transportTest = new TransportTest(response, OperationsServerList.class);

        HttpBootstrapTransportTest transport = new HttpBootstrapTransportTest(response, host, remotePublicKey);
        transportTest.setMocksToTransport(transport);

        Resolve request = new Resolve("1234567");
        OperationsServerList result = transport.sendResolveRequest(request);
        verify(transportTest.getHttpClient(), times(1)).execute(any(HttpPost.class));
        assertEquals(response, result);
    }
}
