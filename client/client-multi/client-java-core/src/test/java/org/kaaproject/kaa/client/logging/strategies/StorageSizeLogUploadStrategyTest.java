package org.kaaproject.kaa.client.logging.strategies;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.mockito.Mockito;

public class StorageSizeLogUploadStrategyTest {

    @Test
    public void testLessThanVolumeThreshold() {
        int thresholdVolume = 5;

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)(thresholdVolume - 1));

        StorageSizeLogUploadStrategy strategy = new StorageSizeLogUploadStrategy(thresholdVolume);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.NOOP);
    }

    @Test
    public void testEqualToVolumeThreshold() {
        int thresholdVolume = 5;

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)thresholdVolume);

        StorageSizeLogUploadStrategy strategy = new StorageSizeLogUploadStrategy(thresholdVolume);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }

    @Test
    public void testGreaterThanVolumeThreshold() {
        int thresholdVolume = 5;

        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);
        Mockito.when(logStorageStatus.getConsumedVolume()).thenReturn((long)(thresholdVolume + 1));

        StorageSizeLogUploadStrategy strategy = new StorageSizeLogUploadStrategy(thresholdVolume);

        Assert.assertEquals(strategy.checkUploadNeeded(logStorageStatus), LogUploadStrategyDecision.UPLOAD);
    }
}
