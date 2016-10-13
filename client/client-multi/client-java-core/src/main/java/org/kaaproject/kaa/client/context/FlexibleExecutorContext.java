package org.kaaproject.kaa.client.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlexibleExecutorContext extends SimpleExecutorContext implements ExecutorContext {
    private static final Logger LOG = LoggerFactory.getLogger(FlexibleExecutorContext.class);

    private static final int DEFAULT_MAX_THREAD_IDLE_MILLISECONDS = 100;
    private static final int DEFAULT_MAX_THREADS = 16;

    public FlexibleExecutorContext() {
        this(DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS);
    }

    public FlexibleExecutorContext(int maxLifeCycleThreads, int maxApiCallThreads, int maxCallbackThreads, int maxScheduledThreads) {
        super(maxLifeCycleThreads, maxApiCallThreads, maxCallbackThreads, maxScheduledThreads);
    }

    public FlexibleExecutorContext(FlexibleExecutorContextBuilder b) {
        super(
                b.getLifeCycleThreadCount(), b.getApiThreadCount(),
                b.getCallbackThreadCount(), b.getScheduledThreadCount(),
                b.getMaxLifeCycleThreadsIdleMilliseconds(), b.getMaxApiThreadsIdleMilliseconds(),
                b.getMaxCallbackThreadsIdleMilliseconds(), b.getMaxScheduledThreadsIdleMilliseconds()
        );
    }

    @Override
    protected ExecutorService createExecutor(int nThreads, int maxThreadsIdleMilliseconds) {
        return new ThreadPoolExecutor(1, nThreads,
                maxThreadsIdleMilliseconds, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>());
    }

    public static class FlexibleExecutorContextBuilder {

        private int lifeCycleThreadCount;
        private int apiThreadCount;
        private int callbackThreadCount;
        private int scheduledThreadCount;

        private int maxLifeCycleThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        private int maxApiThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        private int maxCallbackThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        private int maxScheduledThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;

        public FlexibleExecutorContextBuilder setLifeCycleThreadCount(int lifeCycleThreadCount) {
            this.lifeCycleThreadCount = lifeCycleThreadCount;
            return this;
        }

        public FlexibleExecutorContextBuilder setApiThreadCount(int apiThreadCount) {
            this.apiThreadCount = apiThreadCount;
            return this;
        }

        public FlexibleExecutorContextBuilder setCallbackThreadCount(int callbackThreadCount) {
            this.callbackThreadCount = callbackThreadCount;
            return this;
        }

        public FlexibleExecutorContextBuilder setScheduledThreadCount(int scheduledThreadCount) {
            this.scheduledThreadCount = scheduledThreadCount;
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

        public FlexibleExecutorContextBuilder setMaxScheduledThreadsIdleMilliseconds(int maxScheduledThreadsIdleMilliseconds) {
            this.maxScheduledThreadsIdleMilliseconds = maxScheduledThreadsIdleMilliseconds;
            return this;
        }


        public FlexibleExecutorContextBuilder setLifeCycleThreadCountAndIdleMilliseconds(
                int lifeCycleThreadCount, int maxLifeCycleThreadsIdleMilliseconds) {
            this.lifeCycleThreadCount = lifeCycleThreadCount;
            this.maxLifeCycleThreadsIdleMilliseconds = maxLifeCycleThreadsIdleMilliseconds;
            return this;
        }

        public FlexibleExecutorContextBuilder setApiThreadCountAndIdleMilliseconds(
                int apiThreadCount, int maxApiThreadsIdleMilliseconds) {
            this.apiThreadCount = apiThreadCount;
            this.maxApiThreadsIdleMilliseconds = maxApiThreadsIdleMilliseconds;
            return this;
        }

        public FlexibleExecutorContextBuilder setCallbackThreadCountAndIdleMilliseconds(
                int callbackThreadCount, int maxCallbackThreadsIdleMilliseconds) {
            this.callbackThreadCount = callbackThreadCount;
            this.maxCallbackThreadsIdleMilliseconds = maxCallbackThreadsIdleMilliseconds;
            return this;
        }

        public FlexibleExecutorContextBuilder setScheduledThreadCountAndIdleMilliseconds(
                int scheduledThreadCount, int maxScheduledThreadsIdleMilliseconds) {
            this.scheduledThreadCount = scheduledThreadCount;
            this.maxScheduledThreadsIdleMilliseconds = maxScheduledThreadsIdleMilliseconds;
            return this;
        }


        public FlexibleExecutorContext build() {
            return new FlexibleExecutorContext(this);
        }

        int getLifeCycleThreadCount() {
            return lifeCycleThreadCount;
        }

        int getApiThreadCount() {
            return apiThreadCount;
        }

        int getCallbackThreadCount() {
            return callbackThreadCount;
        }

        int getScheduledThreadCount() {
            return scheduledThreadCount;
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

        int getMaxScheduledThreadsIdleMilliseconds() {
            return maxScheduledThreadsIdleMilliseconds;
        }
    }
}