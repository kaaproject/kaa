package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointStopMessage;
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

}
