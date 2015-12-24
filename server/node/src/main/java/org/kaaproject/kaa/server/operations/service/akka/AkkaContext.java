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
package org.kaaproject.kaa.server.operations.service.akka;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
import org.kaaproject.kaa.server.common.dao.CTLService;
import org.kaaproject.kaa.server.common.dao.PluginService;
import org.kaaproject.kaa.server.operations.service.OperationsService;
import org.kaaproject.kaa.server.operations.service.cache.CacheService;
import org.kaaproject.kaa.server.operations.service.event.EventService;
import org.kaaproject.kaa.server.operations.service.logs.LogAppenderService;
import org.kaaproject.kaa.server.operations.service.metrics.MetricsService;
import org.kaaproject.kaa.server.operations.service.notification.NotificationDeltaService;
import org.kaaproject.kaa.server.operations.service.security.KeyStoreService;
import org.kaaproject.kaa.server.operations.service.user.EndpointUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Component
public class AkkaContext {

    private static final String ENDPOINT_ACTOR_TIMEOUT = "endpoint_actor_timeout";

    private static final String ENDPOINT_EVENT_TIMEOUT = "endpoint_event_timeout";

    private static final String IO_WORKER_COUNT_PROP_NAME = "io_worker_count";

    private static final String AKKA_CONF_FILE_NAME = "akka.conf";

    /** The cache service. */
    @Autowired
    private CacheService cacheService;

    /** The cache service. */
    @Autowired
    private KeyStoreService operationsKeyStoreService;

    /** The operations service. */
    @Autowired
    private OperationsService operationsService;

    /** The notification delta service. */
    @Autowired
    private NotificationDeltaService notificationDeltaService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private LogAppenderService logAppenderService;

    @Autowired
    private EndpointUserService endpointUserService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private CTLService ctlService;

    @Value("#{properties[support_unencrypted_connection]}")
    private Boolean supportUnencryptedConnection;

    private final Config config;

    public AkkaContext() {
        config = ConfigFactory.parseResources(AKKA_CONF_FILE_NAME).withFallback(ConfigFactory.load());
    }

    public Config getConfig() {
        return config;
    }

    public int getIOWorkerCount() {
        return config.getInt(IO_WORKER_COUNT_PROP_NAME);
    }

    public long getInactivityTimeout() {
        return config.getLong(ENDPOINT_ACTOR_TIMEOUT);
    }

    public long getEventTimeout() {
        return config.getLong(ENDPOINT_EVENT_TIMEOUT);
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public KeyStoreService getKeyStoreService() {
        return operationsKeyStoreService;
    }

    public OperationsService getOperationsService() {
        return operationsService;
    }

    public NotificationDeltaService getNotificationDeltaService() {
        return notificationDeltaService;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    public EventService getEventService() {
        return eventService;
    }

    public MetricsService getMetricsService() {
        return metricsService;
    }

    public LogAppenderService getLogAppenderService() {
        return logAppenderService;
    }

    public EndpointUserService getEndpointUserService() {
        return endpointUserService;
    }

    public CTLService getCtlService() {
        return ctlService;
    }

    public PluginService getPluginService() {
        return pluginService;
    }

    public Boolean getSupportUnencryptedConnection() {
        return supportUnencryptedConnection;
    }
}
