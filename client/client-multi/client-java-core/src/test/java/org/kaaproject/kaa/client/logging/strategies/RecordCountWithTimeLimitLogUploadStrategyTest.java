package org.kaaproject.kaa.client.logging.strategies;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.mockito.Mockito;

public class RecordCountWithTimeLimitLogUploadStrategyTest {

    @Test
    public void testLessThanRecordThresholdCount() {
        int thresholdCount = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)(thresholdCount - 1));

        RecordCountWithTimeLimitLogUploadStrategy strategy =
                new RecordCountWithTimeLimitLogUploadStrategy(thresholdCount, uploadCheckPeriod, TimeUnit.MILLISECONDS);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);
    }

    @Test
    public void testEqualToRecordThresholdCount() {
        int thresholdCount = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)thresholdCount);

        RecordCountWithTimeLimitLogUploadStrategy strategy =
                new RecordCountWithTimeLimitLogUploadStrategy(thresholdCount, uploadCheckPeriod, TimeUnit.MILLISECONDS);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

    @Test
    public void testGreaterThanRecordThresholdCount() {
        int thresholdCount = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)(thresholdCount + 1));

        RecordCountWithTimeLimitLogUploadStrategy strategy =
                new RecordCountWithTimeLimitLogUploadStrategy(thresholdCount, uploadCheckPeriod, TimeUnit.MILLISECONDS);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

    @Test
    public void testUploadAfterSomeTime() throws InterruptedException {
        int thresholdCount = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getRecordCount()).thenReturn((long)0);

        RecordCountWithTimeLimitLogUploadStrategy strategy =
                new RecordCountWithTimeLimitLogUploadStrategy(thresholdCount, uploadCheckPeriod, TimeUnit.MILLISECONDS);
        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);

        Thread.sleep(uploadCheckPeriod / 2);
        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);

        Thread.sleep(uploadCheckPeriod / 2);
        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);

        Thread.sleep(uploadCheckPeriod / 2);
        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);

        Thread.sleep(uploadCheckPeriod / 2);
        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

}
