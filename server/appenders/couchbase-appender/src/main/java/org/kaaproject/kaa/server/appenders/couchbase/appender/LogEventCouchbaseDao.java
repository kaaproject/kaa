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

import org.kaaproject.kaa.common.dto.logs.LogEventDto;
import org.kaaproject.kaa.server.appenders.couchbase.config.gen.CouchbaseConfig;
import org.kaaproject.kaa.server.common.log.shared.avro.gen.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.WriteResultChecking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogEventCouchbaseDao implements LogEventDao {

  private static final Logger LOG = LoggerFactory.getLogger(LogEventCouchbaseDao.class);

  private final Random random = new Random();

  private KaaCouchbaseCluster couchbaseConfiguration;
  private CouchbaseTemplate couchbaseTemplate;

  /**
   * Instantiates a new Log event couchbase dao.
   *
   * @param configuration the configuration
   * @throws Exception the exception
   */
  public LogEventCouchbaseDao(CouchbaseConfig configuration) throws Exception {
    couchbaseConfiguration = new KaaCouchbaseCluster(
        configuration.getCouchbaseServerUris().stream()
            .map(v -> v.getServerUri())
            .collect(Collectors.toList()),
        configuration.getBucket(),
        configuration.getPassword());
    couchbaseTemplate = couchbaseConfiguration.connect();
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
    if (couchbaseConfiguration != null) {
      try {
        couchbaseConfiguration.disconnect();
      } catch (Exception ex) {
        LOG.error("Failed to disconnect from couchbase cluster!", ex);
      }
    }
  }

  private String getId(String id) {
    if (id == null || id.length() == 0) {
      id = new UUID(System.currentTimeMillis(), random.nextLong()).toString();
    }
    return id;
  }

}
