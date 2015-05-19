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
import java.util.LinkedList;
import java.util.List;

import org.kaaproject.kaa.client.DesktopKaaPlatformContext;
import org.kaaproject.kaa.client.Kaa;
import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.SimpleKaaClientStateListener;
import org.kaaproject.kaa.client.logging.DefaultLogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;

import static org.kaaproject.kaa.client.logging.LogUploadStrategyDecision.UPLOAD;
import static org.kaaproject.kaa.client.logging.LogUploadStrategyDecision.NOOP;

import org.kaaproject.kaa.schema.sample.logging.Level;
import org.kaaproject.kaa.schema.sample.logging.LogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A demo application that shows how to use the Kaa logging API. 
 */
public class DataCollectionDemo {

    private static final int LOGS_TO_SEND_COUNT = 5;

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionDemo.class);

    public static void main(String[] args) {
        LOG.info("Data collection demo started");
        LOG.info("--= Press any key to exit =--");
        //Create a Kaa client with the Kaa desktop context.
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext(), new SimpleKaaClientStateListener() {
            @Override
            public void onStarted() {
                LOG.info("Kaa client started");
            }

            @Override
            public void onStopped() {
                LOG.info("Kaa client stopped");
            }
        });

        // Set a custom strategy for uploading logs.
        // The default strategy uploads logs after either a threshold logs count 
        // or a threshold logs size has been reached.
        // The following custom strategy uploads every log record as soon as it is created.
        kaaClient.setLogUploadStrategy(new DefaultLogUploadStrategy() {
            @Override
            public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
                return status.getRecordCount() >= 1 ? UPLOAD : NOOP;
            }
        });
        
        // Start the Kaa client and connect it to the Kaa server.
        kaaClient.start();

        // Send logs in a loop.
        for (LogData log : generateLogs(LOGS_TO_SEND_COUNT)) {
            kaaClient.addLogRecord(log);
            LOG.info("Log record {} sent", log.toString());
        }

        try {
         // wait for some input before exiting
            System.in.read();
        } catch (IOException e) {
            LOG.error("IOException was caught", e);
        }

        // Stop the Kaa client and release all the resources which were in use.
        kaaClient.stop();
        LOG.info("Data collection demo stopped");
    }

    public static List<LogData> generateLogs(int logCount) {
        List<LogData> logs = new LinkedList<LogData>();
        for (int i = 0; i < logCount; i++) {
            logs.add(new LogData(Level.KAA_INFO, "TAG", "MESSAGE_" + i));
        }
        return logs;
    }
}
