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
import org.kaaproject.kaa.common.bootstrap.gen.KaaTCPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.communication.KaaTcpParameters;

public class KaaTcpChannelTest {

    @Test
    public void testGetSupportedTransports() {
        KaaTcpChannel tcpChannel = new KaaTcpChannel();
        List<TransportType> transports =  tcpChannel.getSupportedTransports();
        assertNotNull(transports);
        assertEquals(5, transports.size());
        assertEquals(TransportType.CONFIGURATION, transports.get(0));
        assertEquals(TransportType.EVENT, transports.get(1));
        assertEquals(TransportType.NOTIFICATION, transports.get(2));
        assertEquals(TransportType.PROFILE, transports.get(3));
        assertEquals(TransportType.USER, transports.get(4));
    }

    @Test
    public void testGetChannelType() {
        KaaTcpChannel tcpChannel = new KaaTcpChannel();
        assertEquals(tcpChannel.getChannelType(), ChannelType.KAATCP);
    }

    @Test
    public void testIsTransportTypeSupportedTransportType() {
        KaaTcpChannel tcpChannel = new KaaTcpChannel();
        assertFalse(tcpChannel.isTransportTypeSupported(TransportType.LOGGING));
        assertTrue(tcpChannel.isTransportTypeSupported(TransportType.EVENT));
    }

    @Test
    public void testGetKaaTcpParametersFromSupportedChannel() throws ParsingException {
        SupportedChannel channel = new SupportedChannel();
        channel.setChannelType(ChannelType.KAATCP);
        channel.setCommunicationParameters(new KaaTCPComunicationParameters("localhost", 1234));
        KaaTcpParameters parameters = KaaTcpChannel.getKaaTcpParametersFromSupportedChannel(channel);
        assertEquals("localhost", parameters.getHostName());
        assertEquals(1234, parameters.getPort());
    }

    @Test(expected=ParsingException.class)
    public void testGetBadKaaTcpParametersFromSupportedChannel() throws ParsingException {
        SupportedChannel channel = new SupportedChannel();
        channel.setChannelType(ChannelType.BOOTSTRAP);
        channel.setCommunicationParameters(new String("http://localhost:1234"));
        KaaTcpChannel.getKaaTcpParametersFromSupportedChannel(channel);
    }

    @Test
    public void testGetSupportedChannelFromKaaTcpParameters() throws ParsingException {
        SupportedChannel channel = new SupportedChannel();
        channel.setChannelType(ChannelType.KAATCP);
        channel.setCommunicationParameters(new KaaTCPComunicationParameters("localhost", 1234));
        KaaTcpParameters parameters = KaaTcpChannel.getKaaTcpParametersFromSupportedChannel(channel);
        SupportedChannel channel1 = KaaTcpChannel.getSupportedChannelFromKaaTcpParameters(parameters);
        assertEquals(channel, channel1);
    }

    @Test
    public void testGetSupportedTransportTypes() {
        List<TransportType> transports =  KaaTcpChannel.getSupportedTransportTypes();
        assertNotNull(transports);
        assertEquals(5, transports.size());
        assertEquals(TransportType.CONFIGURATION, transports.get(0));
        assertEquals(TransportType.EVENT, transports.get(1));
        assertEquals(TransportType.NOTIFICATION, transports.get(2));
        assertEquals(TransportType.PROFILE, transports.get(3));
        assertEquals(TransportType.USER, transports.get(4));
    }

    @Test
    public void testGetType() {
        assertEquals(ChannelType.KAATCP, KaaTcpChannel.getType());
    }

    @Test
    public void testIsTransportSupported() {
        assertFalse(KaaTcpChannel.isTransportSupported(TransportType.LOGGING));
        assertTrue(KaaTcpChannel.isTransportSupported(TransportType.EVENT));
    }

}
