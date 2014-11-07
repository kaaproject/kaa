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
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.kaaproject.kaa.server.operations.service.logs.flume.client.FlumeClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class FlumeLogAppender extends AbstractLogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(FlumeLogAppender.class);

    @Autowired
    private FlumeEventBuilder flumeEventBuilder;
    private FlumeClientManager flumeClientManger;

    @Override
    public void close() {
        flumeClientManger.cleanUp();
        LOG.debug("Stoped flume log appender.");
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        Event event = flumeEventBuilder.generateEvent(logEventPack, header);
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
    public void initLogAppender(LogAppenderDto appender) {
        LogAppenderParametersDto parameters = appender.getProperties();
        FlumeAppenderParametersDto flumeParameters = (FlumeAppenderParametersDto) parameters.getParameters();
        flumeClientManger = FlumeClientManager.getInstance(flumeParameters);
    }

}
