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

package org.kaaproject.kaa.client.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractExecutorContext implements ExecutorContext {
  protected static final int DEFAULT_TIMEOUT = 5;
  protected static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractExecutorContext.class);
  private final int timeout;
  private final TimeUnit timeunit;

  protected AbstractExecutorContext() {
    this(DEFAULT_TIMEOUT, DEFAULT_TIMEUNIT);
  }

  protected AbstractExecutorContext(int timeout, TimeUnit timeunit) {
    super();
    this.timeout = timeout;
    this.timeunit = timeunit;
  }

  protected int getTimeout() {
    return timeout;
  }

  protected TimeUnit getTimeunit() {
    return timeunit;
  }

  protected void shutdownExecutor(ExecutorService executor) {
    if (executor == null) {
      LOG.warn("Can't shutdown empty executor");
      return;
    }
    LOG.debug("Shutdown executor service");
    executor.shutdown();
    LOG.debug("Waiting for executor service to shutdown for {} {}", getTimeout(), getTimeunit());
    try {
      executor.awaitTermination(getTimeout(), getTimeunit());
    } catch (InterruptedException ex) {
      LOG.warn("Interrupted while waiting for executor to shutdown", ex);
    }
  }
}
