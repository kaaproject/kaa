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

/**
 * 
 */
package org.kaaproject.kaa.server.common.thrift.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Class ThriftExecutor.
 *
 * @author Andrey Panasenko <apanasenko@cybervisiontech.com>
 */
public class ThriftExecutor {

    /** The executor. */
    private static ExecutorService executor = null;

    private ThriftExecutor() {
    }

    /**
     * Execute.
     *
     * @param client the client
     */
    public static void execute(Runnable client) {
        getExecutorService().execute(client);
    }
    
    /**
     * Shutdown.
     */
    public static void shutdown() {
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }
    
    /**
     * Gets the executor service.
     *
     * @return the executor service
     */
    private static synchronized ExecutorService getExecutorService() {
        if (executor == null) {
            executor = Executors.newCachedThreadPool();
        }
        return executor;
    }
    
}
