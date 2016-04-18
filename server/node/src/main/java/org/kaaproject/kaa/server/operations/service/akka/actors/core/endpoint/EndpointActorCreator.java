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