package org.kaaproject.kaa.client.logging.strategies;

import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference implementation for {@link LogUploadStrategy}.
 * Start log upload when there is countThreshold records in storage.
 */
public class RecordCountLogUploadStrategy extends DefaultLogUploadStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(RecordCountLogUploadStrategy.class);

    public RecordCountLogUploadStrategy() {}

    public RecordCountLogUploadStrategy(int countThreshold){
        this.countThreshold = countThreshold;
    }

    @Override
    protected LogUploadStrategyDecision checkUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;


        if(status.getRecordCount() == countThreshold){
            LOG.info("Need to upload logs - current count: {}, threshold: {}", status.getRecordCount(), countThreshold);
            decision = LogUploadStrategyDecision.UPLOAD;
        }

        return decision;
    }

    public long getRecordsThreshold() {
        return countThreshold;
    }

    public void setRecordsThreshold(int countThreshold) {
        this.countThreshold = countThreshold;
    }
}
