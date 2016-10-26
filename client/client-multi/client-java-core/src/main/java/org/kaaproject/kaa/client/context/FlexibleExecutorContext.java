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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlexibleExecutorContext extends AbstractExecutorContext implements ExecutorContext {
  private static final Logger LOG = LoggerFactory.getLogger(FlexibleExecutorContext.class);

  public static final int DEFAULT_MIN_THREADS = 0;
  public static final int DEFAULT_MAX_THREADS = Integer.MAX_VALUE;
  public static final int DEFAULT_MAX_THREAD_IDLE_MILLISECONDS = 100;

  private final int maxLifeCycleThreads;
  private final int maxApiThreads;
  private final int maxCallbackThreads;
  private final int minScheduledThreads;

  private final int maxLifeCycleThreadsIdleMilliseconds;
  private final int maxApiThreadsIdleMilliseconds;
  private final int maxCallbackThreadsIdleMilliseconds;

  private ExecutorService lifeCycleExecutor;
  private ExecutorService apiExecutor;
  private ExecutorService callbackExecutor;
  private ScheduledExecutorService scheduledExecutor;

  public FlexibleExecutorContext() {
    this(DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MIN_THREADS);
  }

  /**
   * All-args constructor.
   */
  public FlexibleExecutorContext(int maxLifeCycleThreads, int maxApiThreads,
                                 int maxCallbackThreads, int minScheduledThreads) {
    this.maxLifeCycleThreads = maxLifeCycleThreads;
    this.maxApiThreads = maxApiThreads;
    this.maxCallbackThreads = maxCallbackThreads;
    this.minScheduledThreads = minScheduledThreads;

    maxLifeCycleThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
    maxCallbackThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
    maxApiThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
  }

  /**
   * Constructor for Builder pattern.
   */
  private FlexibleExecutorContext(FlexibleExecutorContextBuilder builder) {
    maxLifeCycleThreads = builder.getMaxLifeCycleThreads();
    maxApiThreads = builder.getMaxApiThreads();
    maxCallbackThreads = builder.getMaxCallbackThreads();
    minScheduledThreads = builder.getMinScheduledThreads();
    maxLifeCycleThreadsIdleMilliseconds = builder.getMaxLifeCycleThreadsIdleMilliseconds();
    maxCallbackThreadsIdleMilliseconds = builder.getMaxCallbackThreadsIdleMilliseconds();
    maxApiThreadsIdleMilliseconds = builder.getMaxApiThreadsIdleMilliseconds();
  }

  @Override
  public void init() {
    LOG.debug("Creating executor services");
    lifeCycleExecutor = createExecutor(maxLifeCycleThreads, maxLifeCycleThreadsIdleMilliseconds);
    apiExecutor = createExecutor(maxApiThreads, maxApiThreadsIdleMilliseconds);
    callbackExecutor = createExecutor(maxCallbackThreads, maxCallbackThreadsIdleMilliseconds);
    scheduledExecutor = createScheduledExecutor(minScheduledThreads);
    LOG.debug("Creation of executor services is finished");
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

  private ExecutorService createExecutor(int maxThreads, int maxThreadsIdleMilliseconds) {
    return new ThreadPoolExecutor(DEFAULT_MIN_THREADS, maxThreads,
        maxThreadsIdleMilliseconds, TimeUnit.MILLISECONDS,
        new SynchronousQueue<Runnable>());
  }

  private ScheduledExecutorService createScheduledExecutor(int minThreads) {
    return Executors.newScheduledThreadPool(minThreads);
  }

  public static class FlexibleExecutorContextBuilder {

    private int maxLifeCycleThreads = DEFAULT_MAX_THREADS;
    private int maxApiThreads = DEFAULT_MAX_THREADS;
    private int maxCallbackThreads = DEFAULT_MAX_THREADS;
    private int minScheduledThreads = DEFAULT_MIN_THREADS;

    private int maxLifeCycleThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
    private int maxApiThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
    private int maxCallbackThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;

    public FlexibleExecutorContextBuilder setMaxLifeCycleThreads(int maxLifeCycleThreads) {
      this.maxLifeCycleThreads = maxLifeCycleThreads;
      return this;
    }

    public FlexibleExecutorContextBuilder setMaxApiThreads(int maxApiThreads) {
      this.maxApiThreads = maxApiThreads;
      return this;
    }

    public FlexibleExecutorContextBuilder setMaxCallbackThreads(int maxCallbackThreads) {
      this.maxCallbackThreads = maxCallbackThreads;
      return this;
    }

    public FlexibleExecutorContextBuilder setMinScheduledThreads(int minScheduledThreads) {
      this.minScheduledThreads = minScheduledThreads;
      return this;
    }


    public FlexibleExecutorContextBuilder setMaxLifeCycleThreadsIdleMilliseconds(int maxLifeCycleThreadsIdleMilliseconds) {
      this.maxLifeCycleThreadsIdleMilliseconds = maxLifeCycleThreadsIdleMilliseconds;
      return this;
    }

    public FlexibleExecutorContextBuilder setMaxApiThreadsIdleMilliseconds(int maxApiThreadsIdleMilliseconds) {
      this.maxApiThreadsIdleMilliseconds = maxApiThreadsIdleMilliseconds;
      return this;
    }

    public FlexibleExecutorContextBuilder setMaxCallbackThreadsIdleMilliseconds(int maxCallbackThreadsIdleMilliseconds) {
      this.maxCallbackThreadsIdleMilliseconds = maxCallbackThreadsIdleMilliseconds;
      return this;
    }


    /**
     * Set threads amount and appropriate idle time for life cycle threads.
     *
     * @param lifeCycleThreadCount                threads amount
     * @param maxLifeCycleThreadsIdleMilliseconds idle time (milliseconds)
     * @return builder
     */
    public FlexibleExecutorContextBuilder setLifeCycleThreadCountAndIdleMilliseconds(
        int lifeCycleThreadCount, int maxLifeCycleThreadsIdleMilliseconds) {
      this.maxLifeCycleThreads = lifeCycleThreadCount;
      this.maxLifeCycleThreadsIdleMilliseconds = maxLifeCycleThreadsIdleMilliseconds;
      return this;
    }

    /**
     * Set threads amount and appropriate idle time for api`s threads.
     *
     * @param apiThreadCount                threads amount
     * @param maxApiThreadsIdleMilliseconds idle time (milliseconds)
     * @return builder
     */
    public FlexibleExecutorContextBuilder setApiThreadCountAndIdleMilliseconds(
        int apiThreadCount, int maxApiThreadsIdleMilliseconds) {
      this.maxApiThreads = apiThreadCount;
      this.maxApiThreadsIdleMilliseconds = maxApiThreadsIdleMilliseconds;
      return this;
    }

    /**
     * Set threads amount and appropriate idle time for callback`s threads.
     *
     * @param callbackThreadCount                threads amount
     * @param maxCallbackThreadsIdleMilliseconds idle time (milliseconds)
     * @return builder
     */
    public FlexibleExecutorContextBuilder setCallbackThreadCountAndIdleMilliseconds(
        int callbackThreadCount, int maxCallbackThreadsIdleMilliseconds) {
      this.maxCallbackThreads = callbackThreadCount;
      this.maxCallbackThreadsIdleMilliseconds = maxCallbackThreadsIdleMilliseconds;
      return this;
    }


    public FlexibleExecutorContext build() {
      return new FlexibleExecutorContext(this);
    }

    int getMaxLifeCycleThreads() {
      return maxLifeCycleThreads;
    }

    int getMaxApiThreads() {
      return maxApiThreads;
    }

    int getMaxCallbackThreads() {
      return maxCallbackThreads;
    }

    int getMinScheduledThreads() {
      return minScheduledThreads;
    }

    int getMaxLifeCycleThreadsIdleMilliseconds() {
      return maxLifeCycleThreadsIdleMilliseconds;
    }

    int getMaxApiThreadsIdleMilliseconds() {
      return maxApiThreadsIdleMilliseconds;
    }

    int getMaxCallbackThreadsIdleMilliseconds() {
      return maxCallbackThreadsIdleMilliseconds;
    }
  }
}