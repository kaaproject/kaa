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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlexibleExecutorContextTest {

  private FlexibleExecutorContext executorContext;

  private static final int DEFAULT_CORE_POOL_SIZE = 1;
  private static final int DEFAULT_MAX_POOL_SIZE = FlexibleExecutorContext.getDefaultMaxThreads();
  private static final int DEFAULT_MAX_THREAD_IDLE_MILLISECONDS = FlexibleExecutorContext.getDefaultMaxThreadIdleMilliseconds();

  private static final int CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS = 40;
  private static final int CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS = 50;
  private static final int CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS = 60;

  private static final int CUSTOM_LIFE_CYCLE_THREAD_COUNT = 4;
  private static final int CUSTOM_API_THREAD_COUNT = 5;
  private static final int CUSTOM_CALLBACK_THREAD_COUNT = 6;
  private static final int CUSTOM_SCHEDULED_THREAD_COUNT = 7;

  @Test
  public void testConstructorWithoutParameters() throws Exception {
    executorContext = new FlexibleExecutorContext();
    executorContext.init();

    ExecutorService executorService;
    ThreadPoolExecutor threadPoolExecutor;
    long corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

    executorService = executorContext.getLifeCycleExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong life cycle executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong life cycle executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
    Assert.assertEquals("Wrong life cycle executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getApiExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong api executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong api executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
    Assert.assertEquals("Wrong api executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getCallbackExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong callback executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong callback executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
    Assert.assertEquals("Wrong callback executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getScheduledExecutor();
    Assert.assertTrue(executorService instanceof ScheduledThreadPoolExecutor);
    threadPoolExecutor = (ScheduledThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    Assert.assertEquals("Wrong scheduled executor core pool size", DEFAULT_MAX_POOL_SIZE, corePoolSize);
  }

  @Test
  public void testConstructorWithAllParameters() throws Exception {
    executorContext = new FlexibleExecutorContext(CUSTOM_LIFE_CYCLE_THREAD_COUNT, CUSTOM_API_THREAD_COUNT, CUSTOM_CALLBACK_THREAD_COUNT, CUSTOM_SCHEDULED_THREAD_COUNT);
    executorContext.init();

    ExecutorService executorService;
    ThreadPoolExecutor threadPoolExecutor;
    long corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

    executorService = executorContext.getLifeCycleExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong life cycle executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong life cycle executor maximum pool size", CUSTOM_LIFE_CYCLE_THREAD_COUNT, maxPoolSize);
    Assert.assertEquals("Wrong life cycle executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getApiExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong api executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong api executor maximum pool size", CUSTOM_API_THREAD_COUNT, maxPoolSize);
    Assert.assertEquals("Wrong api executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getCallbackExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong callback executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong callback executor maximum pool size", CUSTOM_CALLBACK_THREAD_COUNT, maxPoolSize);
    Assert.assertEquals("Wrong callback executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getScheduledExecutor();
    Assert.assertTrue(executorService instanceof ScheduledThreadPoolExecutor);
    threadPoolExecutor = (ScheduledThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    Assert.assertEquals("Wrong scheduled executor core pool size", CUSTOM_SCHEDULED_THREAD_COUNT, corePoolSize);
  }

  @Test
  public void testConstructorWithBuilder() throws Exception {
    executorContext = new FlexibleExecutorContext.FlexibleExecutorContextBuilder()
        .setLifeCycleThreadCount(CUSTOM_LIFE_CYCLE_THREAD_COUNT)
        .setMaxLifeCycleThreadsIdleMilliseconds(CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS)
        .setApiThreadCount(CUSTOM_API_THREAD_COUNT)
        .setMaxApiThreadsIdleMilliseconds(CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS)
        .setCallbackThreadCount(CUSTOM_CALLBACK_THREAD_COUNT)
        .setMaxCallbackThreadsIdleMilliseconds(CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS)
        .setScheduledThreadCount(CUSTOM_SCHEDULED_THREAD_COUNT)
        .build();
    executorContext.init();

    ExecutorService executorService;
    ThreadPoolExecutor threadPoolExecutor;
    long corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

    executorService = executorContext.getLifeCycleExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong life cycle executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong life cycle executor maximum pool size", CUSTOM_LIFE_CYCLE_THREAD_COUNT, maxPoolSize);
    Assert.assertEquals("Wrong life cycle executor maximum pool size", CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getApiExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong api executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong api executor maximum pool size", CUSTOM_API_THREAD_COUNT, maxPoolSize);
    Assert.assertEquals("Wrong api executor maximum pool size", CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getCallbackExecutor();
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong callback executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
    Assert.assertEquals("Wrong callback executor maximum pool size", CUSTOM_CALLBACK_THREAD_COUNT, maxPoolSize);
    Assert.assertEquals("Wrong callback executor maximum pool size", CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

    executorService = executorContext.getScheduledExecutor();
    Assert.assertTrue(executorService instanceof ScheduledThreadPoolExecutor);
    threadPoolExecutor = (ScheduledThreadPoolExecutor) executorService;
    corePoolSize = threadPoolExecutor.getCorePoolSize();
    Assert.assertEquals("Wrong scheduled executor core pool size", CUSTOM_SCHEDULED_THREAD_COUNT, corePoolSize);
  }

  @Test
  public void testShutdownExecutors() throws Exception {
    executorContext = new FlexibleExecutorContext();
    executorContext.init();

    ExecutorService executorService = executorContext.getLifeCycleExecutor();

    executorContext.stop();
    executorContext.getTimeunit().sleep(executorContext.getTimeout());
    Assert.assertTrue(executorService.isShutdown());
  }
}