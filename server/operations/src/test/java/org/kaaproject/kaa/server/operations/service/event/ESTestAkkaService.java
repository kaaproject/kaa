/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.event;

import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.AkkaService;
import org.kaaproject.kaa.server.operations.service.akka.messages.io.NettyCommandAwareMessage;

import akka.actor.ActorSystem;

/**
 * @author Andrey Panasenko
 *
 */
public class ESTestAkkaService implements AkkaService {

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.akka.AkkaService#getActorSystem()
     */
    @Override
    public ActorSystem getActorSystem() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.akka.AkkaService#process(org.kaaproject.kaa.server.operations.service.akka.messages.io.NettyCommandAwareMessage)
     */
    @Override
    public void process(NettyCommandAwareMessage message) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.akka.AkkaService#onRedirectionRule(org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule)
     */
    @Override
    public void onRedirectionRule(RedirectionRule redirectionRule) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.kaaproject.kaa.server.operations.service.akka.AkkaService#onNotification(org.kaaproject.kaa.server.common.thrift.gen.operations.Notification)
     */
    @Override
    public void onNotification(Notification notification) {
        // TODO Auto-generated method stub

    }

}
