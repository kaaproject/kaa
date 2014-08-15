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
package org.kaaproject.kaa.server.operations.service.logs;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.operations.service.logs.filesystem.FileSystemLogAppender;
import org.kaaproject.kaa.server.operations.service.logs.flume.FlumeLogAppender;
import org.kaaproject.kaa.server.operations.service.logs.mongo.MongoDBLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DefaultLogAppenderBuilder implements LogAppenderBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogAppenderBuilder.class);

    @Autowired
    private ApplicationContext applicationContext;

    public DefaultLogAppenderBuilder() {
    }

    @Override
    public LogAppender getAppender(LogAppenderDto appenderConfig) {
        LogAppender logAppender = null;
        if (appenderConfig != null) {
            switch (appenderConfig.getType()) {
                case FILE:
                    logAppender = applicationContext.getBean(FileSystemLogAppender.class);
                    break;
                case FLUME:
                    logAppender = applicationContext.getBean(FlumeLogAppender.class);
                    break;
                case MONGO:
                    logAppender = applicationContext.getBean(MongoDBLogAppender.class);
                    break;
                default:
                    LOG.debug("Incorrect type of log appender [{}].", appenderConfig.getType());
                    break;
            }
            if (logAppender != null) {
                LOG.debug("Init log appender [{}] with appender configuration {[]}.", logAppender, appenderConfig);
                logAppender.setName(appenderConfig.getName());
                logAppender.setAppenderId(appenderConfig.getId());
                logAppender.init(appenderConfig);
            }
        }
        return logAppender;
    }
}