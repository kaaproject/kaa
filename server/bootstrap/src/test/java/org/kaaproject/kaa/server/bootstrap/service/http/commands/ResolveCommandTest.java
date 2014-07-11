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

/**
 * 
 */
package org.kaaproject.kaa.server.bootstrap.service.http.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;

import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.bootstrap.CommonBSConstants;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;
import org.kaaproject.kaa.server.bootstrap.service.OperationsServerListService;
import org.kaaproject.kaa.server.bootstrap.service.http.BootstrapConfig;
import org.kaaproject.kaa.server.bootstrap.service.initialization.BootstrapInitializationService;
import org.kaaproject.kaa.server.bootstrap.service.security.FileKeyStoreService;
import org.kaaproject.kaa.server.bootstrap.service.security.KeyStoreService;
import org.kaaproject.kaa.server.common.http.server.NettyHttpServer;

/**
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 *
 */
public class ResolveCommandTest {

    private static NettyHttpServer serverMock;
    private static BootstrapConfig config;
    private static BootstrapInitializationService biserviceMock;
    private static KeyStoreService keystoreMock;
    private static OperationsServerListService endpointListMock;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static OperationsServerList serverList;
    
    private static final String APPLICATION_TOKEN_TEST = "123";
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        KeyPairGenerator clientKeyGen = KeyPairGenerator.getInstance("RSA");
        clientKeyGen.initialize(2048);
        KeyPair clientKeyPair = clientKeyGen.genKeyPair();
        privateKey = clientKeyPair.getPrivate();
        publicKey = clientKeyPair.getPublic();
        
        List<OperationsServer> endpointServerArray = new LinkedList<OperationsServer>();
        ByteBuffer pk = ByteBuffer.wrap(new byte[] {10,12,13,14});
        List<SupportedChannel> supportedChannels = new ArrayList<>();
        HTTPComunicationParameters httpParams = new HTTPComunicationParameters();
        httpParams.setHostName("endpoint.com");
        httpParams.setPort(8080);
        SupportedChannel sc1 = new SupportedChannel(ChannelType.HTTP, httpParams);
        supportedChannels.add(sc1);
        OperationsServer server = new OperationsServer("host1",10,pk,supportedChannels );
        endpointServerArray.add(server);
        serverList = new OperationsServerList(endpointServerArray);
        
        config = new BootstrapConfig();
        serverMock = mock(NettyHttpServer.class);
        biserviceMock = mock(BootstrapInitializationService.class);
        keystoreMock = mock(FileKeyStoreService.class);
        endpointListMock = mock(OperationsServerListService.class);
        when(keystoreMock.getPublicKey()).thenReturn(publicKey);
        when(keystoreMock.getPrivateKey()).thenReturn(privateKey);
        when(biserviceMock.getKeyStoreService()).thenReturn(keystoreMock);
        config.setBootstrapInitializationService(biserviceMock);
        when(serverMock.getConf()).thenReturn(config);
        config.setOperationsServerListService(endpointListMock);
        when(endpointListMock.getOpsServerList()).thenReturn(serverList);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand#setServer(org.kaaproject.kaa.server.common.http.server.NettyHttpServer)}.
     */
    @Test
    public void testSetServer() {
        ResolveCommand cmd = new ResolveCommand();
        assertNotNull("ReolveCommand created", cmd);
        
        cmd.setServer(serverMock);
        assertNotNull(cmd.conf);
        assertNotNull(cmd.serverPrivate);
        assertNotNull(cmd.serverPublic);
        assertNotNull(cmd.crypt);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand#parse()}.
     */
    @Test
    public void testParse() {
        ResolveCommand cmd = new ResolveCommand();
        cmd.setServer(serverMock);
        
        //Test correct request
        try {
            cmd.setHttpRequest(getRequest(false));
            cmd.parse();
        } catch (Exception e) {
            fail(e.toString());
        }
        
        assertNotNull(cmd.requestData);

        //Test Exception on no application Token
        ResolveCommand cmd1 = new ResolveCommand();
        cmd1.setServer(serverMock);
        
        try {
            cmd1.setHttpRequest(getRequest(true));
            cmd1.parse();
            fail("Parse should fire Exception, No Application Token attribute");
        } catch (Exception e) {
            assertNotNull("Test on No application token attribute pass. "+ e.toString(),e);
        }
        
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand#process()}.
     */
    @Test
    public void testProcess() {
        ResolveCommand cmd = new ResolveCommand();
        cmd.setServer(serverMock);
        
        try {
            cmd.setHttpRequest(getRequest(false));
            cmd.parse();
            cmd.process();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
        assertNotNull(cmd.applicationToken);
        assertEquals(APPLICATION_TOKEN_TEST, cmd.applicationToken);
        assertNotNull(cmd.responseBody);
        assertNotNull(cmd.responseSignature);
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand#getHttpResponse()}.
     */
    @Test
    public void testGetHttpResponse() {
        ResolveCommand cmd = new ResolveCommand();
        cmd.setServer(serverMock);
        
        try {
            cmd.setHttpRequest(getRequest(false));
            cmd.parse();
            cmd.process();
            HttpResponse response = cmd.getHttpResponse();
            assertNotNull(response);
            
            String xsig = response.headers().get(CommonBSConstants.SIGNATURE_HEADER_NAME);
            assertNotNull(xsig);
            
            byte[] sig = Base64.decodeBase64(xsig);
            assertEquals(cmd.responseSignature.length, sig.length);
            for (int i = 0; i < sig.length; i++) {
                assertEquals(cmd.responseSignature[i], sig[i]);
            }
            
            assertEquals(HttpHeaders.Values.CLOSE, response.headers().get(CONNECTION));
            
        } catch (Exception e) {
            fail(e.toString());
        }
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand#isNeedConnectionClose()}.
     */
    @Test
    public void testIsNeedConnectionClose() {
        ResolveCommand cmd = new ResolveCommand();
        assertEquals(Boolean.TRUE, new Boolean(cmd.isNeedConnectionClose()));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.server.bootstrap.service.http.commands.ResolveCommand#ResolveCommand()}.
     */
    @Test
    public void testResolveCommand() {
        ResolveCommand cmd = new ResolveCommand();
        assertNotNull("ResolveCommand created", cmd);
        
        assertNotNull(cmd.requestConverter);
        assertNotNull(cmd.responseConverter);

    }
    
    private HttpRequest getRequest(boolean woAppToken) throws Exception {
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
        
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,HttpMethod.POST,CommonBSConstants.BOOTSTRAP_RESOLVE_URI);
        
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaders.Names.HOST, "gns.cybervisiontech.com");
        
        HttpPostRequestEncoder bodyRequestEncoder = null;
            
        AvroByteArrayConverter<Resolve> requestConverter = new AvroByteArrayConverter<Resolve>(Resolve.class);
        
        Resolve r = new Resolve();
        r.setApplicationToken(APPLICATION_TOKEN_TEST);
        
        bodyRequestEncoder = new HttpPostRequestEncoder(factory, request, false); // false not multipart
        if (woAppToken) {
            bodyRequestEncoder.addBodyAttribute(CommonBSConstants.APPLICATION_TOKEN_ATTR_NAME+"111", new String(requestConverter.toByteArray(r)));
        } else {
            bodyRequestEncoder.addBodyAttribute(CommonBSConstants.APPLICATION_TOKEN_ATTR_NAME, new String(requestConverter.toByteArray(r)));
        }
        request = bodyRequestEncoder.finalizeRequest();
        
        return request;
    }

}
