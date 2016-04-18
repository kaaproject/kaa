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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import akka.actor.ActorRef;


public class EndpointStopMessage {
    private final EndpointObjectHash endpointKey;
    private final String actorKey;
    private final ActorRef originator;

    public EndpointStopMessage(EndpointObjectHash endpointKey, String actorKey, ActorRef originator) {
        super();
        this.endpointKey = endpointKey;
        this.actorKey = actorKey;
        this.originator = originator;
    }

    public EndpointObjectHash getEndpointKey() {
        return endpointKey;
    }

    public String getActorKey() {
        return actorKey;
    }

    public ActorRef getOriginator() {
        return originator;
    }
}
