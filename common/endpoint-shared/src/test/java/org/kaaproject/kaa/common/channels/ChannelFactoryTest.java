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

import org.junit.Test;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.communication.HttpLongPollParameters;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;

/**
 * @author Andrey Panasenko
 *
 */
public class ChannelFactoryTest {

    @Test
    public void testGetChannelFromChannelType() {
        assertEquals(ChannelType.BOOTSTRAP,ChannelFactory.getChannelFromChannelType(ChannelType.BOOTSTRAP).getChannelType());
        assertEquals(ChannelType.HTTP,ChannelFactory.getChannelFromChannelType(ChannelType.HTTP).getChannelType());
        assertEquals(ChannelType.HTTP_LP,ChannelFactory.getChannelFromChannelType(ChannelType.HTTP_LP).getChannelType());
    }

    @Test
    public void testGetChannelFromChannel() throws ParsingException {
        assertEquals(ChannelType.BOOTSTRAP,ChannelFactory.getChannelFromSupportedChannel(new SupportedChannel(ChannelType.BOOTSTRAP, null)).getChannelType());
        assertEquals(ChannelType.HTTP,ChannelFactory.getChannelFromSupportedChannel(new SupportedChannel(ChannelType.HTTP, null)).getChannelType());
        assertEquals(ChannelType.HTTP_LP,ChannelFactory.getChannelFromSupportedChannel(new SupportedChannel(ChannelType.HTTP_LP, null)).getChannelType());
    }


    @Test
    public void testTransformHttpChannelToAndFromSupportedChannel() {
        HttpParameters params = new HttpParameters();
        assertNotNull(params);
        params.setHostName("host1");
        params.setPort(100);

        SupportedChannel suppChannel = HttpChannel.getSupportedChannelFromHttpParameters(params);
        assertNotNull(suppChannel);

        try {
            HttpParameters afterParams = HttpChannel.getHttpParametersFromSupportedChannel(suppChannel);
            assertEquals(params, afterParams);
        } catch (ParsingException e) {
            fail(e.toString());
        }
    }


    @Test
    public void testTransformHttpLPChannelToAndFromSupportedChannel() {
        HttpLongPollParameters params = new HttpLongPollParameters();
        assertNotNull(params);
        params.setHostName("host1");
        params.setPort(100);

        SupportedChannel suppChannel = HttpLongPollChannel.getSupportedChannelFromHttpLongPollParameters(params);
        assertNotNull(suppChannel);

        try {
            HttpLongPollParameters afterParams = HttpLongPollChannel.getHttpLongPollParametersFromSupportedChannel(suppChannel);
            assertEquals(params, afterParams);
        } catch (ParsingException e) {
            fail(e.toString());
        }
    }
    
    @Test
    public void testGetChannelFromSupportedChannel() {
        
        String hostName = "localhost";
        int port = 100;
        HttpParameters httpCommunicationParameters = new HttpParameters();
        httpCommunicationParameters.setHostName(hostName);
        httpCommunicationParameters.setPort(port);
        
        SupportedChannel httpSupportedChannel = new SupportedChannel(ChannelType.HTTP, httpCommunicationParameters);
        SupportedChannel httpLpSupportedChannel = new SupportedChannel(ChannelType.HTTP_LP, httpCommunicationParameters);
        SupportedChannel bsSupportedChannel = new SupportedChannel(ChannelType.BOOTSTRAP, null);
        try {
            Channel httpChannel = ChannelFactory.getChannelFromSupportedChannel(httpSupportedChannel);
            Channel httpLpChannel = ChannelFactory.getChannelFromSupportedChannel(httpLpSupportedChannel);
            Channel bsChannel = ChannelFactory.getChannelFromSupportedChannel(bsSupportedChannel);
            assertNotNull(httpChannel);
            assertNotNull(httpLpChannel);
            assertNotNull(bsChannel);
            assertEquals(ChannelType.HTTP, httpChannel.getChannelType());
            assertEquals(ChannelType.HTTP_LP, httpLpChannel.getChannelType());
            assertEquals(ChannelType.BOOTSTRAP, bsChannel.getChannelType());
        } catch (ParsingException e) {
            fail(e.toString());
        }
    }
}
