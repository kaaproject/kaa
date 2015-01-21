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
package org.kaaproject.kaa.server.transport.channel;

/**
 * Represents transport channel context. Provides methods to write data to
 * channel.
 * 
 * @author Andrew Shvayka
 *
 */
public interface ChannelContext {

    /**
     * Writes and flushes given object
     * 
     * @param response
     *            - object to write
     */
    void writeAndFlush(Object response);
    
    /**
     * Writes given object but don't flush it
     * @param object
     */
    void write(Object object);

    /**
     * Sends flush command to given channel
     */
    void flush();
    
    /**
     * Notifies channel context about exceptions related to processing messages
     * for this channel
     * 
     * @param e  exception caught
     */
    void fireExceptionCaught(Exception e);

}
