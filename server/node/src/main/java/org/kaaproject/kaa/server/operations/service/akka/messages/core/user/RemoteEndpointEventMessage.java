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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

import org.kaaproject.kaa.server.common.dao.EndpointService;
import org.kaaproject.kaa.server.operations.service.akka.actors.core.user.LocalUserActor;
import org.kaaproject.kaa.server.operations.service.event.RemoteEndpointEvent;

/**
 * Represents message with events that was sent from remote server.
 * Originator: {@link EndpointService}
 * Destination: {@link LocalUserActor}
 * 
 * @author Andrew Shvayka
 *
 */
public class RemoteEndpointEventMessage implements UserAwareMessage, TenantAwareMessage{

    private final RemoteEndpointEvent event;

    public RemoteEndpointEventMessage(RemoteEndpointEvent event) {
        super();
        this.event = event;
    }

    public RemoteEndpointEvent getEvent() {
        return event;
    }

    @Override
    public String getTenantId() {
        return event.getTenantId();
    }

    @Override
    public String getUserId() {
        return event.getUserId();
    }
}
