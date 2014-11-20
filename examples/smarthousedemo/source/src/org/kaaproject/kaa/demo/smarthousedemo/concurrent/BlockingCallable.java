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

import java.util.concurrent.Callable;

public abstract class BlockingCallable<V> implements Callable<V> {

    protected volatile V result;
    protected volatile Exception exception;
    
    private boolean waitForResult; 
    
    public BlockingCallable() {
        this(true);
    }
    
    public BlockingCallable(boolean waitForResult) {
        this.waitForResult = waitForResult;
    }
    
    public V execute(TimeoutExecutor executor, long timeoutMillis) throws Throwable {
        return executor.execute(this, timeoutMillis);
    }
    
    public V call() throws Exception {
        executeAsync();
        if (waitForResult) {
            synchronized(this) {
                wait();
            }
        }
        if (exception != null) {
            throw exception;
        }
        return result;
    }
    
    public void onComplete(V result) {
        this.result = result;
        synchronized(this) {
            notify();
        }
    }
    
    protected void onException(Exception exception) {
        this.exception = exception;
        synchronized(this) {
            notify();
        }
    }
    
    protected abstract void executeAsync();
    
    
}
