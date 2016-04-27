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

package org.kaaproject.kaa.server.operations.service.logs;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.common.log.shared.appender.LogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultLogAppenderBuilder implements LogAppenderBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogAppenderBuilder.class);

    public DefaultLogAppenderBuilder() {
        super();
    }

    @Override
    public LogAppender getAppender(LogAppenderDto appenderConfig) throws ReflectiveOperationException {
        if (appenderConfig == null) {
            throw new IllegalArgumentException("appender config can't be null");
        }
        try {
            @SuppressWarnings("unchecked")
            Class<LogAppender> appenderClass = (Class<LogAppender>) Class
                    .forName(appenderConfig.getPluginClassName());
            LogAppender logAppender = appenderClass.newInstance();
            LOG.debug("Init log appender [{}] with appender configuration [{}].", logAppender, appenderConfig);
            logAppender.setName(appenderConfig.getName());
            logAppender.setAppenderId(appenderConfig.getId());
            logAppender.setApplicationToken(appenderConfig.getApplicationToken());
            logAppender.init(appenderConfig);
            return logAppender;
        } catch (ClassNotFoundException e) {
            LOG.error("Unable to find custom appender class {}", appenderConfig.getPluginClassName());
            throw e;
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Unable to instantiate custom appender from class {}", appenderConfig.getPluginClassName());
            throw e;
        }
    }
}