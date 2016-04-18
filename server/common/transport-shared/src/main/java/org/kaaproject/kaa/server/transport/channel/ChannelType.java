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
 * Represents the types of channels supported by Kaa server components. Messages
 * that are dispatched by a specific channel may be handled differently based on
 * the channel type.
 * 
 * @author Andrew Shvayka
 *
 */
public enum ChannelType {

    /**
     * The sync channel indicates that messages from this channel require an
     * immediate reply. One of the sync channels is a regular http channel.
     */
    SYNC(false, false),

    /**
     * The sync with timeout channel indicates that messages from this channel
     * does not require an immediate reply. A reply to the incoming message may
     * delayed if there are no updates from Kaa server in the reply. An example
     * of the sync with timeout channel is an http long poll channel.
     */
    SYNC_WITH_TIMEOUT(true, false),

    /**
     * The async channel indicates that messages from this channel does not
     * require any reply at all. Incoming and outcoming messages are
     * independent, thus communication is asynchronous. The server will push
     * updates to this channel as soon as they arrive.
     */
    ASYNC(false, true);

    private final boolean longPoll;
    private final boolean async;

    private ChannelType(boolean longPoll, boolean async) {
        this.longPoll = longPoll;
        this.async = async;
    }

    public boolean isAsync() {
        return async;
    }

    public boolean isLongPoll() {
        return longPoll;
    }
}
