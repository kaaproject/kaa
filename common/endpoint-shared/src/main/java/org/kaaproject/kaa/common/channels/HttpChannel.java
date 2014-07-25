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

import java.util.List;

import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.HTTPComunicationParameters;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;
import org.kaaproject.kaa.common.channels.communication.HttpParameters;

/**
 * HttpChannel Class.
 * 
 * @author Andrey Panasenko
 *
 */
public class HttpChannel extends Channel {
    private static ChannelType type = ChannelType.HTTP;
    
    private static TransportType[] supportedTransports = new TransportType[] {
        TransportType.CONFIGURATION,
        TransportType.EVENT,
        TransportType.NOTIFICATION,
        TransportType.PROFILE,
        TransportType.USER,
        TransportType.LOGGING
    };
    
    /**
     * Default Constructor.
     */
    public HttpChannel() {
        
    }

    /**
     * Parse Avro SupportedChannel to extract HTTP Communications Parameters
     * @param supportedChannel Avro SupportedChannel
     * @return HttpParameters
     * @throws ParsingException if parsing failed.
     */
    public static HttpParameters getHttpParametersFromSupportedChannel(SupportedChannel supportedChannel) 
        throws ParsingException {
        Object obj = supportedChannel.getCommunicationParameters();
        if (obj instanceof HTTPComunicationParameters) {
            HttpParameters params = new HttpParameters();
            params.setHostName(((HTTPComunicationParameters)obj).getHostName());
            params.setPort(((HTTPComunicationParameters)obj).getPort().intValue());
            return params;
        } else {
            throw new ParsingException("Error: Communication parameters with incorrect Class.");
        }
    }
    
    /**
     * Create Avro SupportedChannel object from HttpParameters
     * @param params HttpParameters 
     * @return SupportedChannel
     */
    public static SupportedChannel getSupportedChannelFromHttpParameters(HttpParameters params) {
        HTTPComunicationParameters communicationParameters = new HTTPComunicationParameters(params.getHostName(), params.getPort());
        return new SupportedChannel(getType(), (Object)communicationParameters );
    }
    
    /**
     * Return List of supported transports for Channel
     * @return List<TransportTypes> of supported transports.
     */
    public static List<TransportType> getSupportedTransportTypes() {
        return listTransportTypesFromArray(supportedTransports);
    }
    
    /**
     * Return AVRO ChannelType for Channel
     * @return ChannelType of Channel
     */
    public static ChannelType getType() {
        return type;
    }

    /**
     * Check if specified TransportType is supported by specific Channel 
     * @param type TransportTypes
     * @return boolean, true if specified type is supported by Channel
     */
    public static boolean isTransportSupported(TransportType type) {
        return isTransportTypeSupported(supportedTransports, type);
    }
    
    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.Channel#getSupportedTransports()
     */
    @Override
    public List<TransportType> getSupportedTransports() {
        return getSupportedTransportTypes();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.Channel#getChannelType()
     */
    @Override
    public ChannelType getChannelType() {
        return getType();
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.common.channels.Channel#isTransportTypeSupported(org.kaaproject.kaa.common.TransportTypes)
     */
    @Override
    public boolean isTransportTypeSupported(TransportType type) {
        return isTransportTypeSupported(supportedTransports, type);
    }
    
    
}
