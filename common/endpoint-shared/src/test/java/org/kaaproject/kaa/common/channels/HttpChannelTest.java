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

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;

/**
 * @author Andrey Panasenko
 *
 */
public class HttpChannelTest {

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getSupportedTransportTypes()}.
     */
    @Test
    public void testGetSupportedTransportTypes() {
        List<TransportType> transports =  HttpChannel.getSupportedTransportTypes();
        assertNotNull(transports);
        assertEquals(6, transports.size());
        assertEquals(TransportType.CONFIGURATION, transports.get(0));
        assertEquals(TransportType.EVENT, transports.get(1));
        assertEquals(TransportType.NOTIFICATION, transports.get(2));
        assertEquals(TransportType.PROFILE, transports.get(3));
        assertEquals(TransportType.USER, transports.get(4));
        assertEquals(TransportType.LOGGING, transports.get(5));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getSupportedTransports()}.
     */
    @Test
    public void testGetSupportedTransports() {
        Channel channel = new HttpChannel();
        List<TransportType> transports =  channel.getSupportedTransports();
        assertNotNull(transports);
        assertEquals(6, transports.size());
        assertEquals(TransportType.CONFIGURATION, transports.get(0));
        assertEquals(TransportType.EVENT, transports.get(1));
        assertEquals(TransportType.NOTIFICATION, transports.get(2));
        assertEquals(TransportType.PROFILE, transports.get(3));
        assertEquals(TransportType.USER, transports.get(4));
        assertEquals(TransportType.LOGGING, transports.get(5));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getType()}.
     */
    @Test
    public void testGetType() {
        assertEquals(ChannelType.HTTP, HttpChannel.getType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getChannelType()}.
     */
    @Test
    public void testGetChannelType() {
        Channel channel = new HttpChannel();
        assertEquals(ChannelType.HTTP, channel.getChannelType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#isTransportTypeSupported(org.kaaproject.kaa.common.TransportType)}.
     */
    @Test
    public void testIsTransportTypeSupportedTransportTypes() {
        Channel channel = new HttpChannel();
        assertEquals(false,channel.isTransportTypeSupported(TransportType.BOOTSTRAP));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.CONFIGURATION));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.EVENT));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.LOGGING));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.NOTIFICATION));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.PROFILE));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.USER));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#isTransportSupported(org.kaaproject.kaa.common.TransportType)}.
     */
    @Test
    public void testIsTransportSupported() {
        assertEquals(false,HttpChannel.isTransportSupported(TransportType.BOOTSTRAP));
        assertEquals(true,HttpChannel.isTransportSupported(TransportType.CONFIGURATION));
        assertEquals(true,HttpChannel.isTransportSupported(TransportType.EVENT));
        assertEquals(true,HttpChannel.isTransportSupported(TransportType.LOGGING));
        assertEquals(true,HttpChannel.isTransportSupported(TransportType.NOTIFICATION));
        assertEquals(true,HttpChannel.isTransportSupported(TransportType.PROFILE));
        assertEquals(true,HttpChannel.isTransportSupported(TransportType.USER));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.HttpChannel#getHttpParametersFromSupportedChannel(org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel)}.
     */
    @Test
    public void testGetHttpParametersFromSupportedChannel() {
        
        HTTPComunicationParameters communicationParameters = new HTTPComunicationParameters("host1", 100);
        
        SupportedChannel supportedChannel = new SupportedChannel(ChannelType.HTTP, communicationParameters );
        
        try {
            HttpParameters convertedParams = HttpChannel.getHttpParametersFromSupportedChannel(supportedChannel);
            SupportedChannel convertedSuppChannel = HttpChannel.getSupportedChannelFromHttpParameters(convertedParams);
            assertEquals(supportedChannel, convertedSuppChannel);
        } catch (ParsingException e) {
            fail(e.toString());
        }
    }
    
    /**
     * test method for {@link org.kaaproject.kaa.common.channels.Channel#toString()}
     */
    @Test
    public void testToString() {
        Channel channel = new HttpChannel();
        assertEquals("HttpChannel [" + channel.getChannelType() + "]", channel.toString());
    }
    
    /**
     * test method for {@link org.kaaproject.kaa.common.channels.Channel#equals(Object)}
     */
    @Test
    public void testEquals() {
        Channel channel1 = new HttpChannel();
        Channel channel2 = new HttpChannel();
        Channel channel3 = new HttpLongPollChannel();
        if (!channel1.equals(channel2)) {
            fail("Error HttpChannel not equal other HttpChannel");
        }
        if (channel1.equals(channel3)) {
            fail("Error HttpChannel equal  HttpLongPollChannel");
        }
    }
}
