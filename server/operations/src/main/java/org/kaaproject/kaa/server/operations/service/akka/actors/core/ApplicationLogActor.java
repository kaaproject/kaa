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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.notification.ThriftNotificationMessage;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.UntypedActor;
import akka.japi.Creator;

/**
 * The Class ApplicationLogActor
 */
public class ApplicationLogActor extends UntypedActor {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLogActor.class);
    
    private final String applicationToken;
    
    private final ApplicationLogActorMessageProcessor messageProcessor;
    /**
     * Instantiates a new application log actor.
     *
     * @param logAppenderService
     *
     *            the log appender service
     */
    private ApplicationLogActor(LogAppenderService logAppenderService, ApplicationService applicationService, String applicationToken) {
        this.applicationToken = applicationToken;
        this.messageProcessor = new ApplicationLogActorMessageProcessor(logAppenderService, applicationService, applicationToken);
    }

    /**
     * The Class ActorCreator.
     */
    public static class ActorCreator implements Creator<ApplicationLogActor> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The log appender service. */
        private final LogAppenderService logAppenderService;

        /** The log application service. */
        private final ApplicationService applicationService;

        private final String applicationToken;

        /**
         * Instantiates a new actor creator.
         *
         * @param logAppenderService
         *            the log appender service
         */
        public ActorCreator(LogAppenderService logAppenderService, ApplicationService applicationService, String applicationToken) {
            super();
            this.logAppenderService = logAppenderService;
            this.applicationService = applicationService;
            this.applicationToken = applicationToken;
        }

        /*
         * (non-Javadoc)
         *
         * @see akka.japi.Creator#create()
         */
        @Override
        public ApplicationLogActor create() throws Exception {
            return new ApplicationLogActor(logAppenderService, applicationService, applicationToken);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOG.debug("[{}] Received: {}", applicationToken, message);
        if (message instanceof LogEventPackMessage) {
            messageProcessor.processLogEventPack(getContext(), (LogEventPackMessage)message);
        } else if(message instanceof ThriftNotificationMessage) {
            LOG.debug("[{}] Received thrift notification message: {}", applicationToken, message);
            Notification notification = ((ThriftNotificationMessage) message).getNotification();
            messageProcessor.processLogAppenderNotification(notification);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#preStart()
     */
    @Override
    public void preStart() {
        LOG.info("[{}] Starting ", applicationToken);
    }

    /*
     * (non-Javadoc)
     *
     * @see akka.actor.UntypedActor#postStop()
     */
    @Override
    public void postStop() {
        messageProcessor.stop();
        LOG.info("[{}] Stoped ", applicationToken);
    }
}
