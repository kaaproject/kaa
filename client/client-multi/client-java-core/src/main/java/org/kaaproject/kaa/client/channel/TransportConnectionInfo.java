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

/**
 * Interface for server information. Used by {@link KaaDataChannel} and
 * {@link KaaChannelManager}
 */
public interface TransportConnectionInfo {

  /**
   * Retrieves the channel's server type (i.e. OPERATIONS or BOOTSTRAP).
   *
   * @return the channel's server type.
   * @see ServerType
   */
  ServerType getServerType();

  /**
   * Retrieves the {@link TransportProtocolId}.
   *
   * @return the transport protocol id.
   * @see TransportProtocolId
   */
  TransportProtocolId getTransportId();

  /**
   * Retrieves the access point id (operations/bootstrap service id).
   *
   * @return access point id
   */
  int getAccessPointId();

  /**
   * Retrieves serialized connection properties. Serialization may be specific for each transport
   * protocol implementation.
   *
   * @return serialized connection properties
   */
  byte[] getConnectionInfo();
}
