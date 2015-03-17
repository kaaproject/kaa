/*
 * Copyright 2014 CyberVision, Inc.
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of {@link ExecutorContext executorContext} that uses
 * one thread per each executor
 * 
 * @author Andrew Shvayka
 *
 */
public class SimpleExecutorContext extends AbstractExecutorContext implements ExecutorContext {
    private static final Logger LOG = LoggerFactory.getLogger(SingleThreadExecutorContext.class);

    private static final int SINGLE_THREAD = 1;

    private final int lifeCycleThreadCount;
    private final int apiThreadCount;
    private final int callbackThreadCount;

    private ExecutorService lifeCycleExecutor;
    private ExecutorService apiExecutor;
    private ExecutorService callbackExecutor;

    public SimpleExecutorContext() {
        this(SINGLE_THREAD, SINGLE_THREAD, SINGLE_THREAD);
    }

    public SimpleExecutorContext(int lifeCycleThreadCount, int apiThreadCount, int callbackThreadCount) {
        super();
        this.lifeCycleThreadCount = lifeCycleThreadCount;
        this.apiThreadCount = apiThreadCount;
        this.callbackThreadCount = callbackThreadCount;
    }

    @Override
    public void init() {
        LOG.debug("Creating executor services");
        lifeCycleExecutor = createExecutor(lifeCycleThreadCount);
        apiExecutor = createExecutor(apiThreadCount);
        callbackExecutor = createExecutor(callbackThreadCount);
        LOG.debug("Created executor services");
    }

    @Override
    public void stop() {
        shutdownExecutor(lifeCycleExecutor);
        shutdownExecutor(apiExecutor);
        shutdownExecutor(callbackExecutor);
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

    private ExecutorService createExecutor(int nThreads) {
        if (nThreads == 1) {
            return Executors.newSingleThreadExecutor();
        } else {
            return Executors.newFixedThreadPool(nThreads);
        }
    }
}
