/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.logs.flume;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.avro.FlumeAppenderParametersDto;
import org.kaaproject.kaa.common.dto.logs.avro.LogAppenderParametersDto;
import org.kaaproject.kaa.server.operations.service.logs.LogAppender;
import org.kaaproject.kaa.server.operations.service.logs.LogEventPack;
import org.kaaproject.kaa.server.operations.service.logs.flume.client.FlumeClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class FlumeLogAppender implements LogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeLogAppender.class);

    private String appenderId;
    private String name;

    @Autowired
    private FlumeEventBuilder flumeEventBuilder;
    private FlumeClientManager flumeClientManger;

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void close() {
        flumeClientManger.cleanUp();
    }

    @Override
    public String getAppenderId() {
        return appenderId;
    }

    public void setAppenderId(String appenderId) {
        this.appenderId = appenderId;
    }

    @Override
    public void doAppend(LogEventPack logEventPack) {
        Event event = flumeEventBuilder.generateEvent(logEventPack);
        try {
            if (flumeClientManger != null) {
                flumeClientManger.sendEventToFlume(event);
            } else {
                LOG.warn("Flume client wasn't initialized. Invoke method init before.");
            }
        } catch (EventDeliveryException e) {
            LOG.warn("Can't send flume event.");
        }
    }

    @Override
    public void init(LogAppenderDto appender) {
        LogAppenderParametersDto parameters = appender.getProperties();
        FlumeAppenderParametersDto flumeParameters = (FlumeAppenderParametersDto) parameters.getParameters();
        flumeClientManger = FlumeClientManager.getInstance(flumeParameters);
    }

}
