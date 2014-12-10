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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
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

    private final LogAppenderService logAppenderService;

    /** The log appenders */
    private List<LogAppender> logAppenders;

    private final String applicationId;

    private final String applicationToken;

    private final Map<Integer, LogSchema> logSchemas;

    /**
     * Instantiates a new application log actor.
     *
     * @param logAppenderService
     *
     *            the log appender service
     */
    private ApplicationLogActor(LogAppenderService logAppenderService, ApplicationService applicationService, String applicationToken) {
        this.logAppenderService = logAppenderService;
        this.applicationId = applicationService.findAppByApplicationToken(applicationToken).getId();
        this.logAppenders = logAppenderService.getApplicationAppenders(applicationId);
        this.applicationToken = applicationToken;
        this.logSchemas = new HashMap<>();
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
            processLogEventPack((LogEventPackMessage)message);
        } else if(message instanceof ThriftNotificationMessage) {
            LOG.debug("[{}] Received thrift notification message: {}", applicationToken, message);
            Notification notification = ((ThriftNotificationMessage) message).getNotification();
            processLogAppenderNotification(notification);
        }
    }

    private void processLogEventPack(LogEventPackMessage message) {
        LOG.debug("[{}] Processing log event pack with {} appenders", applicationToken, logAppenders.size());
        LogSchema logSchema = message.getLogSchema();
        if (logSchema == null) {
            logSchema = logSchemas.get(message.getLogSchemaVersion());
            if (logSchema == null) {
                logSchema = logAppenderService.getLogSchema(applicationId, message.getLogSchemaVersion());
                logSchemas.put(message.getLogSchemaVersion(), logSchema);
            }
            message.setLogSchema(logSchema);
        }
        for (LogAppender logAppender : logAppenders) {
        	if(logAppender.isSchemaVersionSupported(logSchema.getVersion())){
        		logAppender.doAppend(message.getLogEventPack());
        	}
        }
    }

    private void processLogAppenderNotification(Notification notification) {
        LOG.debug("Process log appender notification [{}]", notification);
        switch (notification.getOp()) {
            case ADD_LOG_APPENDER:
                LOG.debug("[{}] Add new appender to list of log appenders.", applicationToken);
                addLogAppender(notification.getAppenderId());
                break;
            case REMOVE_LOG_APPENDER:
                LOG.debug("[{}] Remove appender from list of log appenders.", applicationToken);
                removeLogAppender(notification.getAppenderId());
                break;
            case UPDATE_LOG_APPENDER:
                LOG.debug("[{}] Update configuration of existing appender", applicationToken);
                String id = notification.getAppenderId();
                removeLogAppender(id);
                addLogAppender(id);
                break;
            default:
                LOG.debug("[{}] Operation [{}] does not support.", applicationToken, notification.getOp());
        }
    }

    private void addLogAppender(String appenderId) {
        LOG.debug("Adding log appender with id [{}].", appenderId);
        LogAppender logAppender = logAppenderService.getApplicationAppender(appenderId);
        if (logAppender != null) {
            logAppenders.add(logAppender);
        }
        LOG.debug("Fetch log appender [{}] and add to list of log appenders.", logAppender);
    }

    private void removeLogAppender(String appenderId) {
        LOG.debug("Removing log appender with id [{}].", appenderId);
        for (int i = 0; i < logAppenders.size(); i++) {
            LogAppender appender = logAppenders.get(i);
            if (appender != null && appender.getAppenderId().equals(appenderId)) {
                LOG.debug("Close and remo log appender with id [{}].", appenderId);
                appender.close();
                logAppenders.remove(i);
                break;
            }
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
        for (LogAppender logAppender : logAppenders) {
            logAppender.close();
        }
        LOG.info("[{}] Stoped ", applicationToken);
    }
}
