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

import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.common.TransportType;

import java.util.Map;
import java.util.Set;

/**
 * Channel is responsible for sending/receiving data to/from the endpoint
 * server.
 *
 * @author Yaroslav Zeygerman
 */
public interface KaaDataChannel {

  /**
   * Updates the channel's state of the specific service.
   *
   * @param type transport type of the service.
   * @see TransportType
   */
  void sync(TransportType type);

  /**
   * Updates the channel's state of the specific service.
   *
   * @param types transport types to sync.
   * @see TransportType
   */
  void sync(Set<TransportType> types);

  /**
   * Updates the channel's state of all supported services.
   */
  void syncAll();

  /**
   * Notifies channel about successful acknowledgment of the sync only in case
   * of smth change.
   *
   * @param type transport type of the service.
   * @see TransportType
   */
  void syncAck(TransportType type);

  /**
   * Notifies channel about successful acknowledgment of the sync only in case
   * of smth change.
   *
   * @param type transport type to ack.
   * @see TransportType
   */
  void syncAck(Set<TransportType> type);

  /**
   * Retrieves the channel's id. It should be unique in existing channels
   * scope.
   *
   * @return the channel's id.
   */
  String getId();

  /**
   * Retrieves the {@link TransportProtocolId}.
   *
   * @return the transport protocol id.
   * @see TransportProtocolId
   */
  TransportProtocolId getTransportProtocolId();

  /**
   * Retrieves the channel's server type (i.e. OPERATIONS or BOOTSTRAP).
   *
   * @return the channel's server type.
   * @see ServerType
   */
  ServerType getServerType();

  /**
   * Sets the response demultiplexer for this channel.
   *
   * @param demultiplexer demultiplexer instance to be set.
   * @see KaaDataDemultiplexer
   */
  void setDemultiplexer(KaaDataDemultiplexer demultiplexer);

  /**
   * Sets the request multiplexer for this channel.
   *
   * @param multiplexer multiplexer instance to be set.
   * @see KaaDataMultiplexer
   */
  void setMultiplexer(KaaDataMultiplexer multiplexer);

  /**
   * Retrieves info about current used server.
   *
   * @return Server info
   */
  TransportConnectionInfo getServer();

  /**
   * Sets the server's parameters for the current channel.
   *
   * @param server server's parameters.
   * @see TransportConnectionInfo
   */
  void setServer(TransportConnectionInfo server);

  /**
   * Sets connectivity checker to the current channel.
   *
   * @param checker platform-dependent connectivity checker.
   * @see ConnectivityChecker
   */
  void setConnectivityChecker(ConnectivityChecker checker);

  /**
   * Retrieves the map of transport types and their directions supported by
   * this channel.
   *
   * @return the map of transport types.
   * @see TransportType
   * @see ChannelDirection
   */
  Map<TransportType, ChannelDirection> getSupportedTransportTypes();

  /**
   * Shuts down the channel instance. All connections and threads should be
   * terminated. The instance can no longer be used.
   */
  void shutdown();

  /**
   * Pauses the channel's workflow. The channel should stop all network
   * activity.
   */
  void pause();

  /**
   * Resumes the channel's workflow. The channel should restore previous
   * connection.
   */
  void resume();
}
