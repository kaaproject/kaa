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

package org.kaaproject.kaa.server.appenders.flume.appender;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.server.appenders.flume.appender.client.FlumeClientManager;
import org.kaaproject.kaa.server.appenders.flume.config.gen.FlumeConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlumeLogAppender extends AbstractLogAppender<FlumeConfig> {
    
    private static final Logger LOG = LoggerFactory.getLogger(FlumeLogAppender.class);

    private boolean closed = false;
    
    private FlumeEventBuilder flumeEventBuilder;
    private FlumeClientManager<?> flumeClientManger;
    
    public FlumeLogAppender() {
        super(FlumeConfig.class);
    }
    
    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            Event event = flumeEventBuilder.generateEvent(logEventPack, header, getApplicationToken());
            try {
                if (flumeClientManger != null) {
                    flumeClientManger.sendEventToFlume(event);
                } else {
                    LOG.warn("Flume client wasn't initialized. Invoke method init before.");
                }
            } catch (EventDeliveryException e) {
                LOG.warn("Can't send flume event.");
            }            
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    protected void initFromConfiguration(LogAppenderDto appender,
            FlumeConfig configuration) {
        LOG.debug("Initializing new instance of Flume log appender");
        try {
            flumeEventBuilder = new FlumeAvroEventBuilder();
            flumeClientManger = FlumeClientManager.getInstance(configuration);
        } catch (Exception e) {
            LOG.error("Failed to init Flume log appender: ", e);
        }
    }
    
    @Override
    public void close() {
        if (!closed) {
            closed = true;
            if (flumeClientManger != null) {
                flumeClientManger.cleanUp();
            }
            flumeClientManger = null;
        }
        LOG.debug("Stoped Flume log appender.");
    }

}
