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

package org.kaaproject.kaa.server.operations.service.event;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.sync.Event;

public final class EndpointEvent {

    private final UUID uuid;
    private final EndpointObjectHash sender;
    private final Event event;
    private final long createTime;
    private int version;

    public EndpointEvent(EndpointObjectHash sender, Event event) {
        this(sender, event, UUID.randomUUID(), System.currentTimeMillis());
    }

    public EndpointEvent(EndpointObjectHash sender, Event event, UUID uuid, long createTime) {
        this(sender, event, uuid, createTime, 0);
    }

    public EndpointEvent(EndpointObjectHash sender, Event event, UUID uuid, long createTime, int version) {
        super();
        this.sender = sender;
        this.event = event;
        this.uuid = uuid;
        this.createTime = createTime;
        this.version = version;
    }

    public UUID getId() {
        return uuid;
    }

    public EndpointObjectHash getSender() {
        return sender;
    }

    public Event getEvent() {
        return event;
    }

    public String getEventClassFQN() {
        return event.getEventClassFQN();
    }

    public String getTarget() {
        return event.getTarget();
    }

    public long getCreateTime() {
        return createTime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EndpointEvent other = (EndpointEvent) obj;
        if (uuid == null) {
            if (other.uuid != null) {
                return false;
            }
        } else if (!uuid.equals(other.uuid)) {
            return false; 
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointEvent [uuid=");
        builder.append(uuid);
        builder.append(", sender=");
        builder.append(sender);
        builder.append(", event=");
        builder.append(event);
        builder.append("]");
        return builder.toString();
    }

}
