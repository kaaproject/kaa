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

import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.bootstrap.gen.SupportedChannel;

/**
 * ChannelFactory Class.
 * Provides utilities to work with Avro SupportedChannel object.
 * Possible usage:
 * 
 * Channel channel =  getChannelFromSupportedChannel(supportedChannel);
 *      switch(channel.getChannelType()) {
 *          case HTTP:
 *              HttpParameters params = HttpChannel.getHttpParametersFromSupportedChannel(supportedChannel);
 *              ....
 *              break;
 *          case HTTP_LP:
 *              HttpLongPollParameters params = HttpLongPollChannel.getHttpLongPollParametersFromSupportedChannel(supportedChannel);
 *              ....
 *              break;
 *      }
 * 
 * @author Andrey Panasenko
 *
 */
public class ChannelFactory {
    public static Channel getChannelFromChannelType(ChannelType type) {
        switch (type) {
        case HTTP:
            return new HttpChannel();
        case HTTP_LP:
            return new HttpLongPollChannel();
        case BOOTSTRAP:
            return new BootstrapChannel();
        default:
            return null;
        }
    }
    
    public static Channel getChannelFromSupportedChannel(SupportedChannel supportedChannel) 
            throws ParsingException {
        ChannelType type = supportedChannel.getChannelType();
        switch (type) {
        case HTTP:
            return new HttpChannel();
        case HTTP_LP:
            return new HttpLongPollChannel();
        case BOOTSTRAP:
            return new BootstrapChannel();
        }
        throw new ParsingException("ChannelType "+type.toString()+" unparsed.");
    }
    
}
