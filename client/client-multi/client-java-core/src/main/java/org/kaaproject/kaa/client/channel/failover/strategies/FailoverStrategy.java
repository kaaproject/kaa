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

package org.kaaproject.kaa.client.channel.failover.strategies;

import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.failover.FailoverDecision;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;

import java.util.concurrent.TimeUnit;

/**
 * Failover strategy is responsible for producing failover decisions based on failover statuses.
 */
public interface FailoverStrategy {

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
   * Needs to be invoked once client recovered after failover.
   *
   * @param connectionInfo server information
   * @see org.kaaproject.kaa.client.channel.TransportConnectionInfo
   */
  void onRecover(TransportConnectionInfo connectionInfo);

  /**
   * Use the {@link #getTimeUnit()} method to get current time unit.
   *
   * @return period of time after which will be made attempt to tweak bootstrap service.
   */
  long getBootstrapServersRetryPeriod();

  /**
   * Use the {@link #getTimeUnit()} method to get current time unit.
   *
   * @return period of time after which will be made attempt to tweak operation server.
   */
  long getOperationServersRetryPeriod();

  /**
   * @return time unit used within a scope of current failover strategy.
   */
  TimeUnit getTimeUnit();
}
