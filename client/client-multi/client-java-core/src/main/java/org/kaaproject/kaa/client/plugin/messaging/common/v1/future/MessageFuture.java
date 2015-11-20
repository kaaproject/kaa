/*
 * Copyright 2014-2015 CyberVision, Inc.
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
package org.kaaproject.kaa.client.plugin.messaging.common.v1.future;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.kaaproject.kaa.client.plugin.messaging.common.v1.msg.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageFuture<V> implements Future<V> {
    private static final Logger LOG = LoggerFactory.getLogger(MessageFuture.class);

    private static enum State {
        WAITING, CANCELLED, DONE
    }

    private final Message msg;
    private final WeakReference<MessageFutureCancelListener> listener;
    private BlockingQueue<ExecutionResult<V>> queue = new ArrayBlockingQueue<ExecutionResult<V>>(1);
    private volatile State state = State.WAITING;

    public MessageFuture(Message msg, MessageFutureCancelListener listener) {
        this.msg = msg;
        this.listener = new WeakReference<>(listener);
    }

    public UUID getUid() {
        return msg.getUid();
    }

    public Message getMsg() {
        return msg;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        state = State.CANCELLED;
        MessageFutureCancelListener listenerObj = listener.get();
        if (listenerObj != null) {
            listenerObj.onCanceled(getUid());
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return state == State.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state == State.DONE;
    }

    public void setValue(V value) {
        try {
            this.queue.put(new ExecutionResult<V>(value, null));
        } catch (InterruptedException e) {
            LOG.warn("Failed to push value", e);
        }
        state = State.DONE;
    }

    public void setFailure(Exception failure) {
        try {
            this.queue.put(new ExecutionResult<V>(null, failure));
        } catch (InterruptedException e) {
            LOG.warn("Failed to push value", e);
        }
        state = State.DONE;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        ExecutionResult<V> result = queue.take();
        return processResult(result);
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutionResult<V> result = queue.poll(timeout, unit);
        return processResult(result);
    }

    private V processResult(ExecutionResult<V> result) throws ExecutionException {
        if (result.getE() == null) {
            return result.get();
        } else {
            throw new ExecutionException(result.getE());
        }
    }

}
