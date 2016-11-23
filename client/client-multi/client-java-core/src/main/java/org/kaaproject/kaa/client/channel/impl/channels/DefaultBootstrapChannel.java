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
import org.kaaproject.kaa.common.TransportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultBootstrapChannel extends AbstractHttpChannel {

  public static final Logger LOG = LoggerFactory // NOSONAR
      .getLogger(DefaultBootstrapChannel.class);

  private static final Map<TransportType, ChannelDirection> SUPPORTED_TYPES = new HashMap<>();
  private static final String CHANNEL_ID = "default_bootstrap_channel";

  static {
    SUPPORTED_TYPES.put(TransportType.BOOTSTRAP, ChannelDirection.BIDIRECTIONAL);
  }

  public DefaultBootstrapChannel(AbstractKaaClient client, KaaClientState state,
                                 FailoverManager failoverManager) {
    super(client, state, failoverManager);
  }

  private void processTypes(Map<TransportType, ChannelDirection> types) throws Exception {
    LOG.trace("Processing types for [{}]", getId());
    byte[] requestBodyRaw = getMultiplexer().compileRequest(types);
    byte[] decodedResponse = null;
    synchronized (this) {
      LinkedHashMap<String, byte[]> requestEntity = HttpRequestCreator.createBootstrapHttpRequest(
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
    return ServerType.BOOTSTRAP;
  }

  @Override
  public Map<TransportType, ChannelDirection> getSupportedTransportTypes() {
    return SUPPORTED_TYPES;
  }

  @Override
  protected Runnable createChannelRunnable(Map<TransportType, ChannelDirection> typeMap) {
    return new BootstrapRunnable();
  }

  @Override
  protected String getUrlSufix() {
    return "/BS/Sync";
  }

  private class BootstrapRunnable implements Runnable {

    @Override
    public void run() {
      try {
        processTypes(SUPPORTED_TYPES);
        connectionFailed(false);
      } catch (Exception ex) {
        if (!isShutdown()) {
          LOG.error("Failed to receive operation servers list {}", ex);
          connectionFailed(true);
        } else {
          LOG.debug("Failed to receive operation servers list {}", ex);
        }
      }
    }

  }
}
