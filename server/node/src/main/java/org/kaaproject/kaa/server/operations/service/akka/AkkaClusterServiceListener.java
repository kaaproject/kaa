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

package org.kaaproject.kaa.server.operations.service.akka;

import org.kaaproject.kaa.server.operations.service.akka.messages.core.lb.ClusterUpdateMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointRouteMessage;
import org.kaaproject.kaa.server.operations.service.cluster.ClusterServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;

public class AkkaClusterServiceListener implements ClusterServiceListener {

    private static final Logger LOG = LoggerFactory.getLogger(AkkaClusterServiceListener.class);

    private final ActorRef opsActor;

    public AkkaClusterServiceListener(ActorRef opsActor) {
        super();
        this.opsActor = opsActor;
    }
    
    @Override
    public void onRouteMsg(EndpointRouteMessage msg) {
        LOG.debug("Sending message {} to OPS actor", msg);
        opsActor.tell(msg, ActorRef.noSender());
    }
    
    @Override
    public void onEndpointActorMsg(EndpointActorMsg msg) {
        LOG.debug("Sending message {} to OPS actor", msg);
        opsActor.tell(msg, ActorRef.noSender());
    }

    @Override
    public void onClusterUpdated() {
        LOG.trace("Detected cluster topology update");
        opsActor.tell(new ClusterUpdateMessage(), ActorRef.noSender());
    }
}
