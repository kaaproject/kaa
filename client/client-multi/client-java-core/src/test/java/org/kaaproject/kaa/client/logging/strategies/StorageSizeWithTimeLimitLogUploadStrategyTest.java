package org.kaaproject.kaa.client.logging.strategies;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.mockito.Mockito;

public class StorageSizeWithTimeLimitLogUploadStrategyTest {

    @Test
    public void testLessThanRecordThresholdCount() {
        int thresholdVolume = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)(thresholdVolume - 1));

        StorageSizeWithTimeLimitLogUploadStrategy strategy =
                new StorageSizeWithTimeLimitLogUploadStrategy(thresholdVolume, uploadCheckPeriod, TimeUnit.MILLISECONDS);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);
    }

    @Test
    public void testEqualToRecordThresholdCount() {
        int thresholdVolume = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)thresholdVolume);

        StorageSizeWithTimeLimitLogUploadStrategy strategy =
                new StorageSizeWithTimeLimitLogUploadStrategy(thresholdVolume, uploadCheckPeriod, TimeUnit.MILLISECONDS);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

    @Test
    public void testGreaterThanRecordThresholdCount() {
        int thresholdVolume = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)(thresholdVolume + 1));

        StorageSizeWithTimeLimitLogUploadStrategy strategy =
                new StorageSizeWithTimeLimitLogUploadStrategy(thresholdVolume, uploadCheckPeriod, TimeUnit.MILLISECONDS);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

    @Test
    public void testUploadAfterSomeTime() throws InterruptedException {
        int thresholdVolume = 5;
        int uploadCheckPeriod = 2000; // 2 Sec

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)0);

        StorageSizeWithTimeLimitLogUploadStrategy strategy =
                new StorageSizeWithTimeLimitLogUploadStrategy(thresholdVolume, uploadCheckPeriod, TimeUnit.MILLISECONDS);
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
