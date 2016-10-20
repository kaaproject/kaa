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

package org.kaaproject.kaa.client.channel.impl.channels;

import org.kaaproject.kaa.client.AbstractKaaClient;
import org.kaaproject.kaa.client.channel.ChannelDirection;
import org.kaaproject.kaa.client.channel.ServerType;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultOperationHttpChannel extends AbstractHttpChannel {
  public static final Logger LOG = LoggerFactory //NOSONAR
      .getLogger(DefaultOperationsChannel.class);

  private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<>();
  private static final String CHANNEL_ID = "default_operations_http_channel";

  static {
    SUPPORTED_TYPES.put(TransportType.EVENT, ChannelDirection.UP);
    SUPPORTED_TYPES.put(TransportType.LOGGING, ChannelDirection.UP);
  }

  public DefaultOperationHttpChannel(AbstractKaaClient client, KaaClientState state,
                                     FailoverManager failoverManager) {
    super(client, state, failoverManager);
  }

  private void processTypes(Map<TransportType, ChannelDirection> types) throws Exception {
    byte[] requestBodyRaw = getMultiplexer().compileRequest(types);
    byte[] decodedResponse = null;
    synchronized (this) {
      LinkedHashMap<String, byte[]> requestEntity = HttpRequestCreator.createOperationHttpRequest(
              requestBodyRaw, getHttpClient().getEncoderDecoder());
      byte[] responseDataRaw = getHttpClient().executeHttpRequest("", requestEntity, false);
      decodedResponse = getHttpClient().getEncoderDecoder().decodeData(responseDataRaw);
    }
    getDemultiplexer().processResponse(decodedResponse);
  }

  @Override
  public String getId() {
    return CHANNEL_ID;
  }

  @Override
  public ServerType getServerType() {
    return ServerType.OPERATIONS;
  }

  @Override
  public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  protected Runnable createChannelRunnable(
      Map<TransportType, ChannelDirection> typeMap) {
    return new OperationRunnable(typeMap);
  }

  @Override
  protected String getUrlSufix() {
    return "/EP/Sync";
  }

  private class OperationRunnable implements Runnable {

    private final Map<TransportType, ChannelDirection> typesToProcess;

    OperationRunnable(Map<TransportType, ChannelDirection> types) {
      this.typesToProcess = types;
    }

    @Override
    public void run() {
      try {
        processTypes(typesToProcess);
        connectionFailed(false);
      } catch (TransportException ex) {
        LOG.error("Failed to receive response from the operation {}", ex);
        connectionFailed(true, ex.getStatus());
      } catch (Exception ex) {
        LOG.error("Failed to receive response from the operation {}", ex);
        connectionFailed(true);
      }
    }
  }
}
