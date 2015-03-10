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
 This class demonstrates Kaa log upload system. */
public class DataCollector {

    private static final int LOGS_TO_SEND_COUNT = 5;

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DataCollector.class);

    public static void main(String[] args) {
        LOGGER.info("Data collection demo started");
        //Creating Kaa desktop client instance
        KaaClient kaaClient = Kaa.newClient(new DesktopKaaPlatformContext());
        //setting custom upload strategy
        kaaClient.setLogUploadStrategy(new OneLogUploadStrategy());
        //starting Kaa client
        kaaClient.start();

        //sending logs in loop
        for (LogData log : getLogs(LOGS_TO_SEND_COUNT)) {
            kaaClient.addLogRecord(log);
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //stoping client
        kaaClient.stop();
        LOGGER.info("Data collection demo stopped");
    }

    public static List<LogData> getLogs(int logCount) {
        List<LogData> logs = new LinkedList<LogData>();
        for (int i = 0; i < logCount; i++) {
            logs.add(new LogData(Level.INFO, "TAG", "MESSAGE_" + i));
        }
        return logs;
    }

    //default upload strategy sends logs after some count or some logs size reached
    //this one sends every log record
    private static class OneLogUploadStrategy extends DefaultLogUploadStrategy {
        @Override
        public LogUploadStrategyDecision isUploadNeeded(LogStorageStatus status) {
            return status.getRecordCount() >= 1 ? UPLOAD : NOOP;
        }
    }
}
