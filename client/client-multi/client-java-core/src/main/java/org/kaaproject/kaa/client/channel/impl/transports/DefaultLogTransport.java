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

package org.kaaproject.kaa.client.channel.impl.transports;

import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.logging.LogProcessor;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DefaultLogTransport extends AbstractKaaTransport implements
    LogTransport {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultLogTransport.class);
  private LogProcessor processor;

  @Override
  public void setLogProcessor(LogProcessor processor) {
    this.processor = processor;
  }

  @Override
  public LogSyncRequest createLogRequest() {
    if (processor != null) {
      LogSyncRequest request = new LogSyncRequest();
      processor.fillSyncRequest(request);
      return request;
    } else {
      LOG.error("Can't create request. LogProcessor is null");
    }
    return null;
  }

  @Override
  public void onLogResponse(LogSyncResponse response) {
    if (processor != null) {
      try {
        processor.onLogResponse(response);
      } catch (IOException ex) {
        LOG.error("Failed to process Log response: {}", ex);
      }
    } else {
      LOG.error("Can't process response. LogProcessor is null");
    }
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.LOGGING;
  }

}
