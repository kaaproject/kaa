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
package org.kaaproject.kaa.server.bootstrap.service.statistics;

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
            .getLogger(StatisticsFactory.class);
    
    /**
     * 
     */
    public static StatisticsService getService(ChannelType type) {
        return null;
    }
    
    public static void shutdown() {
        
    }
}
