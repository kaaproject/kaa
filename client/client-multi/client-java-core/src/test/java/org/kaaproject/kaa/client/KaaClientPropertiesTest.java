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

package org.kaaproject.kaa.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.TransportProtocolIdConstants;
import org.kaaproject.kaa.client.util.CommonsBase64;

public class KaaClientPropertiesTest {
    @Test
    public void testGetBootstrapServers() throws Exception {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setBase64(CommonsBase64.getInstance());
        Map<TransportProtocolId, List<TransportConnectionInfo>> bootstraps = properties.getBootstrapServers();
        assertEquals(1, bootstraps.size());

        assertNotNull(bootstraps.get(TransportProtocolIdConstants.TCP_TRANSPORT_ID));
        assertEquals(1, bootstraps.get(TransportProtocolIdConstants.TCP_TRANSPORT_ID).size());
        TransportConnectionInfo serverInfo = bootstraps.get(TransportProtocolIdConstants.TCP_TRANSPORT_ID).get(0);
        
        assertEquals(ServerType.BOOTSTRAP, serverInfo.getServerType());
        assertEquals(1, serverInfo.getAccessPointId());
        assertEquals(TransportProtocolIdConstants.TCP_TRANSPORT_ID, serverInfo.getTransportId());
        
    }

    @Test
    public void testGetSdkToken() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals("O7D+oECY1jhs6qIK8LA0zdaykmQ=", properties.getSdkToken());
    }

    @Test
    public void testGetPollDelay() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(0, properties.getPollDelay().intValue());
    }

    @Test
    public void testGetPollPeriod() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(10, properties.getPollPeriod().intValue());
    }

    @Test
    public void testGetPollUnit() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(TimeUnit.SECONDS, properties.getPollUnit());
    }

    @Test
    public void testGetDefaultConfigData() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(null, properties.getDefaultConfigData());
    }

    @Test
    public void testGetDefaultConfigSchema() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(null, properties.getDefaultConfigSchema());
    }

    @Test
    public void testGetWorkingDirectory() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals("." + KaaClientProperties.FILE_SEPARATOR, properties.getWorkingDirectory());
    }

    @Test
    public void testSetWorkingDirectory() throws IOException {
        String requestedWorkDir = "dir_";
        String fileSeparator = System.getProperty("file.separator");
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setWorkingDirectory(requestedWorkDir);
        assertEquals(requestedWorkDir + fileSeparator, properties.getWorkingDirectory());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBlankWorkingDirectory() throws IOException {
        String requestedWorkDir = "";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setWorkingDirectory(requestedWorkDir);
    }

    @Test
    public void testGetStateFileName() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals("state.properties", properties.getStateFileName());
    }

    @Test
    public void testGetStateFileFullName() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(properties.getWorkingDirectory() + "state.properties", properties.getStateFileFullName());
    }

    @Test
    public void testSetStateFileName() throws IOException {
        String requestedName = "test_state.properties";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setStateFileName(requestedName);
        assertEquals(requestedName, properties.getStateFileName());
        assertEquals(properties.getWorkingDirectory() + requestedName, properties.getStateFileFullName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBlankStateFileName() throws IOException {
        String requestedName = "";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setStateFileName(requestedName);
    }


    @Test
    public void testGetPublicKeyFileName() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals("key.public", properties.getPublicKeyFileName());
    }

    @Test
    public void testGetPublicKeyFileFullName() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(properties.getWorkingDirectory() + "key.public", properties.getPublicKeyFileFullName());
    }

    @Test
    public void testSetPublicKeyFileName() throws IOException {
        String requestedName = "test_key.public";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setPublicKeyFileName(requestedName);
        assertEquals(requestedName, properties.getPublicKeyFileName());
        assertEquals(properties.getWorkingDirectory() + requestedName, properties.getPublicKeyFileFullName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBlankPublicKeyFileName() throws IOException {
        String requestedName = "";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setPublicKeyFileName(requestedName);
    }


    @Test
    public void testGetPrivateKeyFileName() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals("key.private", properties.getPrivateKeyFileName());
    }

    @Test
    public void testGetPrivateKeyFileFullName() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(properties.getWorkingDirectory() + "key.private", properties.getPrivateKeyFileFullName());
    }

    @Test
    public void testSetPrivateKeyFileName() throws IOException {
        String requestedName = "test_key.private";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setPrivateKeyFileName(requestedName);
        assertEquals(requestedName, properties.getPrivateKeyFileName());
        assertEquals(properties.getWorkingDirectory() + requestedName, properties.getPrivateKeyFileFullName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBlankPrivateKeyFileName() throws IOException {
        String requestedName = "";
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        properties.setPrivateKeyFileName(requestedName);
    }

}
