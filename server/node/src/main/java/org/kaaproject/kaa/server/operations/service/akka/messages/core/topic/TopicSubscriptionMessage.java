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
public class TopicSubscriptionMessage extends EndpointAwareMessage{

    /** The topic id. */
    private final String topicId;

    /** The seq number. */
    private final int seqNumber;

    /** The system nf schema version. */
    private final int systemNfSchemaVersion;

    /** The user nf schema version. */
    private final int userNfSchemaVersion;

    /**
     * Instantiates a new topic registration request message.
     *
     * @param topicId the topic id
     * @param seqNumber the seq number
     * @param systemNfSchemaVersion the system nf schema version
     * @param userNfSchemaVersion the user nf schema version
     * @param appToken the app token
     * @param key the key
     * @param originator the originator
     */
    public TopicSubscriptionMessage(String topicId, int seqNumber, int systemNfSchemaVersion, int userNfSchemaVersion, String appToken, EndpointObjectHash key,  ActorRef originator) {
        super(appToken, key, originator);
        this.topicId = topicId;
        this.seqNumber = seqNumber;
        this.systemNfSchemaVersion = systemNfSchemaVersion;
        this.userNfSchemaVersion = userNfSchemaVersion;
    }

    /**
     * Gets the topic id.
     *
     * @return the topic id
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * Gets the seq number.
     *
     * @return the seq number
     */
    public int getSeqNumber() {
        return seqNumber;
    }

    /**
     * Gets the system nf schema version.
     *
     * @return the system nf schema version
     */
    public int getSystemNfSchemaVersion() {
        return systemNfSchemaVersion;
    }

    /**
     * Gets the user nf schema version.
     *
     * @return the user nf schema version
     */
    public int getUserNfSchemaVersion() {
        return userNfSchemaVersion;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TopicSubscriptionMessage [topicId=");
        builder.append(topicId);
        builder.append(", seqNumber=");
        builder.append(seqNumber);
        builder.append(", systemNfSchemaVersion=");
        builder.append(systemNfSchemaVersion);
        builder.append(", userNfSchemaVersion=");
        builder.append(userNfSchemaVersion);
        builder.append("]");
        return builder.toString();
    }
}
