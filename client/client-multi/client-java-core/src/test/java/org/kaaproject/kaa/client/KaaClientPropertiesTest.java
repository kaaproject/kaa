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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.TransportProtocolIdConstants;
import org.kaaproject.kaa.client.util.CommonsBase64;

public class KaaClientPropertiesTest {
    @Test
    public void testGetBootstrapServers() throws Exception {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
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
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
        assertEquals("O7D+oECY1jhs6qIK8LA0zdaykmQ=", properties.getSdkToken());
    }

    @Test
    public void testGetPollDelay() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
        assertEquals(0, properties.getPollDelay().intValue());
    }

    @Test
    public void testGetPollPeriod() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
        assertEquals(10, properties.getPollPeriod().intValue());
    }

    @Test
    public void testGetPollUnit() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
        assertEquals(TimeUnit.SECONDS, properties.getPollUnit());
    }

    @Test
    public void testGetDefaultConfigData() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
        assertEquals(null, properties.getDefaultConfigData());
    }

    @Test
    public void testGetDefaultConfigSchema() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties(CommonsBase64.getInstance());
        assertEquals(null, properties.getDefaultConfigSchema());
    }

}
