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

package org.kaaproject.kaa.client.channel;

import org.kaaproject.kaa.common.endpoint.gen.ProtocolMetaData;

/**
 * Generic implementation of {@link TransportConnectionInfo} that is based on
 * {@link ProtocolMetaData}.
 *
 * @author Andrew Shvayka
 */
public class GenericTransportInfo implements TransportConnectionInfo {

  protected final ServerType serverType;
  protected final TransportProtocolId transportId;
  protected final ProtocolMetaData md;

  /**
   * All-args constructor.
   */
  public GenericTransportInfo(ServerType serverType, ProtocolMetaData md) {
    super();
    this.serverType = serverType;
    this.md = md;
    this.transportId = new TransportProtocolId(md.getProtocolVersionInfo().getId(),
            md.getProtocolVersionInfo().getVersion());
  }

  @Override
  public ServerType getServerType() {
    return serverType;
  }

  @Override
  public TransportProtocolId getTransportId() {
    return transportId;
  }

  @Override
  public int getAccessPointId() {
    return md.getAccessPointId();
  }

  @Override
  public byte[] getConnectionInfo() {
    return md.getConnectionInfo().array();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GenericTransportInfo)) {
      return false;
    }

    GenericTransportInfo that = (GenericTransportInfo) obj;

    if (md != null ? !md.equals(that.md) : that.md != null) {
      return false;
    }
    if (serverType != that.serverType) {
      return false;
    }
    if (transportId != null ? !transportId.equals(that.transportId) : that.transportId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = serverType != null ? serverType.hashCode() : 0;
    result = 31 * result + (transportId != null ? transportId.hashCode() : 0);
    result = 31 * result + (md != null ? md.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("GenericTransportInfo [serverType=");
    builder.append(serverType);
    builder.append(", transportId=");
    builder.append(transportId);
    builder.append(", md=");
    builder.append(md);
    builder.append("]");
    return builder.toString();
  }
}
