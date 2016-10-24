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

  private static final int CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS = 40;
  private static final int CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS = 50;
  private static final int CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS = 60;

  private static final int CUSTOM_MAX_LIFE_CYCLE_THREADS = 4;
  private static final int CUSTOM_MAX_API_THREADS = 5;
  private static final int CUSTOM_MAX_CALLBACK_THREADS = 6;
  private static final int CUSTOM_MIN_SCHEDULED_THREADS = 7;

  @Test
  public void testConstructorWithoutParameters() throws Exception {
    executorContext = new FlexibleExecutorContext();
    executorContext.init();

    ExecutorService executorService;
//    ThreadPoolExecutor threadPoolExecutor;
//    long corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

//    executorService = executorContext.getLifeCycleExecutor();
//    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
//    threadPoolExecutor = (ThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
//    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
//    Assert.assertEquals("Wrong life cycle executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MIN_THREADS, corePoolSize);
//    Assert.assertEquals("Wrong life cycle executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREADS, maxPoolSize);
//    Assert.assertEquals("Wrong life cycle executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
//
//    executorService = executorContext.getApiExecutor();
//    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
//    threadPoolExecutor = (ThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
//    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
//    Assert.assertEquals("Wrong api executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MIN_THREADS, corePoolSize);
//    Assert.assertEquals("Wrong api executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREADS, maxPoolSize);
//    Assert.assertEquals("Wrong api executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
//
//    executorService = executorContext.getCallbackExecutor();
//    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
//    threadPoolExecutor = (ThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
//    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
//    Assert.assertEquals("Wrong callback executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MIN_THREADS, corePoolSize);
//    Assert.assertEquals("Wrong callback executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREADS, maxPoolSize);
//    Assert.assertEquals("Wrong callback executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
//
//    executorService = executorContext.getScheduledExecutor();
//    Assert.assertTrue(executorService instanceof ScheduledThreadPoolExecutor);
//    threadPoolExecutor = (ScheduledThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    Assert.assertEquals("Wrong scheduled executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREADS, corePoolSize);


    executorService = executorContext.getLifeCycleExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS,
        "life cycle");
    executorService = executorContext.getApiExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS,
        "api");
    executorService = executorContext.getCallbackExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS,
        "callback");
    executorService = executorContext.getScheduledExecutor();
    checkScheduledExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS);
  }

  @Test
  public void testConstructorWithAllParameters() throws Exception {
    executorContext = new FlexibleExecutorContext(
        CUSTOM_MAX_LIFE_CYCLE_THREADS, CUSTOM_MAX_API_THREADS,
        CUSTOM_MAX_CALLBACK_THREADS, CUSTOM_MIN_SCHEDULED_THREADS
    );
    executorContext.init();

    ExecutorService executorService;
//    ThreadPoolExecutor threadPoolExecutor;
//    long corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;
//
//    executorService = executorContext.getLifeCycleExecutor();
//    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
//    threadPoolExecutor = (ThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
//    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
//    Assert.assertEquals("Wrong life cycle executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MIN_THREADS, corePoolSize);
//    Assert.assertEquals("Wrong life cycle executor maximum pool size",
//        CUSTOM_MAX_LIFE_CYCLE_THREADS, maxPoolSize);
//    Assert.assertEquals("Wrong life cycle executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
//
//    executorService = executorContext.getApiExecutor();
//    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
//    threadPoolExecutor = (ThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
//    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
//    Assert.assertEquals("Wrong api executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MIN_THREADS, corePoolSize);
//    Assert.assertEquals("Wrong api executor maximum pool size",
//        CUSTOM_MAX_API_THREADS, maxPoolSize);
//    Assert.assertEquals("Wrong api executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
//
//    executorService = executorContext.getCallbackExecutor();
//    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
//    threadPoolExecutor = (ThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
//    maxThreadIdleMilliseconds = threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
//    Assert.assertEquals("Wrong callback executor core pool size",
//        FlexibleExecutorContext.DEFAULT_MIN_THREADS, corePoolSize);
//    Assert.assertEquals("Wrong callback executor maximum pool size",
//        CUSTOM_MAX_CALLBACK_THREADS, maxPoolSize);
//    Assert.assertEquals("Wrong callback executor maximum pool size",
//        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
//
//    executorService = executorContext.getScheduledExecutor();
//    Assert.assertTrue(executorService instanceof ScheduledThreadPoolExecutor);
//    threadPoolExecutor = (ScheduledThreadPoolExecutor) executorService;
//    corePoolSize = threadPoolExecutor.getCorePoolSize();
//    Assert.assertEquals("Wrong scheduled executor core pool size",
//        CUSTOM_MIN_SCHEDULED_THREADS, corePoolSize);


    executorService = executorContext.getLifeCycleExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        CUSTOM_MAX_LIFE_CYCLE_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS,
        "life cycle");
    executorService = executorContext.getApiExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        CUSTOM_MAX_API_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS,
        "api");
    executorService = executorContext.getCallbackExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        CUSTOM_MAX_CALLBACK_THREADS,
        FlexibleExecutorContext.DEFAULT_MAX_THREAD_IDLE_MILLISECONDS,
        "callback");
    executorService = executorContext.getScheduledExecutor();
    checkScheduledExecutorServices(executorService,
        CUSTOM_MIN_SCHEDULED_THREADS);
  }

  @Test
  public void testConstructorWithBuilder() throws Exception {
    executorContext = new FlexibleExecutorContext.FlexibleExecutorContextBuilder()
        .setMaxLifeCycleThreads(CUSTOM_MAX_LIFE_CYCLE_THREADS)
        .setMaxLifeCycleThreadsIdleMilliseconds(CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS)
        .setMaxApiThreads(CUSTOM_MAX_API_THREADS)
        .setMaxApiThreadsIdleMilliseconds(CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS)
        .setMaxCallbackThreads(CUSTOM_MAX_CALLBACK_THREADS)
        .setMaxCallbackThreadsIdleMilliseconds(CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS)
        .setMinScheduledThreads(CUSTOM_MIN_SCHEDULED_THREADS)
        .build();
    executorContext.init();

    ExecutorService executorService;

    executorService = executorContext.getLifeCycleExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        CUSTOM_MAX_LIFE_CYCLE_THREADS,
        CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS,
        "life cycle");
    executorService = executorContext.getApiExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        CUSTOM_MAX_API_THREADS,
        CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS,
        "api");
    executorService = executorContext.getCallbackExecutor();
    checkThreadPoolExecutorServices(executorService,
        FlexibleExecutorContext.DEFAULT_MIN_THREADS,
        CUSTOM_MAX_CALLBACK_THREADS,
        CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS,
        "callback");
    executorService = executorContext.getScheduledExecutor();
    checkScheduledExecutorServices(executorService,
        CUSTOM_MIN_SCHEDULED_THREADS);
  }


  private void checkThreadPoolExecutorServices(ExecutorService executorService,
                                               int minThreads, int maxThreads,
                                               int maxIdleMilliseconds,
                                               String executorName) {
    ThreadPoolExecutor threadPoolExecutor;
    int actualMinThreads;
    int actualMaxThreads;
    int actualMaxThreadIdleMilliseconds;
    Assert.assertTrue(executorService instanceof ThreadPoolExecutor);
    threadPoolExecutor = (ThreadPoolExecutor) executorService;
    actualMinThreads = threadPoolExecutor.getCorePoolSize();
    actualMaxThreads = threadPoolExecutor.getMaximumPoolSize();
    actualMaxThreadIdleMilliseconds = (int) threadPoolExecutor.getKeepAliveTime(TimeUnit.MILLISECONDS);
    Assert.assertEquals("Wrong " + executorName + " executor core pool size",
        minThreads, actualMinThreads);
    Assert.assertEquals("Wrong " + executorName + " executor maximum pool size",
        maxThreads, actualMaxThreads);
    Assert.assertEquals("Wrong " + executorName + " executor maximum pool size",
        maxIdleMilliseconds, actualMaxThreadIdleMilliseconds);
  }

  private void checkScheduledExecutorServices(ExecutorService executorService,
                                              int minThreads) {
    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    int actualMinThreads;
    Assert.assertTrue(executorService instanceof ScheduledThreadPoolExecutor);
    scheduledThreadPoolExecutor = (ScheduledThreadPoolExecutor) executorService;
    actualMinThreads = scheduledThreadPoolExecutor.getCorePoolSize();
    System.out.println("FlexibleExecutorContextTest.checkScheduledExecutorServices");
    System.out.println("minThreads = " + minThreads);
    System.out.println("actualMinThreads = " + actualMinThreads);
    Assert.assertEquals("Wrong scheduled executor core pool size",
        minThreads, actualMinThreads);
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