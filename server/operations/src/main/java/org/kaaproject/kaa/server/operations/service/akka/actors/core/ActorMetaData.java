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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.HashSet;
import java.util.Set;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;

import akka.actor.ActorRef;

public class ActorMetaData{
    ActorRef actorRef;
    private Set<Integer> pendingRequests;
    
    ActorMetaData(ActorRef actorRef) {
        super();
        this.actorRef = actorRef;
        this.pendingRequests = new HashSet<>();
    }
    
    boolean registerRequest(Integer requestHash){
        return pendingRequests.add(requestHash);
    }
    
    boolean registerResponse(Integer requestHash){
        return pendingRequests.remove(requestHash);
    }
    
    int getPendingRequestsSize(){
        return pendingRequests.size();
    }
    
    public static int getHash(EndpointAwareMessage message){
        //TODO: improve;
        return message.hashCode();
    }
}