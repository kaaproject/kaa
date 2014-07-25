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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kaaproject.kaa.client.channel.BootstrapServerInfo;
import org.kaaproject.kaa.common.endpoint.gen.EndpointVersionInfo;
import org.kaaproject.kaa.common.endpoint.gen.EventClassFamilyVersionInfo;

public class KaaClientPropertiesTest {

    @Test
    public void testGetSupportedConfigVersion() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(1, properties.getSupportedConfigVersion());
    }

    @Test
    public void testGetSupportedProfileVersion() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(1, properties.getSupportedProfileVersion());
    }

    @Test
    public void testGetSupportedSystemNTVersion() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(1, properties.getSupportedSystemNTVersion());
    }

    @Test
    public void testGetSupportedUserNTVersion() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(1, properties.getSupportedUserNTVersion());
    }

    @Test
    public void testGetLogSchemaVersion() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals(1, properties.getLogSchemaVersion());
    }

    @Test
    public void testGetVersionInfo() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        EndpointVersionInfo versionInfo = properties.getVersionInfo();
        assertEquals(1, versionInfo.getConfigVersion().intValue());
        assertEquals(1, versionInfo.getProfileVersion().intValue());
        assertEquals(1, versionInfo.getSystemNfVersion().intValue());
        assertEquals(1, versionInfo.getUserNfVersion().intValue());
        assertEquals(1, versionInfo.getLogSchemaVersion().intValue());
    }

    @Test
    public void testGetBootstrapServers() throws Exception {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        List<BootstrapServerInfo> bootstraps = properties.getBootstrapServers();
        assertEquals(1, bootstraps.size());
        assertEquals("localhost", bootstraps.get(0).getHost());
        assertEquals(9889, bootstraps.get(0).getPort());
    }

    @Test
    public void testGetApplicationToken() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        assertEquals("098155062149502", properties.getApplicationToken());
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
    public void testGetEventFamilyVersions() throws IOException {
        System.setProperty(KaaClientProperties.KAA_CLIENT_PROPERTIES_FILE, "client-test.properties");
        KaaClientProperties properties = new KaaClientProperties();
        List<EventClassFamilyVersionInfo> infos = properties.getEventFamilyVersions();
        assertEquals(2, infos.size());
        assertEquals(1, infos.get(0).getVersion().intValue());
        assertEquals("test_event_family", infos.get(0).getName());
        assertEquals(2, infos.get(1).getVersion().intValue());
        assertEquals("test_event_family2", infos.get(1).getName());
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

}
