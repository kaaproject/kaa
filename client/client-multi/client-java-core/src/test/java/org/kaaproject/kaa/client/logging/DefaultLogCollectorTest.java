/*
 * Copyright 2014 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.client.logging;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.schema.base.Log;
import org.mockito.Mockito;

public class DefaultLogCollectorTest {

    @Test
    public void testNewLogUploadConfiguration() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
//        DefaultLogUploadConfiguration conf = new DefaultLogUploadConfiguration.Builder()
//                                                    .setBatchVolume(3)
//                                                    .setVolumeThreshold(4)
//                                                    .setMaximumAllowedVolume(50)
//                                                    .setLogUploadTimeout(300)
//                                                    .build();
        LogTransport transport = Mockito.mock(LogTransport.class);
        DefaultLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
        DefaultLogUploadStrategy strategy = new DefaultLogUploadStrategy();
        strategy.setCountThreshold(5);
        logCollector.setUploadStrategy(strategy);

        try {
            Log record = new Log();

            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);

            verify(transport, times(1)).sync();
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }
//
//    private static class CustomStrategy implements LogUploadStrategy {
//        @Override
//        public LogUploadStrategyDecision isUploadNeeded(LogUploadConfiguration configuration, LogStorageStatus status) {
//            LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;
//
//            if (status.getRecordCount() > 3) {
//                decision = LogUploadStrategyDecision.CLEANUP;
//            } else if (status.getRecordCount() >= 1) {
//                decision = LogUploadStrategyDecision.UPLOAD;
//            }
//
//            return decision;
//        }
//    }
//
//    @Test
//    public void testNewUploadStrategyWithDefaultStorageStatus() {
//        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
//        LogTransport transport = Mockito.mock(LogTransport.class);
//        DefaultLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
//
//        logCollector.setUploadStrategy(new CustomStrategy());
//
//        try {
//            Log record = new Log();
//
//            logCollector.addLogRecord(record);
//            
//            verify(transport, times(1)).sync();
//        } catch (Exception e) {
//            Assert.assertTrue("Exception: " + e.toString(), false);
//        }
//    }
//
//    private static class CustomStorageStatus implements LogStorageStatus {
//        @Override
//        public long getConsumedVolume() {
//            return 10000000;
//        }
//
//        @Override
//        public long getRecordCount() {
//            return 1;
//        }
//    }
//
//    @Test
//    public void testNewLoStorageStatus() {
//        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
//        LogTransport transport = Mockito.mock(LogTransport.class);
//        DefaultLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
//        CustomStorageStatus status = new CustomStorageStatus();
//        CustomStrategy strategy = new CustomStrategy();
//
//        logCollector.setUploadStrategy(strategy);
//
//        try {
//            Log record = new Log();
//
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//
//            verify(transport, times(1)).sync();
//        } catch (Exception e) {
//            Assert.assertTrue("Exception: " + e.toString(), false);
//        }
//    }
//
//    @Test
//    public void testNewLogStorage() {
//        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
//        LogStorage storage = Mockito.mock(LogStorage.class);
//        LogTransport transport = Mockito.mock(LogTransport.class);
//        DefaultLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
//        CustomStorageStatus status = new CustomStorageStatus();
//
//        logCollector.setStorage(storage);
//
//        try {
//            /*
//             * Size of each record is 5B.
//             */
//            Log record = new Log();
//
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//
//            /*
//             * 1MB is default maximum allowed volume.
//             * This value is hardcoded in Default Log Collector and Default Log Upload Configuration
//             */
//            verify(storage, times(2)).removeOldestRecord(1024 * 1024);
//        } catch (Exception e) {
//            Assert.assertTrue("Exception: " + e.toString(), false);
//        }
//    }
//
//    @Test
//    public void testLogUploadRequestAndSuccessResponse() {
//        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
//        LogTransport transport = Mockito.mock(LogTransport.class);
//        DefaultLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
//        DefaultLogUploadConfiguration conf =
//                new DefaultLogUploadConfiguration.Builder()
//                                            .setBatchVolume(15)
//                                            .setVolumeThreshold(25)
//                                            .setMaximumAllowedVolume(50)
//                                            .setLogUploadTimeout(300)
//                                            .build();
//
//        logCollector.setConfiguration(conf);
//
//        try {
//            Log record = new Log();;
//
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//
//            LogSyncRequest request1 = new LogSyncRequest();
//            logCollector.fillSyncRequest(request1);
//
//            Assert.assertTrue("Actual: " + request1.getLogEntries().size()
//                    , request1.getLogEntries().size() == 3);
//
//            LogSyncResponse uploadResponse = new LogSyncResponse();
//            LogDeliveryStatus status = new LogDeliveryStatus(request1.getRequestId(), SyncResponseResultType.SUCCESS, null);
//            uploadResponse.setDeliveryStatuses(Collections.singletonList(status));
//            try {
//                logCollector.onLogResponse(uploadResponse);
//            } catch (Exception e) {
//
//            }
//
//            verify(transport, times(1)).sync();
//        } catch (Exception e) {
//            Assert.assertTrue("Exception: " + e.toString(), false);
//        }
//    }
//
//    @Test
//    public void testTimeout() {
//        long timeout = 2; // in seconds
//
//        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
//        LogTransport transport = Mockito.mock(LogTransport.class);
//        DefaultLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
//        LogUploadFailoverStrategy failoverStrategy = Mockito.mock(LogUploadFailoverStrategy.class);
//        DefaultLogUploadConfiguration conf =
//                new DefaultLogUploadConfiguration.Builder()
//                                            .setBatchVolume(15)
//                                            .setVolumeThreshold(25)
//                                            .setMaximumAllowedVolume(50)
//                                            .setLogUploadTimeout(timeout)
//                                            .build();
//
//        logCollector.setConfiguration(conf);
//        logCollector.setFailoverStrategy(failoverStrategy);
//
//        try {
//            Log record = new Log();
//
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//            logCollector.addLogRecord(record);
//
//            Mockito.verify(failoverStrategy, Mockito.times(0)).onTimeout();
//
//            LogSyncRequest request1 = Mockito.mock(LogSyncRequest.class);
//            logCollector.fillSyncRequest(request1);
//
//            Thread.sleep(timeout / 2 * 1000);
//            Mockito.verify(failoverStrategy, Mockito.times(0)).onTimeout();
//            Thread.sleep(timeout / 2 * 1000);
//
//            logCollector.addLogRecord(record);
//
//            Mockito.verify(failoverStrategy, Mockito.times(1)).onTimeout();
//        } catch (Exception e) {
//            Assert.assertTrue("Exception: " + e.toString(), false);
//        }
//    }
}