package org.kaaproject.kaa.server.operations.service.akka;

import org.kaaproject.kaa.server.common.dao.ApplicationService;
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

    private static final String IO_WORKER_COUNT_PROP_NAME = "io_worker_count";

    private static final String AKKA_CONF_FILE_NAME = "akka.conf";

    /** The cache service. */
    @Autowired
    private CacheService cacheService;

    /** The cache service. */
    @Autowired
    private KeyStoreService keyStoreService;

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
    
    @Value("#{properties[support_unencrypted_connection]}")
    private Boolean supportUnencryptedConnection;
    
    private final Config config;
    
    public AkkaContext() {
        config = ConfigFactory.parseResources(AKKA_CONF_FILE_NAME).withFallback(ConfigFactory.load());
    }
    
    public Config getConfig(){
        return config;
    }

    public int getIOWorkerCount(){
        return config.getInt(IO_WORKER_COUNT_PROP_NAME);
    }

    public int getInactivityTimeout() {
        return config.getInt(ENDPOINT_ACTOR_TIMEOUT);
    }

    public CacheService getCacheService() {
        return cacheService;
    }

    public KeyStoreService getKeyStoreService() {
        return keyStoreService;
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

    public Boolean getSupportUnencryptedConnection() {
        return supportUnencryptedConnection;
    }
}
