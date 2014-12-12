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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.logging.gen.SuperRecord;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLogCollectorTest {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogCollectorTest.class);

    @Test
    public void testNewLogUploadConfiguration() {
        DefaultLogUploadConfiguration conf = new DefaultLogUploadConfiguration(3, 4, 50);
        LogTransport transport = mock(LogTransport.class);
        DefaultLogCollector logCollector = new DefaultLogCollector(transport);
        logCollector.setConfiguration(conf);

        try {
            SuperRecord record = new SuperRecord();
            record.setLogdata("test");

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

    private static class CustomStrategy implements LogUploadStrategy {
        @Override
        public LogUploadStrategyDecision isUploadNeeded(LogUploadConfiguration configuration, LogStorageStatus status) {
            LogUploadStrategyDecision decision = LogUploadStrategyDecision.NOOP;

            if (status.getRecordCount() > 3) {
                decision = LogUploadStrategyDecision.CLEANUP;
            } else if (status.getRecordCount() >= 1) {
                decision = LogUploadStrategyDecision.UPLOAD;
            }

            return decision;
        }
    }

    @Test
    public void testNewUploadStrategyWithDefaultStorageStatus() {
        LogTransport transport = mock(LogTransport.class);
        DefaultLogCollector logCollector = new DefaultLogCollector(transport);

        logCollector.setUploadStrategy(new CustomStrategy());

        try {
            SuperRecord record = new SuperRecord();

            record.setLogdata("test");
            logCollector.addLogRecord(record);

            verify(transport, times(1)).sync();
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }

    private static class CustomStorageStatus implements LogStorageStatus {
        @Override
        public long getConsumedVolume() {
            return 10000000;
        }

        @Override
        public long getRecordCount() {
            return 1;
        }
    }

    @Test
    public void testNewLoStorageStatus() {
        LogTransport transport = mock(LogTransport.class);
        DefaultLogCollector logCollector = new DefaultLogCollector(transport);
        CustomStorageStatus status = new CustomStorageStatus();
        CustomStrategy strategy = new CustomStrategy();

        logCollector.setStorageStatus(status);
        logCollector.setUploadStrategy(strategy);

        try {
            SuperRecord record = new SuperRecord();

            record.setLogdata("test");

            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);

            verify(transport, times(1)).sync();
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }

    @Test
    public void testNewLogStorage() {
        LogStorage storage = mock(LogStorage.class);
        LogTransport transport = mock(LogTransport.class);
        DefaultLogCollector logCollector = new DefaultLogCollector(transport);
        CustomStorageStatus status = new CustomStorageStatus();

        logCollector.setStorage(storage);
        logCollector.setStorageStatus(status);

        try {
            /*
             * Size of each record is 5B.
             */
            SuperRecord record = new SuperRecord();

            record.setLogdata("test");

            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);

            /*
             * 1MB is default maximum allowed volume.
             * This value is hardcoded in Default Log Collector and Default Log Upload Configuration
             */
            verify(storage, times(2)).removeOldestRecord(1024 * 1024);
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }

    @Test
    public void testLogUploadRequestAndSuccessResponse() {
        LogTransport transport = mock(LogTransport.class);
        DefaultLogCollector logCollector = new DefaultLogCollector(transport);
        DefaultLogUploadConfiguration conf = new DefaultLogUploadConfiguration(15, 25, 50);

        logCollector.setConfiguration(conf);

        try {
            SuperRecord record = new SuperRecord();

            record.setLogdata("test");

            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);

            LogSyncRequest request1 = new LogSyncRequest();
            logCollector.fillSyncRequest(request1);

            Assert.assertTrue("Actual: " + request1.getLogEntries().size()
                    , request1.getLogEntries().size() == 3);

            LogSyncResponse uploadResponse = new LogSyncResponse();
            LogDeliveryStatus status = new LogDeliveryStatus(request1.getRequestId(), SyncResponseResultType.SUCCESS, null);
            uploadResponse.setDeliveryStatuses(Collections.singletonList(status));
            try {
                logCollector.onLogResponse(uploadResponse);
            } catch (Exception e) {

            }

            verify(transport, times(1)).sync();
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }
}