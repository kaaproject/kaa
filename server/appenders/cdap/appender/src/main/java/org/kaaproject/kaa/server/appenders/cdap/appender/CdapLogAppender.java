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

package org.kaaproject.kaa.server.appenders.cdap.appender;

import java.util.Properties;

import org.kaaproject.kaa.server.common.log.shared.appender.CustomLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdapLogAppender extends CustomLogAppender {

    private static final Logger LOG = LoggerFactory.getLogger(CdapLogAppender.class);

    private boolean closed = false;

    public CdapLogAppender() {
    }

    @Override
    public void close() {
        closed = true;
        LOG.debug("Stopped Cdap log appender.");
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header) {
        if (!closed) {
            LOG.debug("[{}] appending {} logs using cdap appender", logEventPack.getEvents().size());
            //List<LogEventDto> dtos = generateLogEvent(logEventPack, header);
        } else {
            LOG.info("Attempted to append to closed appender named [{}].", getName());
        }
    }

    @Override
    protected void initFromProperties(Properties properties) {
        // TODO Auto-generated method stub
    }
 
}
