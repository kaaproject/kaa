package org.kaaproject.kaa.client.logging.strategies;

import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Start log upload when there storage size is >= minStorageSize bytes.
 */
public class StorageSizeLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(StorageSizeLogUploadStrategy.class);

    protected long minStorageSize;

    public StorageSizeLogUploadStrategy() {
        minStorageSize = DEFAULT_UPLOAD_VOLUME_THRESHOLD;
    }

    public StorageSizeLogUploadStrategy(long minStorageSize){
        this.minStorageSize = minStorageSize;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        if(status.getConsumedVolume() >= minStorageSize){
            LOG.info("Need to upload logs - current size: {}, threshold: {}", status.getConsumedVolume(), volumeThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
        }

        return decision;
    }

    public long getMinStorageSize() {
        return minStorageSize;
    }

    public void setMinStorageSize(long minStorageSize) {
        this.minStorageSize = minStorageSize;
    }
}
