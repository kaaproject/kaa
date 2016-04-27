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

import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode;

import akka.actor.ActorRef;

public abstract class AbstractActorCallback implements LogDeliveryCallback {

    /** The actor reference. */
    private final ActorRef actor;

    /** The request id. */
    private final int requestId;

    protected AbstractActorCallback(ActorRef actor, int requestId) {
        super();
        this.actor = actor;
        this.requestId = requestId;
    }

    protected void sendSuccessToEndpoint() {
        sendMessageToEndpoint(new LogDeliveryMessage(this.requestId, true));
    }

    protected void sendFailureToEndpoint(LogDeliveryErrorCode errorCode) {
        sendMessageToEndpoint(new LogDeliveryMessage(this.requestId, false, errorCode));
    }

    private void sendMessageToEndpoint(LogDeliveryMessage message) {
        actor.tell(message, ActorRef.noSender());
    }

    @Override
    public void onInternalError() {
        sendFailureToEndpoint(LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);
    }

    @Override
    public void onConnectionError() {
        sendFailureToEndpoint(LogDeliveryErrorCode.REMOTE_CONNECTION_ERROR);
    }

    @Override
    public void onRemoteError() {
        sendFailureToEndpoint(LogDeliveryErrorCode.REMOTE_INTERNAL_ERROR);
    }
}
