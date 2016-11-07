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

import java.util.concurrent.TimeUnit;

/**
 * Class that describes a decision which is made by a failover manager,
 * which corresponds to a failover strategy.
 *
 * @see FailoverManager
 */
public class FailoverDecision {
  private final FailoverAction action;
  private long retryPeriod;
  private TimeUnit timeUnit;

  public FailoverDecision(FailoverAction action) {
    this.action = action;
  }

  /**
   * All-args constructor.
   */
  public FailoverDecision(FailoverAction action, long retryPeriod, TimeUnit timeUnit) {
    this.action = action;
    this.retryPeriod = retryPeriod;
    this.timeUnit = timeUnit;
  }

  public FailoverAction getAction() {
    return action;
  }

  /**
   * Is used to get retryPeriod value in milliseconds.
   *
   * @return retry period in milliseconds
   */
  public long getRetryPeriod() {
    return TimeUnit.MILLISECONDS.convert(retryPeriod, timeUnit);
  }

  /**
   * Enum which represents an action corresponding to a failover scenario.
   */
  public static enum FailoverAction {
    NOOP,               // doing nothing
    RETRY,
    USE_NEXT_BOOTSTRAP,
    USE_NEXT_OPERATIONS,
    FAILURE
  }
}
