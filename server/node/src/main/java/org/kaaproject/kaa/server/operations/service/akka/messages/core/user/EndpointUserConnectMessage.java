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

package org.kaaproject.kaa.server.operations.service.akka.messages.core.user;

import java.util.List;

import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.endpoint.EndpointAwareMessage;
import org.kaaproject.kaa.server.operations.service.event.EventClassFamilyVersion;

import akka.actor.ActorRef;

/**
 * Represents intent of endpoint to connect to local user actor
 * 
 * @author Andrew Shvayka
 *
 */
public class EndpointUserConnectMessage extends EndpointAwareMessage implements UserAwareMessage {

    private final String userId;
    private final List<EventClassFamilyVersion> ecfVersions;
    private final int cfVersion;
    private final byte[] ucfHash;

    public EndpointUserConnectMessage(String userId, EndpointObjectHash endpointKey, List<EventClassFamilyVersion> ecfVersions,
            int cfVersion, byte[] ucfHash, String applicationToken, ActorRef originator) {
        super(applicationToken, endpointKey, originator);
        this.userId = userId;
        this.ecfVersions = ecfVersions;
        this.cfVersion = cfVersion;
        this.ucfHash = ucfHash;
    }

    public List<EventClassFamilyVersion> getEcfVersions() {
        return ecfVersions;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public int getCfVersion() {
        return cfVersion;
    }

    public byte[] getUcfHash() {
        return ucfHash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("EndpointUserConnectMessage [userId=");
        builder.append(userId);
        builder.append(", ecfVersions=");
        builder.append(ecfVersions);
        builder.append(", cfVersion=");
        builder.append(cfVersion);
        builder.append("]");
        return builder.toString();
    }
}
