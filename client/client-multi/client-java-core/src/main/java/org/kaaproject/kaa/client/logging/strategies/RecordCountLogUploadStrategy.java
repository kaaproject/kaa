package org.kaaproject.kaa.client.logging.strategies;

import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Start log upload when there is recordsThreshold records in storage.
 */
public class RecordCountLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(RecordCountLogUploadStrategy.class);


    protected long recordsThreshold;

    public RecordCountLogUploadStrategy() {
        recordsThreshold = DEFAULT_UPLOAD_COUNT_THRESHOLD;
    }

    public RecordCountLogUploadStrategy(long recordToGoCount){
        this.recordsThreshold = recordToGoCount;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;


        if(status.getRecordCount() == recordsThreshold){
            LOG.info("Need to upload logs - current count: {}, threshold: {}", status.getRecordCount(), countThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
        }

        return decision;
    }

    public long getRecordsThreshold() {
        return recordsThreshold;
    }

    public void setRecordsThreshold(long recordsThreshold) {
        this.recordsThreshold = recordsThreshold;
    }
}
