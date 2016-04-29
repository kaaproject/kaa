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

package org.kaaproject.kaa.server.operations.service.akka;

import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.common.thrift.gen.operations.RedirectionRule;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.user.UserConfigurationUpdate;
import org.kaaproject.kaa.server.transport.message.MessageHandler;

import akka.actor.ActorSystem;

/**
 * The Interface AkkaService.
 */
public interface AkkaService extends MessageHandler {

    /**
     * Gets the actor system.
     *
     * @return the actor system
     */
    ActorSystem getActorSystem();

    /**
     * On redirection rule set
     * 
     * @param redirectionRule the redirection rule
     */
    void onRedirectionRule(RedirectionRule redirectionRule);

    /**
     * On notification.
     *
     * @param notification
     *            the notification
     */
    void onNotification(Notification notification);

    /**
     * Reports update of user configuration to the global user actor
     * 
     * @param update
     *            - user configuration update
     */
    void onUserConfigurationUpdate(UserConfigurationUpdate update);

    void setStatusListener(AkkaStatusListener defaultLoadBalancingService, long loadStatsUpdateFrequency);

    void removeStatusListener();
}
