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
 *  Start log upload when there storage size is >= minStorageSize bytes or records are stored for more then timeLimit TimeUnit units.
 */
public class StorageSizeWithTimeLimitLogUploadStrategy extends DefaultLogUploadStrategy{
    private static final Logger LOG = LoggerFactory.getLogger(StorageSizeWithTimeLimitLogUploadStrategy.class);

    protected long minStorageSize;
    //seconds
    protected long timeLimit;
    protected long lastUploadTime;
    protected TimeUnit timeUnit;

    public StorageSizeWithTimeLimitLogUploadStrategy() {
        minStorageSize = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
        timeLimit = DEFAULT_TIME_LIMIT;
        timeUnit = TimeUnit.MILLISECONDS;
    }

    public StorageSizeWithTimeLimitLogUploadStrategy(long minStorageSize, long timeLimit, TimeUnit timeUnit) {
        this.minStorageSize = minStorageSize;
        this.timeLimit = timeLimit;
        this.timeUnit = timeUnit;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        long currentTime = System.currentTimeMillis();

        if(status.getConsumedVolume() >= minStorageSize){
            LOG.info("Need to upload logs - current size: {}, threshold: {}", status.getConsumedVolume(), volumeThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
            lastUploadTime = currentTime;
        }else if(lastUploadTime != 0 && (currentTime - lastUploadTime) >= timeUnit.toMillis(timeLimit)){
            LOG.info("Need to upload logs - current count: {}, lastUploadedTime: {}, timeLimit: {}", status.getRecordCount(), lastUploadTime, timeLimit);
            decision = LogUploadStrategyDecision.UPLOAD;
            lastUploadTime = currentTime;
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
