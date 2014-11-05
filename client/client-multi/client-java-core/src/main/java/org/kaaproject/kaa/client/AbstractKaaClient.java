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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.bootstrap.DefaultBootstrapManager;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.ConfigurationTransport;
import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataDemultiplexer;
import org.kaaproject.kaa.client.channel.KaaDataMultiplexer;
import org.kaaproject.kaa.client.channel.KaaTransport;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.channel.MetaDataTransport;
import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.RedirectionTransport;
import org.kaaproject.kaa.client.channel.ServerInfo;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.channel.connectivity.ConnectivityChecker;
import org.kaaproject.kaa.client.channel.impl.DefaultBootstrapDataProcessor;
import org.kaaproject.kaa.client.channel.impl.DefaultChannelManager;
import org.kaaproject.kaa.client.channel.impl.DefaultOperationDataProcessor;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultBootstrapChannel;
import org.kaaproject.kaa.client.channel.impl.channels.DefaultOperationTcpChannel;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultBootstrapTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultConfigurationTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultEventTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultLogTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultMetaDataTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultNotificationTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultProfileTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultRedirectionTransport;
import org.kaaproject.kaa.client.channel.impl.transports.DefaultUserTransport;
import org.kaaproject.kaa.client.configuration.DefaultConfigurationProcessor;
import org.kaaproject.kaa.client.configuration.delta.manager.DefaultDeltaManager;
import org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationManager;
import org.kaaproject.kaa.client.configuration.manager.DefaultConfigurationManager;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationPersistenceManager;
import org.kaaproject.kaa.client.configuration.storage.DefaultConfigurationPersistenceManager;
import org.kaaproject.kaa.client.event.DefaultEventManager;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.client.event.registration.DefaultEndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.logging.DefaultLogCollector;
import org.kaaproject.kaa.client.logging.LogCollector;
import org.kaaproject.kaa.client.notification.DefaultNotificationManager;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.profile.DefaultProfileManager;
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.client.schema.DefaultSchemaProcessor;
import org.kaaproject.kaa.client.schema.storage.DefaultSchemaPersistenceManager;
import org.kaaproject.kaa.client.schema.storage.SchemaPersistenceManager;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.bootstrap.gen.ChannelType;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract class that holds general elements of Kaa library.</p>
 *
 * <p>This class creates and binds Kaa library modules. Public access to each
 * module is performed using {@link KaaClient} interface.</p>
 *
 * <p>Class contains abstract methods
 * {@link AbstractKaaClient#createHttpClient(String, PrivateKey, PublicKey, PublicKey)}
 * and {@link AbstractKaaClient#createPersistentStorage()} which are used to
 * reference the platform-specific implementation of http client and Kaa's state
 * persistent storage.</p>
 *
 * <p>Http client ({@link AbstractHttpClient}) is used to provide basic
 * communication with Bootstrap and Operation servers using HTTP protocol.</p>
 *
 * @author Yaroslav Zeygerman
 *
 * @see KaaClient
 * @see AbstractHttpClient
 * @see PersistentStorage
 */
public abstract class AbstractKaaClient implements KaaClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaClient.class);
    private static final long LONG_POLL_TIMEOUT = 60000L;

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
    private final KaaClientState kaaClientState;
    private final BootstrapManager bootstrapManager;
    private final EventManager eventManager;
    private final EventFamilyFactory eventFamilyFactory;

    private final DefaultEndpointRegistrationManager endpointRegistrationManager;
    private final DefaultLogCollector logCollector;

    private final Map<TransportType, KaaTransport> transports = new HashMap<TransportType, KaaTransport>();
    private final DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor();
    private final DefaultBootstrapDataProcessor bootstrapDataProcessor = new DefaultBootstrapDataProcessor();
    private final MetaDataTransport metaDataTransport = new DefaultMetaDataTransport();
    private final KaaChannelManager channelManager;

    private final EndpointObjectHash publicKeyHash;

    private final DefaultBootstrapChannel defaultBootstrapChannel;
    private final DefaultOperationTcpChannel defaultOperationTcpChannel;

    AbstractKaaClient() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        this(new KaaClientProperties());
    }

    AbstractKaaClient(KaaClientProperties properties) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        this.properties = properties;

        Map<ChannelType, List<ServerInfo>> bootstrapServers = properties.getBootstrapServers();
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            throw new RuntimeException("Unable to obtain list of bootstrap servers."); //NOSONAR
        }

        for (Map.Entry<ChannelType, List<ServerInfo>> cursor : bootstrapServers.entrySet()) {
            Collections.shuffle(cursor.getValue());
        }

        kaaClientState = new KaaClientPropertiesState(createPersistentStorage(), properties);

        BootstrapTransport bootstratpTransport = new DefaultBootstrapTransport(properties.getApplicationToken());
        ProfileTransport profileTransport = new DefaultProfileTransport();
        EventTransport eventTransport = new DefaultEventTransport();
        NotificationTransport notificationTransport = new DefaultNotificationTransport();
        ConfigurationTransport configurationTransport = new DefaultConfigurationTransport();
        UserTransport userTransport = new DefaultUserTransport();
        RedirectionTransport redirectionTransport = new DefaultRedirectionTransport();
        LogTransport logTransport = new DefaultLogTransport();

        operationsDataProcessor.setConfigurationTransport(configurationTransport);
        operationsDataProcessor.setEventTransport(eventTransport);
        operationsDataProcessor.setMetaDataTransport(metaDataTransport);
        operationsDataProcessor.setNotificationTransport(notificationTransport);
        operationsDataProcessor.setProfileTransport(profileTransport);
        operationsDataProcessor.setRedirectionTransport(redirectionTransport);
        operationsDataProcessor.setUserTransport(userTransport);
        operationsDataProcessor.setLogTransport(logTransport);

        bootstrapDataProcessor.setBootstrapTransport(bootstratpTransport);


        defaultBootstrapChannel = new DefaultBootstrapChannel(this, kaaClientState);

        defaultBootstrapChannel.setMultiplexer(bootstrapDataProcessor);
        defaultBootstrapChannel.setDemultiplexer(bootstrapDataProcessor);

        profileManager = new DefaultProfileManager(profileTransport);
        bootstrapManager = new DefaultBootstrapManager(bootstratpTransport);
        notificationManager = new DefaultNotificationManager(this.kaaClientState, notificationTransport);
        eventManager = new DefaultEventManager(this.kaaClientState, eventTransport);
        eventFamilyFactory = new EventFamilyFactory(this.eventManager);
        endpointRegistrationManager = new DefaultEndpointRegistrationManager(this.kaaClientState, userTransport, profileTransport);
        logCollector = new DefaultLogCollector(logTransport);

        channelManager = new DefaultChannelManager(bootstrapManager, bootstrapServers);
        defaultOperationTcpChannel = new DefaultOperationTcpChannel(kaaClientState, channelManager);
        defaultOperationTcpChannel.setMultiplexer(operationsDataProcessor);
        defaultOperationTcpChannel.setDemultiplexer(operationsDataProcessor);

        channelManager.addChannel(defaultBootstrapChannel);
        channelManager.addChannel(defaultOperationTcpChannel);
        bootstrapManager.setChannelManager(channelManager);

        publicKeyHash = EndpointObjectHash.fromSHA1(kaaClientState.getPublicKey().getEncoded());
        metaDataTransport.setClientProperties(properties);
        metaDataTransport.setClientState(kaaClientState);
        metaDataTransport.setEndpointPublicKeyhash(publicKeyHash);
        metaDataTransport.setTimeout(LONG_POLL_TIMEOUT);
        bootstratpTransport.setBootstrapManager(bootstrapManager);
        transports.put(TransportType.BOOTSTRAP, bootstratpTransport);
        profileTransport.setProfileManager(profileManager);
        profileTransport.setClientProperties(properties);
        transports.put(TransportType.PROFILE, profileTransport);
        eventTransport.setEventManager(eventManager);
        transports.put(TransportType.EVENT, eventTransport);
        notificationTransport.setNotificationProcessor(notificationManager);
        transports.put(TransportType.NOTIFICATION, notificationTransport);
        configurationTransport.setConfigurationHashContainer(configurationPersistenceManager);
        configurationTransport.setConfigurationProcessor(configurationProcessor);
        configurationTransport.setSchemaProcessor(schemaProcessor);
        transports.put(TransportType.CONFIGURATION, configurationTransport);
        userTransport.setEndpointRegistrationProcessor(endpointRegistrationManager);
        transports.put(TransportType.USER, userTransport);
        redirectionTransport.setBootstrapManager(bootstrapManager);
        transports.put(TransportType.LOGGING, logTransport);
        logTransport.setLogProcessor(logCollector);

        for (KaaTransport transport : transports.values()) {
            transport.setChannelManager(channelManager);
            transport.setClientState(kaaClientState);
        }

        channelManager.setConnectivityChecker(createConnectivityChecker());
    }

    private void initKaaConfiguration() {
        schemaProcessor.subscribeForSchemaUpdates(configurationProcessor);
        schemaProcessor.subscribeForSchemaUpdates(schemaPersistenceManager);
        schemaProcessor.subscribeForSchemaUpdates(configurationPersistenceManager);

        configurationProcessor.subscribeForUpdates(configurationManager);
        configurationProcessor.subscribeForUpdates(deltaManager);
        configurationProcessor.addOnProcessedCallback(configurationManager);

        configurationManager.subscribeForConfigurationUpdates(configurationPersistenceManager);

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

    void init() throws Exception { //NOSONAR
        if (isInitialized) {
            return;
        }

        initKaaConfiguration();
        isInitialized = true;
    }

    public abstract AbstractHttpClient createHttpClient(String url, PrivateKey privateKey,
            PublicKey publicKey, PublicKey remotePublicKey);

    protected abstract PersistentStorage createPersistentStorage();

    protected abstract ConnectivityChecker createConnectivityChecker();

    void start() throws IOException, TransportException {
        if (!isInitialized) {
            LOG.warn("Client is not initialized!");
            // TODO: throw exception instead
            return;
        }
        if (schemaProcessor.getSchema() == null
                || configurationPersistenceManager.getConfigurationHash() == null) {
            LOG.debug("Initializing client state with default configuration");
            kaaClientState.setAppStateSeqNumber(0);
            setDefaultConfiguration();
        }
        bootstrapManager.receiveOperationsServerList();
    }

    void stop() {
        kaaClientState.persist();
        defaultBootstrapChannel.shutdown();
        defaultOperationTcpChannel.shutdown();
    }

    void pause() {
        kaaClientState.persist();
        defaultBootstrapChannel.pause();
    }

    void resume() {
        defaultBootstrapChannel.resume();
    }

    @Override
    public ProfileManager getProfileManager() {
        return profileManager;
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
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

    @Override
    public EndpointRegistrationManager getEndpointRegistrationManager() {
        return endpointRegistrationManager;
    }

    @Override
    public EventFamilyFactory getEventFamilyFactory() {
        return eventFamilyFactory;
    }

    @Override
    public EventListenersResolver getEventListenerResolver() {
        return eventManager;
    }

    @Override
    public KaaChannelManager getChannelMananager() {
        return channelManager;
    }

    @Override
    public KaaDataMultiplexer getOperationMultiplexer() {
        return operationsDataProcessor;
    }

    @Override
    public KaaDataDemultiplexer getOperationDemultiplexer() {
        return operationsDataProcessor;
    }

    @Override
    public KaaDataMultiplexer getBootstrapMultiplexer() {
        return bootstrapDataProcessor;
    }

    @Override
    public KaaDataDemultiplexer getBootstrapDemultiplexer() {
        return bootstrapDataProcessor;
    }

    @Override
    public PublicKey getClientPublicKey() {
        return kaaClientState.getPublicKey();
    }

    @Override
    public String getEndpointKeyHash() {
        return kaaClientState.getEndpointKeyHash().getKeyHash();
    }

    @Override
    public PrivateKey getClientPrivateKey() {
        return kaaClientState.getPrivateKey();
    }

    @Override
    public LogCollector getLogCollector() {
        return logCollector;
    }
}
