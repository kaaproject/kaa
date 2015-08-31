package org.kaaproject.kaa.client.logging.strategies;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.mockito.Mockito;

public class PeriodicLogUploadStrategyTest {

    @Test
    public void testUploadAfterSomeTime() throws InterruptedException {
        int uploadCheckPeriod = 2000; // 2 Sec
        LogStorageStatus logStorageStatus = Mockito.mock(LogStorageStatus.class);

        PeriodicLogUploadStrategy strategy = new PeriodicLogUploadStrategy(uploadCheckPeriod, TimeUnit.MILLISECONDS);
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
