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

/**
 * @author Yaroslav Zeygerman.
 * @see KaaDataChannel
 */
public interface KaaInternalChannelManager extends KaaChannelManager {

  /**
   * Reports to Channel Manager about the new server.
   *
   * @param newServer the parameters of the new server.
   * @see TransportConnectionInfo
   */
  void onTransportConnectionInfoUpdated(TransportConnectionInfo newServer);

  /**
   * Sets connectivity checker to the existing channels.
   *
   * @param checker platform-dependent connectivity checker.
   * @see ConnectivityChecker
   */
  void setConnectivityChecker(ConnectivityChecker checker);

  /**
   * Shuts down the manager and all registered channels. The instance can no
   * longer be used.
   */
  void shutdown();

  /**
   * Pauses all active channels.
   */
  void pause();

  /**
   * Restores channels' activity.
   */
  void resume();

  void setOperationMultiplexer(KaaDataMultiplexer multiplexer);

  void setOperationDemultiplexer(KaaDataDemultiplexer demultiplexer);

  void setBootstrapMultiplexer(KaaDataMultiplexer multiplexer);

  void setBootstrapDemultiplexer(KaaDataDemultiplexer demultiplexer);
}
