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
 * Start log upload when there is recordsToGoCount records in storage or records are stored for more then timeLimit seconds.
 */
public class CustomRecordsCountWithTimeLimitLogUploadStrategy extends DefaultLogUploadStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(CustomRecordsCountWithTimeLimitLogUploadStrategy.class);
    protected long recordsToGoCount;
    //seconds
    protected long timeLimit;
    protected long lastUploadTime;

    public CustomRecordsCountWithTimeLimitLogUploadStrategy() {
        recordsToGoCount = DEFAULT_UPLOAD_COUNT_THRESHOLD;
        timeLimit = DEFAULT_TIME_LIMIT;
    }

    public CustomRecordsCountWithTimeLimitLogUploadStrategy(long recordsToGoCount, int timeLimit) {
        this.recordsToGoCount = recordsToGoCount;
        this.timeLimit = timeLimit;
    }

    @Override
    public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
        LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

        if(!UPLOAD_LOCKED) {
            if(status.getRecordCount() == recordsToGoCount){
                LOG.info("Need to upload logs - current count: {}, threshold: {}", status.getRecordCount(), countThreshold);
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

    public long getRecordsToGoCount() {
        return recordsToGoCount;
    }

    public void setRecordsToGoCount(long recordsToGoCount) {
        this.recordsToGoCount = recordsToGoCount;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }
}
