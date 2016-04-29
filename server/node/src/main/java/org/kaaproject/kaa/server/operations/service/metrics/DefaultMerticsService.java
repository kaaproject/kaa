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

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;

@Service
public class DefaultMerticsService implements MetricsService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMerticsService.class);

    private static final String METRICS_COLLECTION_IS_DISABLED = "Metrics collection is disabled!";
    private static final String KAA_METRICS_LOGGER_NAME = "org.kaaproject.kaa.metrics";

    private final MetricRegistry metrics = new MetricRegistry();
    private volatile boolean enabled;
    private Slf4jReporter reporter;

    private JmxReporter jmx;

    @Autowired
    private SystemMonitoringInfo monitor;

    @Override
    public synchronized MeterClient createMeter(String name, String... names) {
        final Meter meter = metrics.meter(MetricRegistry.name(name, names));
        return new MeterClient() {
            @Override
            public void mark() {
                if (enabled) {
                    meter.mark();
                } else {
                    LOG.trace(METRICS_COLLECTION_IS_DISABLED);
                }
            }
        };
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void startReport() {
        LOG.info("Starting metrics report!");
        reporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger(KAA_METRICS_LOGGER_NAME))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS).build();
        registerSystemMonitor();
        reporter.start(30, TimeUnit.SECONDS);

        this.jmx = JmxReporter.forRegistry(this.metrics).inDomain(KAA_METRICS_LOGGER_NAME).build();
        this.jmx.start();
    }

    @Override
    public void stopReport() {
        LOG.info("Stoping metrics report!");
        reporter.stop();

        this.jmx.stop();
    }

    private void registerSystemMonitor() {
        LOG.info("Registering load average usage metrics.");
        metrics.register(MetricRegistry.name(SystemMonitoringInfo.class, "system-load-average"), new Gauge<Double>() {
            @Override
            public Double getValue() {
                return monitor.getLoadAverage();
            }
        });

        LOG.info("Registering heap memory usage metrics.");
        metrics.register(MetricRegistry.name(SystemMonitoringInfo.class, "heap-memory-usage.mb"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return monitor.getHeapMemoryUsage();
            }
        });

        LOG.info("Registering non heap memory usage metrics.");
        metrics.register(MetricRegistry.name(SystemMonitoringInfo.class, "non-heap-memory-usage.mb"), new Gauge<Long>() {
            @Override
            public Long getValue() {
                return monitor.getNonHeapMemoryUsage();
            }
        });

        LOG.info("Registering live thread count metrics.");
        metrics.register(MetricRegistry.name(SystemMonitoringInfo.class, "thread-count"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return monitor.getLiveThreadCount();
            }
        });
    }
}
