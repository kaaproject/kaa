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

package org.kaaproject.kaa.client.channel.impl;

import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.avro.AvroByteArrayConverter;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class DefaultBootstrapDataProcessor implements KaaDataMultiplexer, KaaDataDemultiplexer {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultBootstrapDataProcessor.class);

  private final AvroByteArrayConverter<SyncRequest> requestConverter =
          new AvroByteArrayConverter<>(SyncRequest.class);
  private final AvroByteArrayConverter<SyncResponse> responseConverter =
          new AvroByteArrayConverter<>(SyncResponse.class);
  private BootstrapTransport transport;

  public void setBootstrapTransport(BootstrapTransport transport) {
    this.transport = transport;
  }

  @Override
  public synchronized byte[] compileRequest(Map<TransportType, ChannelDirection> types)
          throws IOException {
    if (transport != null) {
      SyncRequest request = transport.createResolveRequest();
      LOG.trace("Created Resolve request {}", request);
      return requestConverter.toByteArray(request);
    }
    return null; //NOSONAR
  }

  @Override
  public synchronized void processResponse(byte[] response) throws IOException {
    if (transport != null && response != null) {
      SyncResponse list = responseConverter.fromByteArray(response);
      LOG.trace("Received OperationsServerList response {}", list);
      transport.onResolveResponse(list);
    }
  }

  @Override
  public void preProcess() {
    // Do nothing
  }

  @Override
  public void postProcess() {
    // Do nothing
  }
}
