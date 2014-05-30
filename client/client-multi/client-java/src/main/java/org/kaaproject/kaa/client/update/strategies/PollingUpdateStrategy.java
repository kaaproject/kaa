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

package org.kaaproject.kaa.client.update.strategies;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.kaaproject.kaa.client.update.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polling {@link UpdateStrategy}
 *
 * @author Yaroslav Zeygerman
 *
 */
public class PollingUpdateStrategy implements UpdateStrategy {
    
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(PollingUpdateStrategy.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> pollFuture;
    private final Integer initialDelay;
    private final Integer pollPeriod;
    private final TimeUnit pollUnit;
    private final PollingTaskContainer taskContainer;

    private final Runnable task = new Runnable() {

        @Override
        public void run() {
            taskContainer.getNextTask().execute();
        }
    };

    public PollingUpdateStrategy(PollingTaskContainer container, Integer initialDelay,
            Integer period, TimeUnit unit) {
        this.taskContainer = container;
        this.initialDelay = initialDelay;
        this.pollPeriod = period;
        this.pollUnit = unit;
    }

    private void stopPollScheduler(boolean forced) {
        if (pollFuture != null) {
            LOG.info("Stoping poll future..");
            pollFuture.cancel(forced);
        }
    }

    @Override
    public void startPoll() {
        stopPollScheduler(true);
        LOG.info("Starting poll scheduler..");
        pollFuture = scheduler.scheduleAtFixedRate(task, initialDelay, pollPeriod, pollUnit);
        LOG.info("Poll scheduler started");
    }

    @Override
    public void stopPoll() {
        stopPollScheduler(true);
    }

    @Override
    public void retryCommand(Long milliseconds, Command cmd) {
        stopPollScheduler(false);
        LOG.info("Suspending poll scheduler for {} milliseconds", milliseconds);
        pollFuture = scheduler.schedule(new RunnableCommand(cmd), milliseconds, TimeUnit.MILLISECONDS);
    }

    @Override
    public void executeCommand(Command cmd) {
        scheduler.submit(new RunnableCommand(cmd));
    }

}
