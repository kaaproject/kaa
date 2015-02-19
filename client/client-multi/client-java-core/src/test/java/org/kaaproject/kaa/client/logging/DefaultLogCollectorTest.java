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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryStatus;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncRequest;
import org.kaaproject.kaa.common.endpoint.gen.LogSyncResponse;
import org.kaaproject.kaa.common.endpoint.gen.SyncResponseResultType;
import org.kaaproject.kaa.schema.base.Log;
import org.mockito.Mockito;

public class DefaultLogCollectorTest {

    @Test
    public void testDefaultUploadConfiguration() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        LogTransport transport = Mockito.mock(LogTransport.class);
        AbstractLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
        DefaultLogUploadStrategy strategy = new DefaultLogUploadStrategy();
        strategy.setCountThreshold(5);
        logCollector.setStrategy(strategy);

        try {
            Log record = new Log();

            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            
            verify(transport, times(0)).sync();
            
            logCollector.addLogRecord(record);

            verify(transport, times(1)).sync();
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }

    @Test
    public void testStorageStatusAffect() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        LogTransport transport = Mockito.mock(LogTransport.class);
        AbstractLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
        LogStorage storage = Mockito.mock(LogStorage.class);
        logCollector.setStorage(storage);
        try {
            Log record = new Log();

            Mockito.when(storage.getStatus()).thenReturn(new LogStorageStatus() {
                
                @Override
                public long getRecordCount() {
                    return 1;
                }
                
                @Override
                public long getConsumedVolume() {
                    return 1;
                }
            });
            
            logCollector.addLogRecord(record);
            
            verify(transport, times(0)).sync();
            
            Mockito.when(storage.getStatus()).thenReturn(new LogStorageStatus() {
                
                @Override
                public long getRecordCount() {
                    return 1;
                }
                
                @Override
                public long getConsumedVolume() {
                    return 1024*1024;
                }
            });
            
            logCollector.addLogRecord(record);

            verify(transport, times(1)).sync();
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }

    @Test
    public void testLogUploadRequestAndSuccessResponse() {
        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        LogTransport transport = Mockito.mock(LogTransport.class);
        AbstractLogCollector logCollector = new DefaultLogCollector(transport, channelManager);
        DefaultLogUploadStrategy strategy = new DefaultLogUploadStrategy();
        logCollector.setStrategy(strategy);
        LogStorage storage = Mockito.mock(LogStorage.class);
        logCollector.setStorage(storage);
        
        try {
            Log record = new Log();
            Mockito.when(storage.getStatus()).thenReturn(new LogStorageStatus() {
                @Override
                public long getRecordCount() {
                    return 1;
                }
                
                @Override
                public long getConsumedVolume() {
                    return 1;
                }
            });
            
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            Mockito.when(storage.getStatus()).thenReturn(new LogStorageStatus() {
                
                @Override
                public long getRecordCount() {
                    return 1;
                }
                
                @Override
                public long getConsumedVolume() {
                    return 1024*1024;
                }
            });
            logCollector.addLogRecord(record);
            
            Mockito.when(storage.getRecordBlock(Mockito.anyLong())).thenReturn(new LogBlock(1, Arrays.asList(new LogRecord(record), new LogRecord(record), new LogRecord(record))));

            LogSyncRequest request1 = new LogSyncRequest();
            logCollector.fillSyncRequest(request1);

            Assert.assertEquals(3, request1.getLogEntries().size());

            LogSyncResponse uploadResponse = new LogSyncResponse();
            LogDeliveryStatus status = new LogDeliveryStatus(request1.getRequestId(), SyncResponseResultType.SUCCESS, null);
            uploadResponse.setDeliveryStatuses(Collections.singletonList(status));
            logCollector.onLogResponse(uploadResponse);
            verify(transport, times(2)).sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTimeout() {
        long timeout = 2; // in seconds

        KaaChannelManager channelManager = Mockito.mock(KaaChannelManager.class);
        LogTransport transport = Mockito.mock(LogTransport.class);
        AbstractLogCollector logCollector = new DefaultLogCollector(transport, channelManager);

        DefaultLogUploadStrategy tmp = new DefaultLogUploadStrategy();
        tmp.setTimeout(1);
        LogUploadStrategy strategy = Mockito.spy(tmp);
        logCollector.setStrategy(strategy);

        try {
            Log record = new Log();

            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);
            logCollector.addLogRecord(record);

            Mockito.verify(strategy, Mockito.times(0)).onTimeout(Mockito.any(LogFailoverCommand.class));

            LogSyncRequest request1 = Mockito.mock(LogSyncRequest.class);
            logCollector.fillSyncRequest(request1);

            Thread.sleep(timeout / 2 * 1000);
            Mockito.verify(strategy, Mockito.times(0)).onTimeout(Mockito.any(LogFailoverCommand.class));
            Thread.sleep(timeout / 2 * 1000);

            logCollector.addLogRecord(record);

            Mockito.verify(strategy, Mockito.times(1)).onTimeout(Mockito.any(LogFailoverCommand.class));
        } catch (Exception e) {
            Assert.assertTrue("Exception: " + e.toString(), false);
        }
    }
}