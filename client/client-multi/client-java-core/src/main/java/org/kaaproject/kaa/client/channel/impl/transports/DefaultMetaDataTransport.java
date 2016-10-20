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

import org.kaaproject.kaa.client.KaaClientProperties;
import org.kaaproject.kaa.client.channel.MetaDataTransport;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.common.endpoint.gen.SyncRequestMetaData;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;

import java.nio.ByteBuffer;

public class DefaultMetaDataTransport implements MetaDataTransport {

  private KaaClientProperties properties;
  private KaaClientState state;
  private EndpointObjectHash publicKeyHash;
  private long timeout;

  @Override
  public SyncRequestMetaData createMetaDataRequest() {
    if (state != null && properties != null && publicKeyHash != null) {
      SyncRequestMetaData request = new SyncRequestMetaData();
      request.setSdkToken(properties.getSdkToken());
      request.setEndpointPublicKeyHash(ByteBuffer.wrap(publicKeyHash.getData()));
      request.setProfileHash(ByteBuffer.wrap(state.getProfileHash().getData()));
      request.setTimeout(timeout);
      return request;
    }
    return null;
  }

  @Override
  public void setClientProperties(KaaClientProperties properties) {
    this.properties = properties;
  }

  @Override
  public void setClientState(KaaClientState state) {
    this.state = state;
  }

  @Override
  public void setEndpointPublicKeyhash(EndpointObjectHash hash) {
    this.publicKeyHash = hash;
  }

  @Override
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

}
