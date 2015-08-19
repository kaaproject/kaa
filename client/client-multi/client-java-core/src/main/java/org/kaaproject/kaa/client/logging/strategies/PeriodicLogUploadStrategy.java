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
 * Issue log upload each timeLimit seconds.
 */
public class PeriodicLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicLogUploadStrategy.class);
    //seconds
    protected long timeLimit;
    protected long lastUploadTime;

    public PeriodicLogUploadStrategy() {
        timeLimit = DEFAULT_TIME_LIMIT;
    }

    public PeriodicLogUploadStrategy(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;
        if(!UPLOAD_LOCKED) {
            if(lastUploadTime != 0 && (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastUploadTime) >= timeLimit){
                LOG.info("Need to upload logs - current count: {}, lastUploadedTime: {}, timeLimit: {}", status.getRecordCount(), lastUploadTime, timeLimit);
                decision = LogUploadStrategyDecision.UPLOAD;
                lastUploadTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }
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
