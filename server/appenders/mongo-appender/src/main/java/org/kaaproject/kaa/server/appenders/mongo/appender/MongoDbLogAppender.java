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

package org.kaaproject.kaa.server.appenders.mongo.appender;

import java.text.MessageFormat;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.mongo.config.gen.MongoDbConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.appender.data.ProfileInfo;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoInternalException;
import com.mongodb.MongoServerException;
import com.mongodb.MongoSocketException;

public class MongoDbLogAppender extends AbstractLogAppender<MongoDbConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDbLogAppender.class);

    private LogEventDao logEventDao;
    private String collectionName;
    private boolean closed = false;

    private boolean includeClientProfile;
    private boolean includeServerProfile;

    public MongoDbLogAppender() {
        super(MongoDbConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            try {
                ProfileInfo clientProfile = (this.includeClientProfile) ? logEventPack.getClientProfile() : null;
                ProfileInfo serverProfile = (this.includeServerProfile) ? logEventPack.getServerProfile() : null;
                
                LOG.debug("[{}] appending {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
                List<LogEventDto> dtos = generateLogEvent(logEventPack, header);
                LOG.debug("[{}] saving {} objects", collectionName, dtos.size());
                if (!dtos.isEmpty()) {
                    logEventDao.save(dtos, clientProfile, serverProfile, collectionName);
                    LOG.debug("[{}] appended {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
                }
                listener.onSuccess();
            } catch (MongoSocketException e) {
                LOG.error(MessageFormat.format("[{0}] Attempted to append logs failed due to network error", getName()), e);
                listener.onConnectionError();
            } catch (MongoInternalException | MongoServerException e) {
                LOG.error(MessageFormat.format("[{0}] Attempted to append logs failed due to remote error", getName()), e);
                listener.onRemoteError();
            } catch (Exception e) {
                LOG.error(MessageFormat.format("[{0}] Attempted to append logs failed due to internal error", getName()), e);
                listener.onInternalError();
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
            listener.onInternalError();
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender, MongoDbConfig configuration) {
        LOG.debug("Initializing new instance of MongoDB log appender");
        try {
            logEventDao = new LogEventMongoDao(configuration);
            this.includeClientProfile = configuration.getIncludeClientProfile();
            this.includeServerProfile = configuration.getIncludeServerProfile();
            createCollection(appender.getApplicationToken());
        } catch (Exception e) {
            LOG.error("Failed to init MongoDB log appender: ", e);
        }
    }

    private void createCollection(String applicationToken) {
        if (collectionName == null) {
            collectionName = "logs_" + applicationToken;
            logEventDao.createCollection(collectionName);
        } else {
            LOG.error("Appender is already initialized..");
        }
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (logEventDao != null) {
                logEventDao.close();
                logEventDao = null;
            }
        }
        LOG.debug("Stoped MongoDB log appender.");
    }

}
