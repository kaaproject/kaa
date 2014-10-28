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

/**
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
package org.kaaproject.kaa.examples.robotrun.controller.log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kaaproject.kaa.client.KaaClient;
import org.kaaproject.kaa.client.logging.LogStorageStatus;
import org.kaaproject.kaa.client.logging.LogUploadConfiguration;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.logging.LogUploadStrategyDecision;
import org.kaaproject.kaa.examples.robotrun.gen.Border;
import org.kaaproject.kaa.examples.robotrun.gen.BorderType;
import org.kaaproject.kaa.examples.robotrun.gen.Borders;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderInfo;
import org.kaaproject.kaa.examples.robotrun.labyrinth.BorderUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLogManager implements LogManager {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultLogManager.class);

    private final KaaClient kaaClient;

    private class ReportUploadStrategy implements LogUploadStrategy {
        @Override
        public LogUploadStrategyDecision isUploadNeeded(
                LogUploadConfiguration configuration, LogStorageStatus status)
        {
            /**
             * Every new report should be sent immediately.
             */
            return (status.getRecordCount() > 0 ?
                    LogUploadStrategyDecision.UPLOAD : LogUploadStrategyDecision.NOOP);
        }
    }

    public DefaultLogManager(KaaClient client) {
        if (client == null) {
            LOG.error("Failed to init log manager: null Kaa client");
            throw new IllegalArgumentException("Null Kaa client");
        }

        kaaClient = client;
        kaaClient.getLogCollector().setUploadStrategy(new ReportUploadStrategy());
    }

    @Override
    public void reportBorders(BorderUpdate borderUpdate) {
        try {
            kaaClient.getLogCollector().addLogRecord(toBorders(borderUpdate));
        } catch (IOException e) {
            LOG.warn("Failed to add border report: {}", e.toString());
        }
    }

    public Borders toBorders(BorderUpdate borderUpdate){
        Borders borders = new Borders();

        borders.setHBorders(toBorderList(borderUpdate.getHBorders()));
        borders.setVBorders(toBorderList(borderUpdate.getVBorders()));

        return borders;
    }

    private List<Border> toBorderList( List<BorderInfo> bordersSrc) {
        List<Border> borders = new ArrayList<>(bordersSrc.size());
        for(BorderInfo borderInfo : bordersSrc){
            Border border = new Border(borderInfo.getX(), borderInfo.getY(), BorderType.valueOf(borderInfo.getType().name()));
            borders.add(border);
        }
        return borders;
    }
}
