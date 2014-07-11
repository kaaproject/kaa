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
package org.kaaproject.kaa.server.operations.service.statistics;

import java.util.HashMap;
import java.util.Map;

import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrey Panasenko
 *
 */
public class StatisticsFactory {
    /** The Constant logger. */
    private static final Logger LOG = LoggerFactory
            .getLogger(StatisticsService.class);

    private static Map<ChannelType, StatisticsService> services = new HashMap<>();
    private static Object sync = new Object();

    public static StatisticsService getService(ChannelType type) {
        synchronized (sync) {
            if (services.containsKey(type)) {
                return services.get(type);
            } else {
                StatisticsService service = new StatisticsService(type);
                service.start();
                services.put(type, service);
                return service;
            }
        }
    }

    public static void shutdown() {
        LOG.debug("Statistics factory received shutdown command");
        synchronized (sync) {
            for(StatisticsService service : services.values()) {
                service.shutdown();
            }
            services.clear();
        }
    }
}
