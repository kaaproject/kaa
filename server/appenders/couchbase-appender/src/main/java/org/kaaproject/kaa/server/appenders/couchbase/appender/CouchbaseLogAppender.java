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

package org.kaaproject.kaa.server.appenders.couchbase.appender;

import java.text.MessageFormat;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogAppenderDto;
import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.couchbase.config.gen.CouchbaseConfig;
import org.kaaproject.kaa.server.common.log.shared.appender.AbstractLogAppender;
import org.kaaproject.kaa.server.common.log.shared.appender.LogDeliveryCallback;
import org.kaaproject.kaa.server.common.log.shared.appender.LogEventPack;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchbaseLogAppender extends AbstractLogAppender<CouchbaseConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseLogAppender.class);

    private LogEventDao logEventDao;
    private boolean closed = false;

    public CouchbaseLogAppender() {
        super(CouchbaseConfig.class);
    }

    @Override
    public void doAppend(LogEventPack logEventPack, RecordHeader header, LogDeliveryCallback listener) {
        if (!closed) {
            try {
                LOG.debug("[{}] appending {} logs to couchbase bucket", getApplicationToken(), logEventPack.getEvents().size());
                List<LogEventDto> dtos = generateLogEvent(logEventPack, header);
                LOG.debug("[{}] saving {} objects", getApplicationToken(), dtos.size());
                if (!dtos.isEmpty()) {
                    logEventDao.save(header, dtos);
                    LOG.debug("[{}] appended {} logs to couchbase bucket", getApplicationToken(), logEventPack.getEvents().size());
                }
                listener.onSuccess();
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
    protected void initFromConfiguration(LogAppenderDto appender, CouchbaseConfig configuration) {
        LOG.debug("Initializing new instance of Couchbase log appender");
        try {
            logEventDao = new LogEventCouchbaseDao(configuration);
        } catch (Exception e) {
            LOG.error("Failed to init Couchbase log appender: ", e);
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
        LOG.debug("Stoped Couchbase log appender.");
    }

}
