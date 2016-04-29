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

import org.kaaproject.kaa.server.common.Base64Util;

public final class RemoteEndpointEvent {

    private final String tenantId;
    private final String userId;
    private final EndpointEvent event;
    private final RouteTableAddress recipient;

    public RemoteEndpointEvent(String tenantId, String userId, EndpointEvent event, RouteTableAddress recipient) {
        super();
        this.tenantId = tenantId;
        this.userId = userId;
        this.event = event;
        this.recipient = recipient;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public EndpointEvent getEvent() {
        return event;
    }

    public RouteTableAddress getRecipient() {
        return recipient;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        result = prime * result + ((recipient == null) ? 0 : recipient.hashCode());
        result = prime * result + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        RemoteEndpointEvent other = (RemoteEndpointEvent) obj;
        if (event == null) {
            if (other.event != null) {
                return false;
            }
        } else if (!event.equals(other.event)) {
            return false;
        }
        if (recipient == null) {
            if (other.recipient != null) {
                return false;
            }
        } else if (!recipient.equals(other.recipient)) {
            return false;
        }
        if (tenantId == null) {
            if (other.tenantId != null) {
                return false;
            }
        } else if (!tenantId.equals(other.tenantId)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RemoteEndpointEvent [tenantId=");
        builder.append(tenantId);
        builder.append(", userId=");
        builder.append(userId);
        builder.append(", event=");
        builder.append(event);
        builder.append(", recipient=");
        builder.append(recipient);
        builder.append("]");
        return builder.toString();
    }

    public EndpointEvent toLocalEvent() {
        this.event.getEvent().setTarget(Base64Util.encode(this.recipient.getEndpointKey().getData()));
        return this.event;
    }

}
