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

import org.kaaproject.kaa.client.KaaClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Responsible for creation of {@link ExecutorService executor} instances for
 * SDK internal usage. Implementation should not manage created
 * {@link ExecutorService executor} life-cycle. Executors will be stopped during
 * {@link KaaClient#stop()}, thus {@link ExecutorService executor} instances
 * should not be cached in context or context should check shutdown status
 * before return of cached value.
 *
 * @author Andrew Shvayka
 */
public interface ExecutorContext {

  /**
   * Initialize executors.
   */
  void init();

  /**
   * Stops executors.
   */
  void stop();

  /**
   * Executes lifecycle events/commands of Kaa client.
   *
   * @return the lifecycle executor
   */
  ExecutorService getLifeCycleExecutor();

  /**
   * Executes user API calls to SDK client. For example, serializing of log
   * records before submit to transport.
   *
   * @return the API executor
   */
  ExecutorService getApiExecutor();

  /**
   * Executes callback methods provided by SDK client user.
   *
   * @return the callback executor
   */
  ExecutorService getCallbackExecutor();

  /**
   * Executes scheduled tasks(periodically if needed) as log upload.
   *
   * @return the scheduled executor
   */
  ScheduledExecutorService getScheduledExecutor();
}
