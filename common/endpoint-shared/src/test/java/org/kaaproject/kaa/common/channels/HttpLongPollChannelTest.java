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
import org.kaaproject.kaa.common.bootstrap.gen.HTTPLPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.communication.HttpLongPollParameters;

/**
 * @author Andrey Panasenko
 *
 */
public class HttpLongPollChannelTest {

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getSupportedTransportTypes()}.
     */
    @Test
    public void testGetSupportedTransportTypes() {
        List<TransportType> transports =  HttpLongPollChannel.getSupportedTransportTypes();
        assertNotNull(transports);
        assertEquals(5, transports.size());
        assertEquals(TransportType.CONFIGURATION, transports.get(0));
        assertEquals(TransportType.EVENT, transports.get(1));
        assertEquals(TransportType.NOTIFICATION, transports.get(2));
        assertEquals(TransportType.PROFILE, transports.get(3));
        assertEquals(TransportType.USER, transports.get(4));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getSupportedTransports()}.
     */
    @Test
    public void testGetSupportedTransports() {
        Channel channel = new HttpLongPollChannel();
        List<TransportType> transports =  channel.getSupportedTransports();
        assertNotNull(transports);
        assertEquals(5, transports.size());
        assertEquals(TransportType.CONFIGURATION, transports.get(0));
        assertEquals(TransportType.EVENT, transports.get(1));
        assertEquals(TransportType.NOTIFICATION, transports.get(2));
        assertEquals(TransportType.PROFILE, transports.get(3));
        assertEquals(TransportType.USER, transports.get(4));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getType()}.
     */
    @Test
    public void testGetType() {
        assertEquals(ChannelType.HTTP_LP, HttpLongPollChannel.getType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getChannelType()}.
     */
    @Test
    public void testGetChannelType() {
        Channel channel = new HttpLongPollChannel();
        assertEquals(ChannelType.HTTP_LP, channel.getChannelType());
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#isTransportTypeSupported(org.kaaproject.kaa.common.TransportType)}.
     */
    @Test
    public void testIsTransportTypeSupportedTransportTypes() {
        Channel channel = new HttpLongPollChannel();
        assertEquals(false,channel.isTransportTypeSupported(TransportType.BOOTSTRAP));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.CONFIGURATION));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.EVENT));
        assertEquals(false,channel.isTransportTypeSupported(TransportType.LOGGING));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.NOTIFICATION));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.PROFILE));
        assertEquals(true,channel.isTransportTypeSupported(TransportType.USER));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#isTransportSupported(org.kaaproject.kaa.common.TransportType)}.
     */
    @Test
    public void testIsTransportSupported() {
        assertEquals(false,HttpLongPollChannel.isTransportSupported(TransportType.BOOTSTRAP));
        assertEquals(true,HttpLongPollChannel.isTransportSupported(TransportType.CONFIGURATION));
        assertEquals(true,HttpLongPollChannel.isTransportSupported(TransportType.EVENT));
        assertEquals(false,HttpLongPollChannel.isTransportSupported(TransportType.LOGGING));
        assertEquals(true,HttpLongPollChannel.isTransportSupported(TransportType.NOTIFICATION));
        assertEquals(true,HttpLongPollChannel.isTransportSupported(TransportType.PROFILE));
        assertEquals(true,HttpLongPollChannel.isTransportSupported(TransportType.USER));
    }

    /**
     * Test method for {@link org.kaaproject.kaa.common.channels.Channel#getHttpLongPollParametersFromSupportedChannel(org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel)}. 
     */
    @Test
    public void testGetHttpLongPollParametersFromSupportedChannel() {
        
        HTTPLPComunicationParameters communicationParameters = new HTTPLPComunicationParameters();
        communicationParameters.setHostName("host1");
        communicationParameters.setPort(100);
        SupportedChannel supportedChannel = new SupportedChannel(ChannelType.HTTP_LP, communicationParameters );
        
        try {
            HttpLongPollParameters convertedParams = HttpLongPollChannel.getHttpLongPollParametersFromSupportedChannel(supportedChannel);
            SupportedChannel convertedChannel = HttpLongPollChannel.getSupportedChannelFromHttpLongPollParameters(convertedParams);
            assertEquals(supportedChannel, convertedChannel);
        } catch (ParsingException e) {
            fail(e.toString());
        }
    }
}
