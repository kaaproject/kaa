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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.session;

import java.util.UUID;

public class ChannelTimeoutMessage implements TimeoutMessage {

    private final UUID channelUuid;
    private final long lastActivityTime;

    public ChannelTimeoutMessage(UUID uuid, long lastActivityTime) {
        super();
        this.channelUuid = uuid;
        this.lastActivityTime = lastActivityTime;
    }

    public UUID getChannelUuid() {
        return channelUuid;
    }

    public long getLastActivityTime() {
        return lastActivityTime;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ChannelTimeoutMessage [lastActivityTime=");
        builder.append(lastActivityTime);
        builder.append("]");
        return builder.toString();
    }
}
