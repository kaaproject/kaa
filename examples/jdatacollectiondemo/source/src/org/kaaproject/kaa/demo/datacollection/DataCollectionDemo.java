/*
 * Copyright 2014-2015 CyberVision, Inc.
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

package org.kaaproject.kaa.demo.datacollection;

import java.io.IOException;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogFailoverCommand;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;

import static org.kaaproject.kaa.client.logging.LogUploadStrategyDecision.UPLOAD;
import static org.kaaproject.kaa.client.logging.LogUploadStrategyDecision.NOOP;

import org.kaaproject.kaa.common.endpoint.gen.LogDeliveryErrorCode;
import org.kaaproject.kaa.schema.sample.logging.Level;
import org.kaaproject.kaa.schema.sample.logging.LogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataCollectionDemo {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemo.class);

    public static void main(String[] args) {
        LOG.info("Data collection demo started");
        doWork();
        LOG.info("Data collection demo stopped");
    }

    public static void doWork() {

        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());

        kaaClient.setLogUploadStrategy(new OneLogUploadStrategy());

        kaaClient.start();

        LogData logData1 = new LogData(Level.INFO, "TAG", "MESSAGE_1");
        LogData logData2 = new LogData(Level.INFO, "TAG", "MESSAGE_2");
        LogData logData3 = new LogData(Level.INFO, "TAG", "MESSAGE_3");

        kaaClient.addLogRecord(logData1);
        kaaClient.addLogRecord(logData2);
        kaaClient.addLogRecord(logData3);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        kaaClient.stop();
    }

    private class OneLogUploadStrategy extends DefaultLogUploadStrategy {
        @Override
        public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
            return status.getRecordCount() >= 1 ? UPLOAD : NOOP;
        }
    }
}
