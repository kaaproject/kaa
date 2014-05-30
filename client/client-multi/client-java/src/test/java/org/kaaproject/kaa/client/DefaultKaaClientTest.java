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

package org.kaaproject.kaa.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.KaaClientProperties.BootstrapServerInfo;
import org.kaaproject.kaa.client.bootstrap.OperationsServerInfo;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.transport.HttpBootstrapTransport;
import org.kaaproject.kaa.client.transport.HttpOperationsTransport;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServer;
import org.kaaproject.kaa.common.bootstrap.gen.OperationsServerList;
import org.kaaproject.kaa.common.bootstrap.gen.Resolve;
import org.kaaproject.kaa.common.endpoint.gen.ConfSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.EndpointRegistrationRequest;
import org.kaaproject.kaa.common.endpoint.gen.LongSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.mockito.Mockito;

public class DefaultKaaClientTest {


    @Test
    public void testClientInit() throws Exception{

        Properties kaaProperties = getProperties("client-test.properties");
        KeyPair opsServerKeyPair = KeyUtil.generateKeyPair("client-test-ep1.public", "client-test-ep1.private");

        DefaultKaaClient clientSpy = Mockito.spy(new DefaultKaaClient(kaaProperties));
        HttpBootstrapTransport bootstrapTransportMock = Mockito.mock(HttpBootstrapTransport.class);
        HttpOperationsTransport operationsTransportMock = Mockito.mock(HttpOperationsTransport.class);

        Mockito.doReturn(bootstrapTransportMock).when(clientSpy).initBootstrapTransport(Mockito.any(BootstrapServerInfo.class));
        Mockito.doReturn(operationsTransportMock).when(clientSpy).initOperationsTransport(Mockito.any(OperationsServerInfo.class));

        OperationsServerList opsList = new OperationsServerList();

        List<OperationsServer> opsListBody = new ArrayList<>();
        OperationsServer ops = new OperationsServer("testDns", 1, ByteBuffer.wrap(opsServerKeyPair.getPublic().getEncoded()));
        opsListBody.add(ops);
        opsList.setOperationsServerArray(opsListBody);
        Mockito.when(bootstrapTransportMock.sendResolveRequest(Mockito.any(Resolve.class))).thenReturn(opsList);

        clientSpy.init();

        Assert.assertNotNull(clientSpy.getConfiguationManager());
        Assert.assertNotNull(clientSpy.getConfigurationPersistenceManager());
        Assert.assertNotNull(clientSpy.getDeltaManager());
        Assert.assertNotNull(clientSpy.getNotificationManager());
        Assert.assertNotNull(clientSpy.getProfileManager());
        Assert.assertNotNull(clientSpy.getSchemaPersistenceManager());
    }

    @Test
    public void testClientStartBeforeInit() throws Exception{
        Properties kaaProperties = getProperties("client-test.properties");

        DefaultKaaClient clientSpy = Mockito.spy(new DefaultKaaClient(kaaProperties));

        //does nothing before initialization;
        clientSpy.start();

        Assert.assertNotNull(clientSpy.getConfiguationManager());
        Assert.assertNotNull(clientSpy.getConfigurationPersistenceManager());
        Assert.assertNotNull(clientSpy.getDeltaManager());
        Assert.assertNotNull(clientSpy.getNotificationManager());
        Assert.assertNotNull(clientSpy.getProfileManager());
        Assert.assertNotNull(clientSpy.getSchemaPersistenceManager());
    }

