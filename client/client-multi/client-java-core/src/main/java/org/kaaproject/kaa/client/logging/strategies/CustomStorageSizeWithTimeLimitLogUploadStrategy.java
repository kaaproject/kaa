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
 *  Start log upload when there storage size is >= minStorageSize bytes or records are stored for more then timeLimit seconds.
 */
public class CustomStorageSizeWithTimeLimitLogUploadStrategy extends DefaultLogUploadStrategy{
    private static final Logger LOG = LoggerFactory.getLogger(CustomStorageSizeWithTimeLimitLogUploadStrategy.class);

    protected long minStorageSize;
    //seconds
    protected long timeLimit;
    protected long lastUploadTime;

    public CustomStorageSizeWithTimeLimitLogUploadStrategy() {
        minStorageSize = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
        timeLimit = DEFAULT_TIME_LIMIT;
    }

    public CustomStorageSizeWithTimeLimitLogUploadStrategy(long minStorageSize, long timeLimit) {
        this.minStorageSize = minStorageSize;
        this.timeLimit = timeLimit;
    }

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        if(!UPLOAD_LOCKED) {
            if(status.getConsumedVolume() >= minStorageSize){
                LOG.info("Need to upload logs - current size: {}, threshold: {}", status.getConsumedVolume(), volumeThreshold);
                decision = LogUploadStrategyDecision.UPLOAD;
                lastUploadTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }else if(lastUploadTime != 0 && (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - lastUploadTime) >= timeLimit){
                LOG.info("Need to upload logs - current count: {}, lastUploadedTime: {}, timeLimit: {}", status.getRecordCount(), lastUploadTime, timeLimit);
                decision = LogUploadStrategyDecision.UPLOAD;
                lastUploadTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
            }
        }
        return decision;
    }

    public long getMinStorageSize() {
        return minStorageSize;
    }

    public void setMinStorageSize(long minStorageSize) {
        this.minStorageSize = minStorageSize;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
