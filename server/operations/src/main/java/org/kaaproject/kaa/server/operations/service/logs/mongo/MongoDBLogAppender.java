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

package org.kaaproject.kaa.server.operations.service.logs.mongo;

import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.LogEventService;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class MongoDBLogAppender extends LogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBLogAppender.class);

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private LogEventService logEventService;

    private String collectionName;
    private boolean closed = false;

    public MongoDBLogAppender() {
    }

    public MongoDBLogAppender(String name) {
        setName(name);
    }

    @Override
    public void close() {
        closed = true;
        LOG.debug("Stoped mongo log appender.");
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            LOG.debug("[{}] appending {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
            List<LogEventDto> dtos = generateLogEvent(logEventPack, header);
            LOG.debug("[{}] saving {} objects", collectionName, dtos.size());
            if (!dtos.isEmpty()) {
                logEventService.save(dtos, collectionName);
                LOG.debug("[{}] appended {} logs to mongodb collection", collectionName, logEventPack.getEvents().size());
            }
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    public void initLogAppender(LogAppenderDto appender) {
        createCollection(appender.getApplicationToken());
    }

    private void createCollection(String applicationToken) {
        if (collectionName == null) {
            collectionName = "logs_" + applicationToken;
            logEventService.createCollection(collectionName);
        } else {
            LOG.error("Appender is already initialized..");
        }
    }

}