    @Test
    public void testClientStartLongPollAfterInit() throws Exception{

        Properties kaaProperties = getProperties("client-test.properties");

        KeyPair opsServerKeyPair = KeyUtil.generateKeyPair("client-test-ep1.public", "client-test-ep1.private");

        DefaultKaaClient clientSpy = Mockito.spy(new DefaultKaaClient(kaaProperties));
        HttpBootstrapTransport bootstrapTransportMock = Mockito.mock(HttpBootstrapTransport.class);
        HttpOperationsTransport operationsTransportMock = Mockito.mock(HttpOperationsTransport.class);
        ProfileContainer profileContainerMock = Mockito.mock(ProfileContainer.class);

        Mockito.doReturn(bootstrapTransportMock).when(clientSpy).initBootstrapTransport(Mockito.any(BootstrapServerInfo.class));
        Mockito.doReturn(operationsTransportMock).when(clientSpy).initOperationsTransport(Mockito.any(OperationsServerInfo.class));

        OperationsServerList opsList = new OperationsServerList();

        List<OperationsServer> opsListBody = new ArrayList<>();
        OperationsServer ops = new OperationsServer("testDns", 1, ByteBuffer.wrap(opsServerKeyPair.getPublic().getEncoded()));
        opsListBody.add(ops);
        opsList.setOperationsServerArray(opsListBody);
        Mockito.when(bootstrapTransportMock.sendResolveRequest(Mockito.any(Resolve.class))).thenReturn(opsList);

        clientSpy.init();

        Mockito.verify(bootstrapTransportMock).sendResolveRequest(Mockito.any(Resolve.class));

        clientSpy.getProfileManager().setProfileContainer(profileContainerMock);

        Mockito.when(profileContainerMock.getSerializedProfile()).thenReturn(new byte[0]);
        ConfSyncResponse confSyncResponse = new ConfSyncResponse(ByteBuffer.wrap(new byte[0]), ByteBuffer.wrap(new byte[0]));
        SyncResponse syncResponse = new SyncResponse(1, SyncResponseStatus.CONF_RESYNC, confSyncResponse, null, null);
        Mockito.when(operationsTransportMock.sendRegisterCommand(Mockito.any(EndpointRegistrationRequest.class))).thenReturn(syncResponse);

        clientSpy.start();

        Thread.sleep(5000L);

        Mockito.verify(operationsTransportMock).sendRegisterCommand(Mockito.any(EndpointRegistrationRequest.class));
        Mockito.verify(operationsTransportMock).sendLongSyncRequest(Mockito.any(LongSyncRequest.class));

        clientSpy.stop();

        Assert.assertNotNull(clientSpy.getConfiguationManager());
        Assert.assertNotNull(clientSpy.getConfigurationPersistenceManager());
        Assert.assertNotNull(clientSpy.getDeltaManager());
        Assert.assertNotNull(clientSpy.getNotificationManager());
        Assert.assertNotNull(clientSpy.getProfileManager());
        Assert.assertNotNull(clientSpy.getSchemaPersistenceManager());
    }

    @Test
    public void testClientStartPollAfterInit() throws Exception{

        Properties kaaProperties = getProperties("client-test.properties");

        KeyPair opsServerKeyPair = KeyUtil.generateKeyPair("client-test-ep1.public", "client-test-ep1.private");

        DefaultKaaClient clientSpy = Mockito.spy(new DefaultKaaClient(kaaProperties));
        HttpBootstrapTransport bootstrapTransportMock = Mockito.mock(HttpBootstrapTransport.class);
        HttpOperationsTransport operationsTransportMock = Mockito.mock(HttpOperationsTransport.class);
        ProfileContainer profileContainerMock = Mockito.mock(ProfileContainer.class);

        Mockito.doReturn(bootstrapTransportMock).when(clientSpy).initBootstrapTransport(Mockito.any(BootstrapServerInfo.class));
        Mockito.doReturn(operationsTransportMock).when(clientSpy).initOperationsTransport(Mockito.any(OperationsServerInfo.class));
        OperationsServerList opsList = new OperationsServerList();

        List<OperationsServer> opsListBody = new ArrayList<>();
        OperationsServer ops = new OperationsServer("testDns", 1, ByteBuffer.wrap(opsServerKeyPair.getPublic().getEncoded()));
        opsListBody.add(ops);
        opsList.setOperationsServerArray(opsListBody);
        Mockito.when(bootstrapTransportMock.sendResolveRequest(Mockito.any(Resolve.class))).thenReturn(opsList);

        clientSpy.init();

        Mockito.verify(bootstrapTransportMock).sendResolveRequest(Mockito.any(Resolve.class));

        clientSpy.getProfileManager().setProfileContainer(profileContainerMock);

        Mockito.when(profileContainerMock.getSerializedProfile()).thenReturn(new byte[0]);
        ConfSyncResponse confSyncResponse = new ConfSyncResponse(ByteBuffer.wrap(new byte[0]), ByteBuffer.wrap(new byte[0]));
        SyncResponse syncResponse = new SyncResponse(1, SyncResponseStatus.CONF_RESYNC, confSyncResponse, null, null);
        Mockito.when(operationsTransportMock.sendRegisterCommand(Mockito.any(EndpointRegistrationRequest.class))).thenReturn(syncResponse);

        clientSpy.start();

        Thread.sleep(5000L);

        Mockito.verify(operationsTransportMock).sendRegisterCommand(Mockito.any(EndpointRegistrationRequest.class));
        Mockito.verify(operationsTransportMock).sendLongSyncRequest(Mockito.any(LongSyncRequest.class));

        clientSpy.stop();

        Assert.assertNotNull(clientSpy.getConfiguationManager());
        Assert.assertNotNull(clientSpy.getConfigurationPersistenceManager());
        Assert.assertNotNull(clientSpy.getDeltaManager());
        Assert.assertNotNull(clientSpy.getNotificationManager());
        Assert.assertNotNull(clientSpy.getProfileManager());
        Assert.assertNotNull(clientSpy.getSchemaPersistenceManager());
    }

    protected Properties getProperties(String fileName) throws IOException {
        Properties kaaProperties = new Properties();

        InputStream inputStream = DefaultKaaClientTest.class.getClassLoader().getResourceAsStream(fileName);

        kaaProperties.load(inputStream);

        if(Files.exists(Paths.get("state.properties"))){
            Files.delete(Paths.get("state.properties"));
        }
        return kaaProperties;
    }

}
