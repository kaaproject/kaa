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

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaTransport;
import org.kaaproject.kaa.client.channel.impl.ChannelRuntimeException;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKaaTransport implements KaaTransport {

  /**
   * The Constant LOG.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaTransport.class);

  protected KaaChannelManager channelManager;

  protected KaaClientState clientState;

  @Override
  public void setChannelManager(KaaChannelManager channelManager) {
    this.channelManager = channelManager;
  }

  @Override
  public void setClientState(KaaClientState state) {
    this.clientState = state;
  }

  protected void syncByType(TransportType type) {
    syncByType(type, false);
  }

  protected void syncByType(TransportType type, boolean ack) {
    syncByType(type, ack, false);
  }

  protected void syncByType(TransportType type, boolean ack, boolean all) {
    if (channelManager == null) {
      LOG.error("Channel manager is not set during sync for type {}", type);
      throw new ChannelRuntimeException("Failed to find channel for transport " + type.toString());
    }
    if (ack) {
      channelManager.syncAck(type);
    } else if (all) {
      channelManager.syncAll(type);
    } else {
      channelManager.sync(type);
    }
  }

  protected void syncAll(TransportType type) {
    syncByType(type, false, true);
  }

  protected void syncAckByType(TransportType type) {
    syncByType(type, true);
  }

  @Override
  public void sync() {
    syncByType(getTransportType());
  }

  protected void syncAck() {
    syncAckByType(getTransportType());
  }

  protected void syncAck(SyncResponseStatus status) {
    if (status != SyncResponseStatus.NO_DELTA) {
      LOG.info("Sending ack due to response status: {}", status);
      syncAck();
    }
  }

  protected abstract TransportType getTransportType();
}
