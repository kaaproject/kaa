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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.persistence.FilePersistentStorage;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;

public class AbstractKaaClientTest extends AbstractKaaClient {

    public AbstractKaaClientTest() throws IOException, InvalidKeySpecException,
            NoSuchAlgorithmException {
        super();
    }

    @Test
    public void testGetProfileManager() {
        assertNotNull(getProfileManager());
    }

    @Test
    public void testGetConfiguationManager() {
        assertNotNull(getConfiguationManager());
    }

    @Test
    public void testGetDeltaManager() {
        assertNotNull(getDeltaManager());
    }

    @Test
    public void testGetConfigurationPersistenceManager() {
        assertNotNull(getConfigurationPersistenceManager());
    }

    @Test
    public void testGetSchemaPersistenceManager() {
        assertNotNull(getSchemaPersistenceManager());
    }

    @Test
    public void testGetNotificationManager() {
        assertNotNull(getNotificationManager());
    }

    @Test
    public void testGetEndpointRegistrationManager() {
        assertNotNull(getEndpointRegistrationManager());
    }

    @Test
    public void testGetEventFamilyFactory() {
        assertNotNull(getEventFamilyFactory());
    }

    @Test
    public void testGetEventListenerResolver() {
        assertNotNull(getEventListenerResolver());
    }

    @Test
    public void testGetChannelMananager() {
        assertNotNull(getChannelMananager());
    }

    @Test
    public void testGetOperationMultiplexer() {
        assertNotNull(getOperationMultiplexer());
    }

    @Test
    public void testGetOperationDemultiplexer() {
        assertNotNull(getOperationDemultiplexer());
    }

    @Test
    public void testGetBootstrapMultiplexer() {
        assertNotNull(getBootstrapMultiplexer());
    }

    @Test
    public void testGetBootstrapDemultiplexer() {
        assertNotNull(getBootstrapDemultiplexer());
    }

    @Test
    public void testGetClientPublicKey() {
        assertNotNull(getClientPublicKey());
    }

    @Test
    public void testGetClientPrivateKey() {
        assertNotNull(getClientPrivateKey());
    }

    @Test
    public void testGetLogCollector() {
        assertNotNull(getLogCollector());
    }

    @Override
    public AbstractHttpClient createHttpClient(String url,
            PrivateKey privateKey, PublicKey publicKey,
            PublicKey remotePublicKey) {
        return mock(AbstractHttpClient.class);
    }

    @Override
    protected PersistentStorage createPersistentStorage() {
        PersistentStorage storage = new FilePersistentStorage();
        return storage;
    }

    @Override
    protected ConnectivityChecker createConnectivityChecker() {
        return mock(ConnectivityChecker.class);
    }

    @Test
    public void testInit(){
        try {
            start();
            init();
            init();
        } catch (Exception e) {
            assertTrue("Exception was caught during testInit test: " + e.getMessage(), false);
        }
    }

    @Test
    public void testStart() throws Exception {
        try {
            init();
            start();
            stop();
        } catch (Exception e) {
            assertTrue("Exception was caught during testStart test: " + e.getMessage(), false);
        }
    }
}
