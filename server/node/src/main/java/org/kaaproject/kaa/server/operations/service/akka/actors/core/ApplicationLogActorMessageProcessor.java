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

package org.kaaproject.kaa.server.operations.service.akka.actors.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.common.dto.EndpointProfileDataDto;
import org.kaaproject.kaa.common.dto.EndpointProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ServerProfileSchemaDto;
import org.kaaproject.kaa.common.dto.ctl.CTLSchemaDto;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryErrorCode;
import org.kaaproject.kaa.server.common.log.shared.appender.LogSchema;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseLogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseProfileInfo;
import org.kaaproject.kaa.server.common.log.shared.appender.data.BaseSchemaInfo;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
import org.kaaproject.kaa.server.common.thrift.gen.operations.Notification;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogDeliveryMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.LogEventPackMessage;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.MultiLogDeliveryCallback;
import org.kaaproject.kaa.server.operations.service.akka.messages.core.logs.SingleLogDeliveryCallback;
import org.kaaproject.kaa.server.operations.service.cache.AppVersionKey;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorContext;
import akka.actor.ActorRef;

public class ApplicationLogActorMessageProcessor {
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationLogActorMessageProcessor.class);

    private final LogAppenderService logAppenderService;
    private final CacheService cacheService;
    private final CTLService ctlService;

    private final Map<String, LogAppender> logAppenders;

    private final Map<LogAppenderFilterKey, List<LogAppender>> logAppendersCache;

    private final String applicationId;

    private final String applicationToken;

    private final Map<Integer, LogSchema> logSchemas;

    private final Map<AppVersionKey, BaseSchemaInfo> clientProfileSchemas;

    private final Map<AppVersionKey, BaseSchemaInfo> serverProfileSchemas;

    private final VoidCallback voidCallback;

    public ApplicationLogActorMessageProcessor(AkkaContext context, String applicationToken) {
        super();
        this.logAppenderService = context.getLogAppenderService();
        this.cacheService = context.getCacheService();
        this.ctlService = context.getCtlService();
        this.applicationToken = applicationToken;
        this.applicationId = context.getApplicationService().findAppByApplicationToken(applicationToken).getId();
        this.logAppenders = new HashMap<>();
        this.logAppendersCache = new HashMap<>();
        this.logSchemas = new HashMap<>();
        this.clientProfileSchemas = new HashMap<>();
        this.serverProfileSchemas = new HashMap<>();
        this.voidCallback = new VoidCallback();
        for (LogAppender appender : logAppenderService.getApplicationAppenders(applicationId)) {
            logAppenders.put(appender.getAppenderId(), appender);
        }
    }

    protected void processLogEventPack(ActorContext context, LogEventPackMessage message) {
        LOG.debug("[{}] Processing a log event pack with {} appenders", applicationToken, logAppenders.size());
        fetchSchemas(message);
        LogSchema logSchema = message.getLogSchema();
        List<LogAppender> required = filterAppenders(logSchema.getVersion(), true);
        List<LogAppender> optional = filterAppenders(logSchema.getVersion(), false);
        if (required.size() + optional.size() > 0) {
            optional.forEach(appender -> appender.doAppend(message.getLogEventPack(), voidCallback));
            if (required.size() == 0) {
                sendSuccessMessageToEndpoint(message);
            } else {
                LogDeliveryCallback callback;
                if (required.size() == 1) {
                    callback = new SingleLogDeliveryCallback(message.getOriginator(), message.getRequestId());
                } else {
                    callback = new MultiLogDeliveryCallback(message.getOriginator(), message.getRequestId(), required.size());
                }
                required.forEach(appender -> {
                    try {
                        appender.doAppend(message.getLogEventPack(), callback);
                    } catch (Exception cause) {
                        String text = String.format("Failed to append logs using [%s] (ID: %s)", appender.getName(), appender.getAppenderId());
                        LOG.warn(text, cause);
                        sendErrorMessageToEndpoint(message, LogDeliveryErrorCode.APPENDER_INTERNAL_ERROR);
                    }
                });
            }
        } else {
            sendErrorMessageToEndpoint(message, LogDeliveryErrorCode.NO_APPENDERS_CONFIGURED);
        }
    }

    /**
     * Sends a response to the endpoint.
     *
     * Please note that this method was introduced purely as a workaround to
     * mocking an instance of {@link akka.actor.ActorRef}. Change the method
     * body with caution!
     *
     * @param message A message to respond to
     */
    protected void sendSuccessMessageToEndpoint(LogEventPackMessage message) {
        if (message.getOriginator() != null) {
            LogDeliveryMessage response = new LogDeliveryMessage(message.getRequestId(), true);
            message.getOriginator().tell(response, ActorRef.noSender());
        } else {
            LOG.warn("[{}] Unable to respond to an unknown originator", applicationToken);
        }
    }

    public List<LogAppender> filterAppenders(int schemaVersion, boolean confirmDelivery) {
        LogAppenderFilterKey key = new LogAppenderFilterKey(schemaVersion, confirmDelivery);
        List<LogAppender> result = logAppendersCache.get(key);
        if (result == null) {
            result = new ArrayList<LogAppender>();
            for (LogAppender appender : logAppenders.values()) {
                if (appender.isSchemaVersionSupported(schemaVersion) && appender.isDeliveryConfirmationRequired() == confirmDelivery) {
                    result.add(appender);
                }
            }
        }
        return result;
    }

    private void fetchSchemas(LogEventPackMessage message) {
        BaseLogEventPack logPack = message.getLogEventPack();
        LogSchema logSchema = logPack.getLogSchema();
        if (logSchema == null) {
            logSchema = logSchemas.get(message.getLogSchemaVersion());
            if (logSchema == null) {
                logSchema = logAppenderService.getLogSchema(applicationId, logPack.getLogSchemaVersion());
                logSchemas.put(message.getLogSchemaVersion(), logSchema);
            }
            logPack.setLogSchema(logSchema);
        }
        EndpointProfileDataDto profileDto = logPack.getProfileDto();
        ProfileInfo clientProfile = logPack.getClientProfile();
        if (clientProfile == null) {
            AppVersionKey key = new AppVersionKey(applicationToken, profileDto.getClientProfileVersion());
            BaseSchemaInfo schemaInfo = clientProfileSchemas.get(key);
            if (schemaInfo == null) {
                EndpointProfileSchemaDto profileSchema = cacheService.getProfileSchemaByAppAndVersion(key);
                CTLSchemaDto ctlSchemaDto = cacheService.getCtlSchemaById(profileSchema.getCtlSchemaId());
                String schema = ctlService.flatExportAsString(ctlSchemaDto);
                schemaInfo = new BaseSchemaInfo(ctlSchemaDto.getId(), schema);
                clientProfileSchemas.put(key, schemaInfo);
            }
            logPack.setClientProfile(new BaseProfileInfo(schemaInfo, profileDto.getClientProfileBody()));
        }
        ProfileInfo serverProfile = logPack.getServerProfile();
        if (serverProfile == null) {
            AppVersionKey key = new AppVersionKey(applicationToken, profileDto.getServerProfileVersion());
            BaseSchemaInfo schemaInfo = serverProfileSchemas.get(key);
            if (schemaInfo == null) {
                ServerProfileSchemaDto serverProfileSchema = cacheService.getServerProfileSchemaByAppAndVersion(key);
                CTLSchemaDto ctlSchemaDto = cacheService.getCtlSchemaById(serverProfileSchema.getCtlSchemaId());
                String schema = ctlService.flatExportAsString(ctlSchemaDto);
                schemaInfo = new BaseSchemaInfo(ctlSchemaDto.getId(), schema);
                serverProfileSchemas.put(key, schemaInfo);
            }
            logPack.setServerProfile(new BaseProfileInfo(schemaInfo, profileDto.getServerProfileBody()));
        }
    }

    protected void sendErrorMessageToEndpoint(LogEventPackMessage message, LogDeliveryErrorCode errorCode) {
        if (message.getOriginator() != null) {
            message.getOriginator().tell(new LogDeliveryMessage(message.getRequestId(), false, errorCode), ActorRef.noSender());
        } else {
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
            LOG.info("[{}] Closing appender [{}] with name {}", applicationToken, logAppender.getAppenderId(), logAppender.getName());
            logAppender.close();
        }
    }

    private void addLogAppender(String appenderId) {
        LOG.info("[{}] Adding log appender with id [{}].", applicationId, appenderId);
        if (!logAppenders.containsKey(appenderId)) {
            LogAppender logAppender = logAppenderService.getApplicationAppender(appenderId);
            if (logAppender != null) {
                addAppender(appenderId, logAppender);
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

    protected static final class VoidCallback implements LogDeliveryCallback {
        @Override
        public void onSuccess() {
            // Do nothing
        }

        @Override
        public void onRemoteError() {
            // Do nothing
        }

        @Override
        public void onInternalError() {
            // Do nothing
        }

        @Override
        public void onConnectionError() {
            // Do nothing
        }
    }

    private static final class LogAppenderFilterKey {
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LogAppenderFilterKey other = (LogAppenderFilterKey) obj;
            if (confirmDelivery != other.confirmDelivery) {
                return false;
            }
            if (schemaVersion != other.schemaVersion) {
                return false;
            }
            return true;
        }
    }
}
