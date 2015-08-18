package org.kaaproject.kaa.client.logging.strategies;

import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Start log upload when there is recordsToGoCount records in storage.
 */
public class CustomRecordsCountLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(CustomRecordsCountLogUploadStrategy.class);


    protected long recordsToGoCount;

    public CustomRecordsCountLogUploadStrategy() {
        recordsToGoCount = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    }

    public CustomRecordsCountLogUploadStrategy(long recordToGoCount){
        this.recordsToGoCount = recordToGoCount;
    }

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        if(!UPLOAD_LOCKED) {
            if(status.getRecordCount() == recordsToGoCount){
                LOG.info("Need to upload logs - current count: {}, threshold: {}", status.getRecordCount(), countThreshold);
                decision = LogUploadStrategyDecision.UPLOAD;
            }
        }
        return decision;
    }

    public long getRecordsToGoCount() {
        return recordsToGoCount;
    }

    public void setRecordsToGoCount(long recordsToGoCount) {
        this.recordsToGoCount = recordsToGoCount;
    }
}
