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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.couchbase.config.gen.CouchbaseConfig;
import org.kaaproject.kaa.server.appenders.couchbase.config.gen.CouchbaseServerUri;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.WriteResultChecking;

import com.couchbase.client.CouchbaseClient;

public class LogEventCouchbaseDao implements LogEventDao {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventCouchbaseDao.class);
    
    private final Random RANDOM = new Random();

    private CouchbaseClient couchbaseClient;
    private CouchbaseTemplate couchbaseTemplate;

    public LogEventCouchbaseDao(CouchbaseConfig configuration) throws Exception {

        List<CouchbaseServerUri> couchbaseUris = configuration.getCouchbaseServerUris();
        List<URI> baseList = new ArrayList<URI>(couchbaseUris.size());
        for (CouchbaseServerUri couchbaseServerUri : couchbaseUris) {
            baseList.add(new URI(couchbaseServerUri.getServerUri()));
        }
        String pass = configuration.getPassword();
        if (pass == null) {
            pass = "";
        }
        couchbaseClient = new CouchbaseClient(baseList, configuration.getBucket(), pass);
        
        couchbaseTemplate = new CouchbaseTemplate(couchbaseClient);
        couchbaseTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
    }

    @Override
    public List<LogEvent> save(RecordHeader recordHeader, List<LogEventDto> logEventDtos) {
        List<LogEvent> logEvents = new ArrayList<>(logEventDtos.size());
        for (LogEventDto logEventDto : logEventDtos) {
            LogEvent logEvent = new LogEvent(recordHeader, logEventDto);
            logEvent.setId(getId(logEventDto.getId()));
            logEvents.add(logEvent);
        }
        LOG.debug("Saving {} log events", logEvents.size());
        couchbaseTemplate.insert(logEvents);
        return logEvents;
    }

    @Override
    public void close() {
        if (couchbaseClient != null) {
            couchbaseClient.shutdown();
        }
    }
    
    private String getId(String id) {
        if (id == null || id.length() == 0) {
            id = new UUID(System.currentTimeMillis(), RANDOM.nextLong()).toString();
        }
        return id;
    }

}
