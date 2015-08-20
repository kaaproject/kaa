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
 *  Start log upload when there storage size is >= volumeThreshold bytes or records are stored for more then timeLimit TimeUnit units.
 */
public class StorageSizeWithTimeLimitLogUploadStrategy extends DefaultLogUploadStrategy{
    private static final Logger LOG = LoggerFactory.getLogger(StorageSizeWithTimeLimitLogUploadStrategy.class);

    protected long lastUploadTime;
    protected TimeUnit timeUnit;

    public StorageSizeWithTimeLimitLogUploadStrategy() {
        timeUnit = TimeUnit.SECONDS;
    }

    public StorageSizeWithTimeLimitLogUploadStrategy(int volumeThreshold, long timeLimit, TimeUnit timeUnit) {
        this.volumeThreshold = volumeThreshold;
        this.timeLimit = timeLimit;
        this.timeUnit = timeUnit;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        long currentTime = System.currentTimeMillis();

        if(status.getConsumedVolume() >= volumeThreshold){
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

    public long getVolumeThreshold() {
        return volumeThreshold;
    }

    public void setVolumeThreshold(int volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
