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

import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.BootstrapSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.ProtocolVersionPair;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultBootstrapTransport extends AbstractKaaTransport implements BootstrapTransport {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaClient.class);
  private final String sdkToken;
  private final AtomicInteger increment = new AtomicInteger();
  private BootstrapManager manager;

  public DefaultBootstrapTransport(String sdkToken) {
    this.sdkToken = sdkToken;
  }

  @Override
  public SyncRequest createResolveRequest() {
    if (clientState != null) {
      SyncRequest request = new SyncRequest();
      request.setRequestId(increment.incrementAndGet());
      BootstrapSyncRequest resolveRequest = new BootstrapSyncRequest();
      List<KaaDataChannel> channels = channelManager.getChannels();
      List<ProtocolVersionPair> pairs = new ArrayList<ProtocolVersionPair>(channels.size());
      for (KaaDataChannel channel : channels) {
        TransportProtocolId channelTransportId = channel.getTransportProtocolId();
        pairs.add(new ProtocolVersionPair(channelTransportId.getProtocolId(),
                channelTransportId.getProtocolVersion()));
        LOG.debug("Adding transport with id {} and version {} to resolve request",
                channelTransportId.getProtocolId(), channelTransportId.getProtocolVersion());
      }
      resolveRequest.setSupportedProtocols(pairs);
      resolveRequest.setRequestId(increment.get());
      request.setSyncRequestMetaData(new SyncRequestMetaData(sdkToken, null, null, null));
      request.setBootstrapSyncRequest(resolveRequest);
      return request;
    }
    return null;
  }

  @Override
  public void onResolveResponse(SyncResponse syncResponse) {
    if (manager != null && syncResponse != null
            && syncResponse.getBootstrapSyncResponse() != null) {
      manager.onProtocolListUpdated(
              syncResponse.getBootstrapSyncResponse().getSupportedProtocols());
    }
  }

  @Override
  public void setBootstrapManager(BootstrapManager manager) {
    this.manager = manager;
  }

  @Override
  protected TransportType getTransportType() {
    return TransportType.BOOTSTRAP;
  }

}
