package org.kaaproject.kaa.client.logging.strategies;

import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Issue log upload each timeLimit timeUnit units.
 */
public class PeriodicLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicLogUploadStrategy.class);
    protected long lastUploadTime;
    protected TimeUnit timeUnit;

    public PeriodicLogUploadStrategy() {
        timeUnit = TimeUnit.SECONDS;
    }

    public PeriodicLogUploadStrategy(long timeLimit, TimeUnit timeUnit) {
        this.timeLimit = timeLimit;
        this.timeUnit = timeUnit;
    }


    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        long currentTime = System.currentTimeMillis();

        if(lastUploadTime != 0 && (currentTime - lastUploadTime) >= timeUnit.toMillis(timeLimit)){
            LOG.info("Need to upload logs - current count: {}, lastUploadedTime: {}, timeLimit: {}", status.getRecordCount(), lastUploadTime, timeLimit);
            decision = LogUploadStrategyDecision.UPLOAD;
            lastUploadTime = currentTime;
        }

        return decision;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
