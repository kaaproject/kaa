/*
 * Copyright 2014-2015 CyberVision, Inc.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kaaproject.kaa.client.bootstrap.DefaultBootstrapManager;
import org.kaaproject.kaa.client.channel.GenericTransportInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.context.SimpleExecutorContext;
import org.kaaproject.kaa.client.context.TransportContext;
import org.kaaproject.kaa.client.exceptions.KaaException;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.client.util.CommonsBase64;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolVersionPair;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.kaaproject.kaa.schema.base.Profile;
import org.mockito.Mockito;

public class KaaClientTest {

    private KaaClientPlatformContext platformContext;
    private KaaClientProperties clientProperties;
    private KaaClientStateListener stateListener;
    private PersistentStorage storage;
    private DefaultBootstrapManager bsManagerMock;
    private GenericKaaClient client;

    @Before
    public void beforeTest() throws Exception {
        platformContext = Mockito.mock(KaaClientPlatformContext.class);
        clientProperties = Mockito.mock(KaaClientProperties.class);
        stateListener = Mockito.mock(KaaClientStateListener.class);
        storage = Mockito.mock(PersistentStorage.class);

        Mockito.when(platformContext.getBase64()).thenReturn(CommonsBase64.getInstance());
        Mockito.when(platformContext.getProperties()).thenReturn(clientProperties);
        Mockito.when(platformContext.createPersistentStorage()).thenReturn(storage);
        ExecutorContext executorContext = new SimpleExecutorContext();
        Mockito.when(platformContext.getExecutorContext()).thenReturn(executorContext);

        Mockito.when(clientProperties.getBootstrapServers()).thenReturn(buildDummyConnectionInfo());
        Mockito.when(clientProperties.getPropertiesHash()).thenReturn("test".getBytes());

        initStorageMock(storage);

        bsManagerMock = Mockito.mock(DefaultBootstrapManager.class);
        client = new AbstractKaaClient(platformContext, stateListener) {
            @Override
            protected DefaultBootstrapManager buildBootstrapManager(KaaClientProperties properties, KaaClientState kaaClientState,
                    TransportContext transportContext) {
                return bsManagerMock;
            }
        };

        client.setProfileContainer(new ProfileContainer() {
            @Override
            public Profile getProfile() {
                return new Profile();
            }
        });
    }

    @Test
    public void basicLifeCycleTest() throws Exception {
        client.start();

        Mockito.verify(stateListener, Mockito.timeout(1000)).onStarted();
        Mockito.verify(bsManagerMock).receiveOperationsServerList();

        client.pause();

        Mockito.verify(stateListener, Mockito.timeout(1000)).onPaused();

        client.resume();

        Mockito.verify(stateListener, Mockito.timeout(1000)).onResume();

        client.stop();

        Mockito.verify(stateListener, Mockito.timeout(1000)).onStopped();
    }

    @Test
    public void basicStartBSFailureTest() throws Exception {
        Mockito.doThrow(new TransportException("mock")).when(bsManagerMock).receiveOperationsServerList();

        client.start();

        Mockito.verify(stateListener, Mockito.timeout(1000)).onStartFailure(Mockito.any(KaaException.class));
        Mockito.verify(bsManagerMock).receiveOperationsServerList();

        client.stop();

        Mockito.verify(stateListener, Mockito.timeout(1000)).onStopped();
    }

    protected void initStorageMock(PersistentStorage storage) throws NoSuchAlgorithmException, IOException {
        KeyPair kp = KeyUtil.generateKeyPair();
        Mockito.when(storage.exists(KaaClientPropertiesState.CLIENT_PUBLIC_KEY_DEFAULT)).thenReturn(true);
        Mockito.when(storage.exists(KaaClientPropertiesState.CLIENT_PRIVATE_KEY_DEFAULT)).thenReturn(true);
        Mockito.when(storage.openForRead(KaaClientPropertiesState.CLIENT_PUBLIC_KEY_DEFAULT)).thenReturn(
                new ByteArrayInputStream(kp.getPublic().getEncoded()));
        Mockito.when(storage.openForRead(KaaClientPropertiesState.CLIENT_PRIVATE_KEY_DEFAULT)).thenReturn(
                new ByteArrayInputStream(kp.getPrivate().getEncoded()));
        Mockito.when(storage.openForWrite(Mockito.anyString())).thenReturn(Mockito.mock(OutputStream.class));
    }

    protected Map<TransportProtocolId, List<TransportConnectionInfo>> buildDummyConnectionInfo() {
        Map<TransportProtocolId, List<TransportConnectionInfo>> connectionInfo = new HashMap<>();
        List<TransportConnectionInfo> connectionInfoList = new ArrayList<TransportConnectionInfo>();
        connectionInfoList
                .add(new GenericTransportInfo(ServerType.BOOTSTRAP, new ProtocolMetaData(1, new ProtocolVersionPair(1, 1), null)));
        connectionInfo.put(new TransportProtocolId(1, 1), connectionInfoList);
        return connectionInfo;
    }

}
