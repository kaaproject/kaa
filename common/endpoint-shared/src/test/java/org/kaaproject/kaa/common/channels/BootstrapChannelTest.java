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
package org.kaaproject.kaa.common.channels;

import static org.junit.Assert.*;

import java.util.List;
import org.junit.Test;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;

/**
 * @author Andrey Panasenko
 *
 */
public class BootstrapChannelTest {

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getSupportedTransportTypes()}.
     */
    @Test
    public void testGetSupportedTransportTypes() {
        List<TransportType> transports =  BootstrapChannel.getSupportedTransportTypes();
        assertNotNull(transports);
        assertEquals(1, transports.size());
        assertEquals(TransportType.BOOTSTRAP, transports.get(0));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getSupportedTransports()}.
     */
    @Test
    public void testGetSupportedTransports() {
        Channel channel = new BootstrapChannel();
        List<TransportType> transports =  channel.getSupportedTransports();
        assertNotNull(transports);
        assertEquals(1, transports.size());
        assertEquals(TransportType.BOOTSTRAP, transports.get(0));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getType()}.
     */
    @Test
    public void testGetType() {
        assertEquals(ChannelType.BOOTSTRAP, BootstrapChannel.getType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getChannelType()}.
     */
    @Test
    public void testGetChannelType() {
        Channel channel = new BootstrapChannel();
        assertEquals(ChannelType.BOOTSTRAP, channel.getChannelType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#isTransportTypeSupported(org.kaaproject.kaa.common.TransportType)}.
     */
    @Test
    public void testIsTransportTypeSupportedTransportTypes() {
        Channel channel = new BootstrapChannel();
        assertEquals(true,channel.isTransportTypeSupported(TransportType.BOOTSTRAP));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.CONFIGURATION));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.EVENT));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.LOGGING));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.NOTIFICATION));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.PROFILE));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.USER));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#isTransportSupported(org.kaaproject.kaa.common.TransportType)}.
     */
    @Test
    public void testIsTransportSupported() {
        assertEquals(true,BootstrapChannel.isTransportSupported(TransportType.BOOTSTRAP));
        assertEquals(false,BootstrapChannel.isTransportSupported(TransportType.CONFIGURATION));
        assertEquals(false,BootstrapChannel.isTransportSupported(TransportType.EVENT));
        assertEquals(false,BootstrapChannel.isTransportSupported(TransportType.LOGGING));
        assertEquals(false,BootstrapChannel.isTransportSupported(TransportType.NOTIFICATION));
        assertEquals(false,BootstrapChannel.isTransportSupported(TransportType.PROFILE));
        assertEquals(false,BootstrapChannel.isTransportSupported(TransportType.USER));
    }

}
