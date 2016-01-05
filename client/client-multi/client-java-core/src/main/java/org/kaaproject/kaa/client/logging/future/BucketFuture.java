package org.kaaproject.kaa.client.logging.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class BucketFuture<T> implements Future<T> {
    private static final Logger LOG = LoggerFactory.getLogger(BucketFuture.class);

    private BlockingQueue<ExecutionResult<T>> queue = new ArrayBlockingQueue<>(1);
    private volatile State state = State.WAITING;

    private enum State {
        WAITING, DONE
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return state == State.DONE;
    }

    public void setValue(T value) {
        try {
            this.queue.put(new ExecutionResult<>(value, null));
        } catch (InterruptedException e) {
            LOG.warn("Failed to push value", e);
        }
        state = State.DONE;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        ExecutionResult<T> result = queue.take();
        return processResult(result);
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        ExecutionResult<T> result = queue.poll(timeout, unit);
        return processResult(result);
    }

    private T processResult(ExecutionResult<T> result) throws ExecutionException {
        if (result.getE() == null) {
            return result.get();
        } else {
            throw new ExecutionException(result.getE());
        }
    }
}
