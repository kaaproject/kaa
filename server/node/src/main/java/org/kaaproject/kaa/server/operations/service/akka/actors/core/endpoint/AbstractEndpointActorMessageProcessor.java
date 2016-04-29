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

package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.EndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.route.ThriftEndpointActorMsg;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.session.ActorTimeoutMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public abstract class AbstractEndpointActorMessageProcessor<T extends AbstractEndpointActorState> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEndpointActorMessageProcessor.class);

    protected final T state;

    /** The operations service. */
    protected final OperationsService operationsService;

    /** The app token. */
    protected final String appToken;

    /** The key. */
    protected final EndpointObjectHash key;

    /** The actor key. */
    protected final String actorKey;

    /** The endpoint key. */
    protected final String endpointKey;

    private final long inactivityTimeout;

    public AbstractEndpointActorMessageProcessor(T state, OperationsService operationsService, String appToken, EndpointObjectHash key,
            String actorKey, String endpointKey, long inactivityTimeout) {
        super();
        this.state = state;
        this.operationsService = operationsService;
        this.inactivityTimeout = inactivityTimeout;
        this.appToken = appToken;
        this.key = key;
        this.actorKey = actorKey;
        this.endpointKey = endpointKey;
    }

    public long getInactivityTimeout() {
        return inactivityTimeout;
    }

    public void processActorTimeoutMessage(ActorContext context, ActorTimeoutMessage message) {
        if (state.getLastActivityTime() <= message.getLastActivityTime()) {
            LOG.debug("[{}][{}] Request stop of endpoint actor due to inactivity timeout", endpointKey, actorKey);
            tellParent(context, new EndpointStopMessage(key, actorKey, context.self()));
        }
    }

    protected void tellParent(ActorContext context, Object response) {
        context.parent().tell(response, context.self());
    }

    protected void tellActor(ActorContext context, ActorRef target, Object message) {
        target.tell(message, context.self());
    }

    public void processEndpointActorMsg(ActorContext context, EndpointActorMsg msg) {
        if (msg instanceof ThriftEndpointActorMsg) {
            processThriftMsg(context, (ThriftEndpointActorMsg<?>) msg);
        }
    }

    abstract protected void processThriftMsg(ActorContext context, ThriftEndpointActorMsg<?> msg);
}
