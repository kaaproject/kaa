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

package org.kaaproject.kaa.client.channel.failover;

import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;

/**
 * Manager is responsible for managing current server's failover/connection events.
 */
public interface FailoverManager {

  /**
   * Needs to be invoked when a server fail occurs.
   *
   * @param connectionInfo the connection information of the failed server.
   * @param status         failover status
   * @see org.kaaproject.kaa.client.channel.TransportConnectionInfo
   */
  void onServerFailed(TransportConnectionInfo connectionInfo, FailoverStatus status);

  /**
   * Needs to be invoked as soon as current server is changed.
   *
   * @param connectionInfo the connection information of the newly connected server.
   * @see org.kaaproject.kaa.client.channel.TransportConnectionInfo
   */
  void onServerChanged(TransportConnectionInfo connectionInfo);

  /**
   * Needs to be invoked as soon as connection to the current server is established.
   *
   * @param connectionInfo the connection information of the current server, to which connection was
   *                       successfully established.
   * @see org.kaaproject.kaa.client.channel.TransportConnectionInfo
   */
  void onServerConnected(TransportConnectionInfo connectionInfo);

  /**
   * Needs to be invoked to determine a decision that resolves the failover.
   *
   * @param failoverStatus current status of the failover.
   * @return decision which is meant to resolve the failover.
   * @see FailoverDecision
   * @see FailoverStatus
   */
  FailoverDecision onFailover(FailoverStatus failoverStatus);

  /**
   * @param failoverStrategy strategy that will be used to resolve failovers.
   * @see org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy
   */
  void setFailoverStrategy(FailoverStrategy failoverStrategy);
}
