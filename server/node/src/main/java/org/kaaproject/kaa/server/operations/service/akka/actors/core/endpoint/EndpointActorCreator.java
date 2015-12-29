package org.kaaproject.kaa.server.operations.service.akka.actors.core.endpoint;

import java.util.UUID;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;

import akka.japi.Creator;

/**
 * The Class ActorCreator.
 */
public abstract class EndpointActorCreator<T> implements Creator<T> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Akka service context */
    protected final AkkaContext context;

    /** The actor key */
    protected final String actorKey;

    /** The app token. */
    protected final String appToken;

    /** The endpoint key. */
    protected final EndpointObjectHash endpointKey;

    /**
     * Instantiates a new actor creator.
     *
     * @param context
     *            the context
     * @param endpointActorKey
     *            the endpoint actor key
     * @param appToken
     *            the app token
     * @param endpointKey
     *            the endpoint key
     */
    public EndpointActorCreator(AkkaContext context, String endpointActorKey, String appToken, EndpointObjectHash endpointKey) {
        super();
        this.context = context;
        this.actorKey = endpointActorKey;
        this.appToken = appToken;
        this.endpointKey = endpointKey;
    }
    
    public static String generateActorKey(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}