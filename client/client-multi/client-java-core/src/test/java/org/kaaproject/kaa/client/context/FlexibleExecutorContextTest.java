package org.kaaproject.kaa.client.context;

import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

public class FlexibleExecutorContextTest {

    private FlexibleExecutorContext executorContext;

    private static final int DEFAULT_CORE_POOL_SIZE = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_POOL_SIZE = Integer.MAX_VALUE;
    private static final int DEFAULT_MAX_THREAD_IDLE_MILLISECONDS = 100;

    private static final int CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS = 40;
    private static final int CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS = 50;
    private static final int CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS = 60;
    private static final int CUSTOM_MAX_SCHEDULED_THREADS_IDLE_MILLISECONDS = 70;

    private static final int CUSTOM_LIFE_CYCLE_THEAD_COUNT = 4;
    private static final int CUSTOM_API_COUNT = 5;
    private static final int CUSTOM_CALLBACK_COUNT = 6;
    private static final int CUSTOM_SCHEDULED_COUNT = 7;

    @Test
    public void testConstructorWithoutParameters() throws Exception {
        executorContext = new FlexibleExecutorContext();
        executorContext.init();

        ExecutorService executorService;
        int corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

        executorService = executorContext.getLifeCycleExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong life cycle executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong life cycle executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
        Assert.assertEquals("Wrong life cycle executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getApiExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong api executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong api executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
        Assert.assertEquals("Wrong api executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getCallbackExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong callback executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong callback executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
        Assert.assertEquals("Wrong callback executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getScheduledExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong scheduled executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong scheduled executor maximum pool size", DEFAULT_MAX_POOL_SIZE, maxPoolSize);
        Assert.assertEquals("Wrong scheduled executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
    }

    @Test
    public void testConstructorWithAllParameters() throws Exception {
        executorContext = new FlexibleExecutorContext(CUSTOM_LIFE_CYCLE_THEAD_COUNT, CUSTOM_API_COUNT, CUSTOM_CALLBACK_COUNT, CUSTOM_SCHEDULED_COUNT);
        executorContext.init();

        ExecutorService executorService;
        int corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

        executorService = executorContext.getLifeCycleExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong life cycle executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong life cycle executor maximum pool size", CUSTOM_LIFE_CYCLE_THEAD_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong life cycle executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getApiExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong api executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong api executor maximum pool size", CUSTOM_API_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong api executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getCallbackExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong callback executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong callback executor maximum pool size", CUSTOM_CALLBACK_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong callback executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getScheduledExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong scheduled executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong scheduled executor maximum pool size", CUSTOM_SCHEDULED_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong scheduled executor maximum pool size", DEFAULT_MAX_THREAD_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
    }

    @Test
    public void testConstructorWithBuilder() throws Exception {
        executorContext = new FlexibleExecutorContextBuilder()
                .setLifeCycleThreadCountAndIdleMilliseconds(CUSTOM_LIFE_CYCLE_THEAD_COUNT, CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS)
                .setApiThreadCountAndIdleMilliseconds(CUSTOM_API_COUNT, CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS)
                .setCallbackThreadCountAndIdleMilliseconds(CUSTOM_CALLBACK_COUNT, CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS)
                .setScheduledThreadCountAndIdleMilliseconds(CUSTOM_SCHEDULED_COUNT, CUSTOM_MAX_SCHEDULED_THREADS_IDLE_MILLISECONDS)
                .build();
        executorContext.init();

        ExecutorService executorService;
        int corePoolSize, maxPoolSize, maxThreadIdleMilliseconds;

        executorService = executorContext.getLifeCycleExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong life cycle executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong life cycle executor maximum pool size", CUSTOM_LIFE_CYCLE_THEAD_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong life cycle executor maximum pool size", CUSTOM_MAX_LIFE_CYCLE_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getApiExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong api executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong api executor maximum pool size", CUSTOM_LIFE_API_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong api executor maximum pool size", CUSTOM_MAX_API_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getCallbackExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong callback executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong callback executor maximum pool size", CUSTOM_LIFE_CALLBACK_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong callback executor maximum pool size", CUSTOM_MAX_CALLBACK_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);

        executorService = executorContext.getScheduledExecutor();
        corePoolSize = executorService.getCorePoolSize();
        maxPoolSize = executorService.getMaximumPoolSize();
        maxThreadIdleMilliseconds = executorService.getKeepAliveTime();
        Assert.assertEquals("Wrong scheduled executor core pool size", DEFAULT_CORE_POOL_SIZE, corePoolSize);
        Assert.assertEquals("Wrong scheduled executor maximum pool size", CUSTOM_LIFE_SCHEDULED_COUNT, maxPoolSize);
        Assert.assertEquals("Wrong scheduled executor maximum pool size", CUSTOM_MAX_SCHEDULED_THREADS_IDLE_MILLISECONDS, maxThreadIdleMilliseconds);
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