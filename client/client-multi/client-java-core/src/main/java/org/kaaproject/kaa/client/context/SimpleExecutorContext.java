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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Simple implementation of {@link ExecutorContext executorContext} that uses
 * one thread per each executor.
 *
 * @author Andrew Shvayka
 */
public class SimpleExecutorContext extends AbstractExecutorContext implements ExecutorContext {
  private static final Logger LOG = LoggerFactory.getLogger(SingleThreadExecutorContext.class);

  private static final int SINGLE_THREAD = 1;

  private final int lifeCycleThreadCount;
  private final int apiThreadCount;
  private final int callbackThreadCount;
  private final int scheduledThreadCount;

  private ExecutorService lifeCycleExecutor;
  private ExecutorService apiExecutor;
  private ExecutorService callbackExecutor;
  private ScheduledExecutorService scheduledExecutor;

  public SimpleExecutorContext() {
    this(SINGLE_THREAD, SINGLE_THREAD, SINGLE_THREAD, SINGLE_THREAD);
  }

  /**
   * All-args constructor.
   */
  public SimpleExecutorContext(int lifeCycleThreadCount, int apiThreadCount,
                               int callbackThreadCount, int scheduledThreadCount) {
    super();
    this.lifeCycleThreadCount = lifeCycleThreadCount;
    this.apiThreadCount = apiThreadCount;
    this.callbackThreadCount = callbackThreadCount;
    this.scheduledThreadCount = scheduledThreadCount;
  }

  @Override
  public void init() {
    LOG.debug("Creating executor services");
    lifeCycleExecutor = createExecutor(lifeCycleThreadCount);
    apiExecutor = createExecutor(apiThreadCount);
    callbackExecutor = createExecutor(callbackThreadCount);
    scheduledExecutor = createScheduledExecutor(scheduledThreadCount);
    LOG.debug("Created executor services");
  }

  @Override
  public void stop() {
    shutdownExecutor(lifeCycleExecutor);
    shutdownExecutor(apiExecutor);
    shutdownExecutor(callbackExecutor);
    shutdownExecutor(scheduledExecutor);
  }

  @Override
  public ExecutorService getLifeCycleExecutor() {
    return lifeCycleExecutor;
  }

  @Override
  public ExecutorService getApiExecutor() {
    return apiExecutor;
  }

  @Override
  public ExecutorService getCallbackExecutor() {
    return callbackExecutor;
  }

  @Override
  public ScheduledExecutorService getScheduledExecutor() {
    return scheduledExecutor;
  }

  private ExecutorService createExecutor(int threadsNumber) {
    if (threadsNumber == 1) {
      return Executors.newSingleThreadExecutor();
    } else {
      return Executors.newFixedThreadPool(threadsNumber);
    }
  }

  private ScheduledExecutorService createScheduledExecutor(int threadsNumber) {
    if (threadsNumber == 1) {
      return Executors.newSingleThreadScheduledExecutor();
    } else {
      return Executors.newScheduledThreadPool(threadsNumber);
    }
  }
}
