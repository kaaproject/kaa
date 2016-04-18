/*
 * Copyright 2014-2016 CyberVision, Inc.
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
 * Represents a transport channel context and methods to write data to the
 * channel.
 * 
 * @author Andrew Shvayka
 *
 */
public interface ChannelContext {

    /**
     * Writes and flushes the given object
     * 
     * @param response
     *            the object to write
     */
    void writeAndFlush(Object response);

    /**
     * Writes the given object but doesn't flush it
     * 
     * @param object
     *            the object to write
     */
    void write(Object object);

    /**
     * Sends the flush command to the given channel
     */
    void flush();

    /**
     * Notifies the channel context about exceptions related to message
     * processing for this channel
     * 
     * @param e
     *            the caught exception
     */
    void fireExceptionCaught(Exception e);

}
