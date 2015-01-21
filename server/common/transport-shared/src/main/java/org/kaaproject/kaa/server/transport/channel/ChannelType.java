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
 * Represents types of channels supported by Kaa server components. Messages
 * that are dispatched by specific channel may be handled differently based on
 * channel type.
 * 
 * @author Andrew Shvayka
 *
 */
public enum ChannelType {

    /**
     * Sync channel identifies that messages from this channel require immediate
     * reply. One of the sync channels is regular http channel
     */
    SYNC(false, false),

    /**
     * Sync with timeout channel identifies that messages from this channel does
     * not require immediate reply. Reply to the incoming message may delayed if
     * and only if there is no updates from Kaa server in the reply. One of the
     * example channels is http long poll channel
     */
    SYNC_WITH_TIMEOUT(true, false),

    /**
     * Async channel identifies that messages from this channel does not require
     * reply at all. Incoming and outcoming messages are independent, thus
     * communication is asynchronous. Server will push updates to this channel
     * as soon as they arrive.
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
