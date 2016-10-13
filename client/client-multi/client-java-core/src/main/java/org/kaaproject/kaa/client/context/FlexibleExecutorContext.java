package org.kaaproject.kaa.client.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class FlexibleExecutorContext extends AbstractExecutorContext implements ExecutorContext {
    private static final Logger LOG = LoggerFactory.getLogger(FlexibleExecutorContext.class);

    private static final int DEFAULT_MAX_THREAD_IDLE_MILLISECONDS = 100;
    private static final int DEFAULT_MAX_THREADS = Integer.MAX_VALUE;

    private final int lifeCycleThreadCount;
    private final int apiThreadCount;
    private final int callbackThreadCount;
    private final int scheduledThreadCount;

    private final int maxLifeCycleThreadsIdleMilliseconds;
    private final int maxApiThreadsIdleMilliseconds;
    private final int maxCallbackThreadsIdleMilliseconds;

    private ExecutorService lifeCycleExecutor;
    private ExecutorService apiExecutor;
    private ExecutorService callbackExecutor;
    private ScheduledExecutorService scheduledExecutor;

    public FlexibleExecutorContext() {
        this(DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS);
    }

    /**
     * All-args constructor.
     */
    public FlexibleExecutorContext(int lifeCycleThreadCount, int apiThreadCount,
                                   int callbackThreadCount, int scheduledThreadCount) {
        this.lifeCycleThreadCount = lifeCycleThreadCount;
        this.apiThreadCount = apiThreadCount;
        this.callbackThreadCount = callbackThreadCount;
        this.scheduledThreadCount = scheduledThreadCount;

        maxLifeCycleThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        maxCallbackThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        maxApiThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
    }

    /**
     * Constructor for Builder pattern.
     */
    public FlexibleExecutorContext(FlexibleExecutorContextBuilder builder) {
        lifeCycleThreadCount = builder.getLifeCycleThreadCount();
        apiThreadCount = builder.getApiThreadCount();
        callbackThreadCount =  builder.getCallbackThreadCount();
        scheduledThreadCount = builder.getScheduledThreadCount();
        maxLifeCycleThreadsIdleMilliseconds = builder.getMaxLifeCycleThreadsIdleMilliseconds();
        maxCallbackThreadsIdleMilliseconds = builder.getMaxCallbackThreadsIdleMilliseconds();
        maxApiThreadsIdleMilliseconds = builder.getMaxApiThreadsIdleMilliseconds();
    }

    @Override
    public void init() {
        LOG.debug("Creating executor services");
        lifeCycleExecutor = createExecutor(lifeCycleThreadCount, maxLifeCycleThreadsIdleMilliseconds);
        apiExecutor = createExecutor(apiThreadCount, maxApiThreadsIdleMilliseconds);
        callbackExecutor = createExecutor(callbackThreadCount, maxCallbackThreadsIdleMilliseconds);
        scheduledExecutor = createScheduledExecutor(scheduledThreadCount);
        LOG.debug("Created executor services");
    }

    @Override
    public void stop() {
        shutdownExecutor(lifeCycleExecutor);
        shutdownExecutor(apiExecutor);
        shutdownExecutor(callbackExecutor);
        shutdownExecutor(scheduledExecutor);
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

    @Override
    public ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    private ExecutorService createExecutor(int nThreads, int maxThreadsIdleMilliseconds) {
        return new ThreadPoolExecutor(1, nThreads,
                maxThreadsIdleMilliseconds, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>());
    }

    private ScheduledExecutorService createScheduledExecutor(int nThreads) {
        return Executors.newScheduledThreadPool(nThreads);
    }

    public static class FlexibleExecutorContextBuilder {

        private int lifeCycleThreadCount;
        private int apiThreadCount;
        private int callbackThreadCount;
        private int scheduledThreadCount;

        private int maxLifeCycleThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        private int maxApiThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;
        private int maxCallbackThreadsIdleMilliseconds = DEFAULT_MAX_THREAD_IDLE_MILLISECONDS;

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


        public FlexibleExecutorContext build() {
            FlexibleExecutorContext f = new FlexibleExecutorContext(this);
            return f;
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
    }
}