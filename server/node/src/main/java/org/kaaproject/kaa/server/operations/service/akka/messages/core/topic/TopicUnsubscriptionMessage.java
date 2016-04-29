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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.topic;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;

import akka.actor.ActorRef;


/**
 * The Class TopicRegistrationRequestMessage.
 */
public class TopicUnsubscriptionMessage extends EndpointAwareMessage{

    /** The topic id. */
    private final String topicId;

    /**
     * Instantiates a new topic registration request message.
     *
     * @param topicId the topic id
     * @param appToken the app token
     * @param key the key
     * @param originator the originator
     */
    public TopicUnsubscriptionMessage(String topicId, String appToken, EndpointObjectHash key,  ActorRef originator) {
        super(appToken, key, originator);
        this.topicId = topicId;
    }

    /**
     * Gets the topic id.
     *
     * @return the topic id
     */
    public String getTopicId() {
        return topicId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopicUnsubscriptionMessage [topicId=");
        builder.append(topicId);
        builder.append("]");
        return builder.toString();
    }
}
