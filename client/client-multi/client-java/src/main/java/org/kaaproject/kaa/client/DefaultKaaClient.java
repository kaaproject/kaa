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

package org.kaaproject.kaa.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Properties;

import org.kaaproject.kaa.client.KaaClientProperties.BootstrapServerInfo;
import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.bootstrap.DefaultBootstrapManager;
import org.kaaproject.kaa.client.bootstrap.OperationsServerInfo;
import org.kaaproject.kaa.client.configuration.DefaultConfigurationProcessor;
import org.kaaproject.kaa.client.configuration.delta.manager.DefaultDeltaManager;
import org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationManager;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManager;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationPersistenceManager;
import org.kaaproject.kaa.client.configuration.storage.DefaultConfigurationPersistenceManager;
import org.kaaproject.kaa.client.notification.DefaultNotificationManager;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.persistance.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistance.KaaClientState;
import org.kaaproject.kaa.client.profile.DefaultProfileManager;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.client.schema.DefaultSchemaProcessor;
import org.kaaproject.kaa.client.schema.storage.DefaultSchemaPersistenceManager;
import org.kaaproject.kaa.client.schema.storage.SchemaPersistenceManager;
import org.kaaproject.kaa.client.transport.HttpBootstrapTransport;
import org.kaaproject.kaa.client.transport.HttpOperationsTransport;
import org.kaaproject.kaa.client.update.listeners.ConfigurationUpdateListener;
import org.kaaproject.kaa.client.update.listeners.RedirectionUpdateListener;
import org.kaaproject.kaa.client.update.DefaultUpdateManager;
import org.kaaproject.kaa.client.update.listeners.NotificationUpdateListener;
import org.kaaproject.kaa.client.update.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link KaaClient} implementation
 *
 * @author Yaroslav Zeygerman
 *
 */
public class DefaultKaaClient implements KaaClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultKaaClient.class);
    private final DefaultConfigurationProcessor configurationProcessor = new DefaultConfigurationProcessor();
    private boolean isInitialized = false;

    private final DefaultSchemaProcessor schemaProcessor = new DefaultSchemaProcessor();
    private final DefaultConfigurationPersistenceManager configurationPersistenceManager = new DefaultConfigurationPersistenceManager(
            configurationProcessor);
    private final DefaultSchemaPersistenceManager schemaPersistenceManager = new DefaultSchemaPersistenceManager(
            schemaProcessor);
    private final DefaultConfigurationManager configurationManager = new DefaultConfigurationManager();
    private final DefaultDeltaManager deltaManager = new DefaultDeltaManager();

    private final DefaultNotificationManager notificationManager;
    private final DefaultProfileManager profileManager;

    private final KaaClientProperties properties;
    private List<BootstrapServerInfo> bootstrapServers;
    private final KaaClientState kaaClientState;
    private final BootstrapManager bootstrapManager;
    private final UpdateManager updateManager;

    DefaultKaaClient(Properties properties){
        this.properties = new KaaClientProperties(properties);
        this.kaaClientState = new KaaClientPropertiesState(this.properties);
        this.updateManager = new DefaultUpdateManager(this.properties, this.kaaClientState);
        this.profileManager = new DefaultProfileManager(this.updateManager, this.kaaClientState);
        this.updateManager.setSerializedProfileContainer(this.profileManager
                .getSerializedProfileContainer());
        this.bootstrapManager = new DefaultBootstrapManager(this.properties.getApplicationToken());
        this.notificationManager = new DefaultNotificationManager(this.updateManager, this.kaaClientState);
    }

    private void initKaaConfiguration() {
        schemaProcessor.subscribeForSchemaUpdates(configurationProcessor);
        schemaProcessor.subscribeForSchemaUpdates(schemaPersistenceManager);
        schemaProcessor.subscribeForSchemaUpdates(configurationPersistenceManager);

        configurationProcessor.subscribeForUpdates(configurationManager);
        configurationProcessor.subscribeForUpdates(deltaManager);
        configurationProcessor.addOnProcessedCallback(configurationManager);

        configurationManager.subscribeForConfigurationUpdates(configurationPersistenceManager);

        this.updateManager.addUpdateListener(new ConfigurationUpdateListener(
                configurationProcessor, schemaProcessor,
                configurationPersistenceManager, kaaClientState));
    }

    private void setDefaultConfiguration() throws IOException {
        byte [] schema = properties.getDefaultConfigSchema();
        if (schema != null && schema.length > 0) {
            schemaProcessor.loadSchema(ByteBuffer.wrap(schema));
            byte [] config = properties.getDefaultConfigData();
            if (config != null && config.length > 0) {
                configurationProcessor.processConfigurationData(ByteBuffer.wrap(config), true);
            }
        }
    }

    void init() throws Exception {
        if (isInitialized) {
            return;
        }
        this.bootstrapServers = properties.getBootstrapServers();
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            throw new RuntimeException("Unable to obtain list of bootstrap servers.");
        }
        BootstrapServerInfo bootstrapServer = bootstrapServers.get(0);
        this.bootstrapManager.setTransport(initBootstrapTransport(bootstrapServer));
        this.bootstrapManager.receiveOperationsServerList();
        OperationsServerInfo operationsServer = this.bootstrapManager.getNextOperationsServer();

        LOG.info("Starting with endpoint server {}", operationsServer.getHostName());

        this.updateManager.setTransport(initOperationsTransport(operationsServer));

        this.updateManager.setTransportExceptionHandler(new DefaultTransportExceptionHandler(
                    bootstrapManager, updateManager, kaaClientState));
        this.updateManager.addUpdateListener(new NotificationUpdateListener(notificationManager));
        this.updateManager.addUpdateListener(new RedirectionUpdateListener(bootstrapManager, updateManager, kaaClientState));
        initKaaConfiguration();
        isInitialized = true;
    }

    protected HttpOperationsTransport initOperationsTransport(OperationsServerInfo operationsServer) {
        return new HttpOperationsTransport(
                operationsServer.getHostName(),
                this.kaaClientState.getPrivateKey(),
                this.kaaClientState.getPublicKey(),
                operationsServer.getKey());
    }

    protected HttpBootstrapTransport initBootstrapTransport(BootstrapServerInfo bootstrapServer) {
        return new HttpBootstrapTransport(bootstrapServer.getURL(), bootstrapServer.getPublicKey());
    }

    void start() throws IOException {
        if (!isInitialized) {
            // TODO: throw exception instead
            return;
        }
        if (schemaProcessor.getSchema() == null
                || configurationPersistenceManager.getConfigurationHash() == null) {
            kaaClientState.setAppStateSeqNumber(0);
            setDefaultConfiguration();
        }
        updateManager.start();
    }

    void stop() {
        updateManager.stop();
        this.kaaClientState.persist();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public ConfigurationManager getConfiguationManager() {
        return configurationManager;
    }

    @Override
    public DeltaManager getDeltaManager() {
        return deltaManager;
    }

    @Override
    public ConfigurationPersistenceManager getConfigurationPersistenceManager() {
        return configurationPersistenceManager;
    }

    @Override
    public SchemaPersistenceManager getSchemaPersistenceManager() {
        return schemaPersistenceManager;
    }

    @Override
    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

}
