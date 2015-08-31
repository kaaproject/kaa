package org.kaaproject.kaa.client.logging.strategies;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.mockito.Mockito;

public class RecordCountLogUploadStrategyTest {

    @Test
    public void testLessThanRecordThresholdCount() {
        int thresholdCount = 5;

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)(thresholdCount - 1));

        RecordCountLogUploadStrategy strategy = new RecordCountLogUploadStrategy(thresholdCount);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);
    }

    @Test
    public void testEqualToRecordThresholdCount() {
        int thresholdCount = 5;

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)thresholdCount);

        RecordCountLogUploadStrategy strategy = new RecordCountLogUploadStrategy(thresholdCount);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

    @Test
    public void testGreaterThanRecordThresholdCount() {
        int thresholdCount = 5;

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)(thresholdCount + 1));

        RecordCountLogUploadStrategy strategy = new RecordCountLogUploadStrategy(thresholdCount);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }
}
