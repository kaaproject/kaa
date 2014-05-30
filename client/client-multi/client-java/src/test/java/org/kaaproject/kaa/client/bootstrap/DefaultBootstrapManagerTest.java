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

package org.kaaproject.kaa.client.bootstrap;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;

import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.BootstrapRuntimeException;
import org.kaaproject.kaa.client.bootstrap.DefaultBootstrapManager;
import org.kaaproject.kaa.client.transport.BootstrapTransport;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class DefaultBootstrapManagerTest {

    @Test
    public void testReceiveOperationsServerList() throws TransportException {
        DefaultBootstrapManager manager = new DefaultBootstrapManager("");
        BootstrapTransport transport = mock(BootstrapTransport.class);

        OperationsServerList serverList = new OperationsServerList();
        when(transport.sendResolveRequest(any(Resolve.class))).thenReturn(serverList);
        manager.setTransport(transport);

        boolean exception = false;
        try {
            manager.receiveOperationsServerList();
        } catch (BootstrapRuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        LinkedList<OperationsServer> list = new LinkedList<OperationsServer>();
        list.add(new OperationsServer("host1", 1, ByteBuffer.wrap(new byte [] { 1, 2, 3 })));
        list.add(new OperationsServer("host2", 3, ByteBuffer.wrap(new byte [] { 1, 2, 3 })));
        serverList.setOperationsServerArray(list);

        manager.receiveOperationsServerList();

        verify(transport, times(2)).sendResolveRequest(any(Resolve.class));
    }

    @Test
    public void testOperationsServerInfoRetrieving() throws TransportException, NoSuchAlgorithmException, InvalidKeySpecException {
        DefaultBootstrapManager manager = new DefaultBootstrapManager("");

        boolean exception = false;
        try {
            manager.getNextOperationsServer();
        } catch (BootstrapRuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        BootstrapTransport transport = mock(BootstrapTransport.class);

        // Generating pseudo bootstrap key
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.genKeyPair();

        OperationsServerList serverList = new OperationsServerList();
        OperationsServer server = new OperationsServer();
        server.setDNSName("localhost:9889");
        server.setPublicKey(ByteBuffer.wrap(keyPair.getPublic().getEncoded()));
        LinkedList<OperationsServer> list = new LinkedList<OperationsServer>();
        list.add(server);
        serverList.setOperationsServerArray(list);

        when(transport.sendResolveRequest(any(Resolve.class))).thenReturn(serverList);
        manager.setTransport(transport);

        manager.receiveOperationsServerList();

        OperationsServerInfo info = manager.getNextOperationsServer();
        assertEquals("localhost:9889", info.getHostName());
        assertArrayEquals(keyPair.getPublic().getEncoded(), info.getKey().getEncoded());

        assertNull(manager.getOperationsServerByDnsName(null));
        assertNull(manager.getOperationsServerByDnsName("some.name"));
        OperationsServerInfo infoByName = manager.getOperationsServerByDnsName("localhost:9889");
        assertEquals("localhost:9889", infoByName.getHostName());
        assertArrayEquals(keyPair.getPublic().getEncoded(), infoByName.getKey().getEncoded());
    }

}
