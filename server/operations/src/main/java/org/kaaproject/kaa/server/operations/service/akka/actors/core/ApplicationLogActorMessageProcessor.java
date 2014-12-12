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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.MultiLogDeliveryCallback;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.SingleLogDeliveryCallback;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class ApplicationLogActorMessageProcessor {
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLogActorMessageProcessor.class);

    private final LogAppenderService logAppenderService;

    private final Map<String, LogAppender> logAppenders;
    
    private final Map<LogAppenderFilterKey, List<LogAppender>> logAppendersCache;

    private final String applicationId;

    private final String applicationToken;

    private final Map<Integer, LogSchema> logSchemas;
    
    private final VoidCallback voidCallback;

    public ApplicationLogActorMessageProcessor(LogAppenderService logAppenderService,
            ApplicationService applicationService, String applicationToken) {
        super();
        this.logAppenderService = logAppenderService;
        this.applicationToken = applicationToken;
        this.applicationId = applicationService.findAppByApplicationToken(applicationToken).getId();
        this.logAppenders = new HashMap<>();
        this.logAppendersCache = new HashMap<>();
        this.logSchemas = new HashMap<>();
        this.voidCallback = new VoidCallback();
        for (LogAppender appender : logAppenderService.getApplicationAppenders(applicationId)) {
            logAppenders.put(appender.getAppenderId(), appender);
        }
    }

    protected void processLogEventPack(ActorContext context, LogEventPackMessage message) {
        LOG.debug("[{}] Processing log event pack with {} appenders", applicationToken, logAppenders.size());
        LogSchema logSchema = fetchLogSchema(message);
        List<LogAppender> required = filterAppenders(logSchema.getVersion(), true);
        List<LogAppender> optional = filterAppenders(logSchema.getVersion(), false);
        if (required.size() > 0 || optional.size() > 0) {
        	for(LogAppender logAppender : optional){
        		 logAppender.doAppend(message.getLogEventPack(), voidCallback);
        	}
        	
            LogDeliveryCallback callback;
            if (required.size() > 1) {
                callback = new MultiLogDeliveryCallback(message.getOriginator(), message.getRequestId(), required.size());
            } else {
                callback = new SingleLogDeliveryCallback(message.getOriginator(), message.getRequestId());
            }
            try {
                for (LogAppender logAppender : required) {
                    logAppender.doAppend(message.getLogEventPack(), callback);
                }
            } catch (Exception e) {
                LOG.debug("Error during execution of appender(s)", e);
                sendErrorMessageToEndpoint(message, LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);
            }
        } else {
            sendErrorMessageToEndpoint(message, LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED);
        }
    }
    
    public List<LogAppender> filterAppenders(int schemaVersion, boolean confirmDelivery){
    	LogAppenderFilterKey key = new LogAppenderFilterKey(schemaVersion, confirmDelivery);
    	List<LogAppender> result = logAppendersCache.get(key);
    	if(result == null){
    		result = new ArrayList<LogAppender>();
    		for(LogAppender appender : logAppenders.values()){
    			if(appender.isSchemaVersionSupported(schemaVersion) && appender.isDeliveryConfirmationRequired() == confirmDelivery){
    				result.add(appender);
    			}
    		}
    	}
    	return result;
    }

	private LogSchema fetchLogSchema(LogEventPackMessage message) {
		LogSchema logSchema = message.getLogSchema();
        if (logSchema == null) {
            logSchema = logSchemas.get(message.getLogSchemaVersion());
            if (logSchema == null) {
                logSchema = logAppenderService.getLogSchema(applicationId, message.getLogSchemaVersion());
                logSchemas.put(message.getLogSchemaVersion(), logSchema);
            }
            message.setLogSchema(logSchema);
        }
		return logSchema;
	}

    private void sendErrorMessageToEndpoint(LogEventPackMessage message, LogDeliveryErrorCode errorCode) {
    	if(message.getOriginator() != null){
    		message.getOriginator().tell(new LogDeliveryMessage(message.getRequestId(), false, errorCode),
                ActorRef.noSender());
    	}else{
    		LOG.warn("[{}] Can't send error message to unknown originator.", applicationToken);
    	}
    }

    protected void processLogAppenderNotification(Notification notification) {
        LOG.debug("Process log appender notification [{}]", notification);
        String appenderId = notification.getAppenderId();
        switch (notification.getOp()) {
        case ADD_LOG_APPENDER:
            addLogAppender(appenderId);
            break;
        case REMOVE_LOG_APPENDER:
            removeLogAppender(appenderId);
            break;
        case UPDATE_LOG_APPENDER:
            removeLogAppender(appenderId);
            addLogAppender(appenderId);
            break;
        default:
            LOG.debug("[{}][{}] Operation [{}] is not supported.", applicationToken, appenderId, notification.getOp());
        }
    }

    protected void stop() {
        for (LogAppender logAppender : logAppenders.values()) {
            LOG.info("[{}] Closing appender [{}] with name {}", applicationToken, logAppender.getAppenderId(),
                    logAppender.getName());
            logAppender.close();
        }
    }

    private void addLogAppender(String appenderId) {
        LOG.info("[{}] Adding log appender with id [{}].", applicationId, appenderId);
        if (!logAppenders.containsKey(appenderId)) {
            LogAppender logAppender = logAppenderService.getApplicationAppender(appenderId);
            if (logAppender != null) {
                addAppender(appenderId, logAppender);
            } else {
                LOG.info("[{}] Log appender [{}] registered.", applicationId, appenderId);
            }
        } else {
            LOG.info("[{}] Log appender [{}] is already registered.", applicationId, appenderId);
        }
    }

    private void removeLogAppender(String appenderId) {
        if (logAppenders.containsKey(appenderId)) {
            LOG.info("[{}] Closing log appender with id [{}].", applicationToken, appenderId);
            removeAppender(appenderId).close();
        } else {
            LOG.warn("[{}] Can't remove unregistered appender with id [{}]", applicationToken, appenderId);
        }
    }

	private LogAppender removeAppender(String appenderId) {
		logAppendersCache.clear();
		return logAppenders.remove(appenderId);
	}
    
	private void addAppender(String appenderId, LogAppender logAppender) {
		logAppendersCache.clear();
		logAppenders.put(appenderId, logAppender);
	}
	
    private static final class VoidCallback implements LogDeliveryCallback {
		@Override
		public void onSuccess() {
		}

		@Override
		public void onRemoteError() {
		}

		@Override
		public void onInternalError() {
		}

		@Override
		public void onConnectionError() {
		}
	}

	private static final class LogAppenderFilterKey{
    	private int schemaVersion;
    	private boolean confirmDelivery;
		
    	private LogAppenderFilterKey(int schemaVersion, boolean confirmDelivery) {
			super();
			this.schemaVersion = schemaVersion;
			this.confirmDelivery = confirmDelivery;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (confirmDelivery ? 1231 : 1237);
			result = prime * result + schemaVersion;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LogAppenderFilterKey other = (LogAppenderFilterKey) obj;
			if (confirmDelivery != other.confirmDelivery)
				return false;
			if (schemaVersion != other.schemaVersion)
				return false;
			return true;
		}
    }	
}
