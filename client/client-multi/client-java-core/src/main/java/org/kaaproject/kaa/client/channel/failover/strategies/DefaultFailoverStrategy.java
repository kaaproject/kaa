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
import org.kaaproject.kaa.client.channel.failover.FailoverDecision.FailoverAction;
import org.kaaproject.kaa.client.channel.failover.FailoverStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Reference implementation for {@link FailoverStrategy}.
 */
public class DefaultFailoverStrategy implements FailoverStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultFailoverStrategy.class);

  private static final long DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD = 2;
  private static final long DEFAULT_OPERATION_SERVERS_RETRY_PERIOD = 2;
  private static final long DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD = 5;
  private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

  private long bootstrapServersRetryPeriod;
  private long operationsServersRetryPeriod;
  private long noConnectivityRetryPeriod;
  private TimeUnit timeUnit;

  /**
   * Instantiates the DelautFailoverStrategy.
   */
  public DefaultFailoverStrategy() {
    this(DEFAULT_BOOTSTRAP_SERVERS_RETRY_PERIOD,
        DEFAULT_OPERATION_SERVERS_RETRY_PERIOD,
        DEFAULT_NO_CONNECTIVITY_RETRY_PERIOD,
        DEFAULT_TIME_UNIT);
  }

  /**
   * All-args constructor.
   */
  public DefaultFailoverStrategy(long bootstrapServersRetryPeriod,
                                 long operationsServersRetryPeriod,
                                 long noConnectivityRetryPeriod,
                                 TimeUnit timeUnit) {
    this.bootstrapServersRetryPeriod = bootstrapServersRetryPeriod;
    this.operationsServersRetryPeriod = operationsServersRetryPeriod;
    this.noConnectivityRetryPeriod = noConnectivityRetryPeriod;
    this.timeUnit = timeUnit;
  }

  @Override
  public FailoverDecision onFailover(FailoverStatus failoverStatus) {
    LOG.trace("Producing failover decision for failover status: {}", failoverStatus);
    switch (failoverStatus) {
      case BOOTSTRAP_SERVERS_NA:
        return new FailoverDecision(FailoverAction.RETRY, bootstrapServersRetryPeriod, timeUnit);
      case CURRENT_BOOTSTRAP_SERVER_NA:
        return new FailoverDecision(FailoverAction.USE_NEXT_BOOTSTRAP, bootstrapServersRetryPeriod,
                timeUnit);
      case NO_OPERATION_SERVERS_RECEIVED:
        return new FailoverDecision(FailoverAction.USE_NEXT_BOOTSTRAP, bootstrapServersRetryPeriod,
                timeUnit);
      case OPERATION_SERVERS_NA:
        return new FailoverDecision(FailoverAction.RETRY, operationsServersRetryPeriod, timeUnit);
      case NO_CONNECTIVITY:
        return new FailoverDecision(FailoverAction.RETRY, noConnectivityRetryPeriod, timeUnit);
      case ENDPOINT_VERIFICATION_FAILED:
      case ENDPOINT_CREDENTIALS_REVOKED:
        return new FailoverDecision(FailoverAction.RETRY);
      default:
        return new FailoverDecision(FailoverAction.NOOP);
    }
  }

  @Override
  public void onRecover(TransportConnectionInfo connectionInfo) {
    LOG.debug("SDK recovered after failover with connection info: {}", connectionInfo);
  }

  @Override
  public long getBootstrapServersRetryPeriod() {
    return bootstrapServersRetryPeriod;
  }

  @Override
  public long getOperationServersRetryPeriod() {
    return operationsServersRetryPeriod;
  }

  @Override
  public TimeUnit getTimeUnit() {
    return timeUnit;
  }
}
