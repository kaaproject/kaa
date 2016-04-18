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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.logs;

import akka.actor.ActorRef;

/**
 * Implementation of LogDeliveryCallback that forwards message to dedicated
 * actor.
 */
public class SingleLogDeliveryCallback extends AbstractActorCallback {

    /**
     * Instantiates a new actor log delivery callback.
     * 
     * @param actor
     *            the actor
     * @param requestId
     *            the request id
     */
    public SingleLogDeliveryCallback(ActorRef actor, int requestId) {
        super(actor, requestId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback
     * #onSuccess()
     */
    @Override
    public void onSuccess() {
        sendSuccessToEndpoint();
    }
}