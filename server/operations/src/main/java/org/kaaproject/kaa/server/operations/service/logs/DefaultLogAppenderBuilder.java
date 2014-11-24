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
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
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
            try {
                @SuppressWarnings("unchecked")
                Class<LogAppender> appenderClass = (Class<LogAppender>) Class.forName(appenderConfig.getAppenderClassName());
                logAppender = appenderClass.newInstance();
            } catch (ClassNotFoundException e) {
                LOG.error("Unable to find custom appender class " + appenderConfig.getAppenderClassName(), e);
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.error("Unable to instantiate custom appender from class " + appenderConfig.getAppenderClassName(), e);
            } 
        }
        if (logAppender != null) {
            LOG.debug("Init log appender [{}] with appender configuration {[]}.", logAppender, appenderConfig);
            logAppender.setName(appenderConfig.getName());
            logAppender.setAppenderId(appenderConfig.getId());
            logAppender.setApplicationToken(appenderConfig.getApplicationToken());
            logAppender.init(appenderConfig);
        }
        return logAppender;
    }
}