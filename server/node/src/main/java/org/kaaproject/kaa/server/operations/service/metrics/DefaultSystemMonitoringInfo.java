/*
 * Copyright 2014-2016 CyberVision, Inc.
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

package org.kaaproject.kaa.server.operations.service.metrics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import org.springframework.stereotype.Component;

@Component
public class DefaultSystemMonitoringInfo implements SystemMonitoringInfo {

    private static final int MB = 1024 * 1024;

    @Override
    public double getLoadAverage() {
        return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
    }

    @Override
    public long getHeapMemoryUsage() {
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return heapMemoryUsage.getUsed() / MB;

    }

    @Override
    public long getNonHeapMemoryUsage() {
        MemoryUsage nonHeapMemoryUsage = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
        return nonHeapMemoryUsage.getUsed() / MB;
    }

    @Override
    public int getLiveThreadCount() {
        return ManagementFactory.getThreadMXBean().getThreadCount();
    }
}
