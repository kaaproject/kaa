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

package org.kaaproject.kaa.demo.smarthousedemo.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class TimeoutExecutor {

    private final ExecutorService executor = Executors.newCachedThreadPool();
    
    public TimeoutExecutor() {
    }
    
    public <V> V execute(BlockingCallable<V> callable, long timeoutMillis) throws Throwable {
        Future<V> future = executor.submit(callable);
        try {
            V result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            return result;
        }
        catch (Exception e) {
            future.cancel(true);
            if (e instanceof ExecutionException) {
                throw e.getCause();
            }
            else {
                throw e;
            }
        }
    }
}
