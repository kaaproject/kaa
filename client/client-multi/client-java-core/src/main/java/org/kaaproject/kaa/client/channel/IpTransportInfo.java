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
import org.kaaproject.kaa.common.endpoint.gen.ProtocolVersionPair;
import org.kaaproject.kaa.common.endpoint.security.KeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.PublicKey;

public class IpTransportInfo extends GenericTransportInfo {
  public static final Logger LOG = LoggerFactory // NOSONAR
      .getLogger(IpTransportInfo.class);
  private static final Charset UTF8 = Charset.forName("UTF-8");

  private final String host;
  private final int port;
  private final PublicKey publicKey;

  /**
   * All-args constructor.
   */
  public IpTransportInfo(TransportConnectionInfo parent) {
    super(parent.getServerType(), new ProtocolMetaData(parent.getAccessPointId(),
            new ProtocolVersionPair(parent.getTransportId()
        .getProtocolId(), parent.getTransportId().getProtocolVersion()),
            ByteBuffer.wrap(parent.getConnectionInfo())));
    ByteBuffer buf = md.getConnectionInfo().duplicate();
    byte[] publicKeyData = new byte[buf.getInt()];
    buf.get(publicKeyData);
    try {
      this.publicKey = KeyUtil.getPublic(publicKeyData);
    } catch (InvalidKeyException ex) {
      LOG.error("Can't initialize public key", ex);
      throw new RuntimeException(ex);
    }
    byte[] hostData = new byte[buf.getInt()];
    buf.get(hostData);
    this.host = new String(hostData, UTF8);
    this.port = buf.getInt();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public String getUrl() {
    return "http://" + host + ":" + port;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("IpTransportInfo [host=");
    builder.append(host);
    builder.append(", port=");
    builder.append(port);
    builder.append("]");
    return builder.toString();
  }

  @Override
  public ServerType getServerType() {
    return serverType;
  }
}
