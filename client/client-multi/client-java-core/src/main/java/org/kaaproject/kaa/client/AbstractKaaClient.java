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
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
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
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaInternalChannelManager;
import org.kaaproject.kaa.client.channel.KaaTransport;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.channel.MetaDataTransport;
import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.RedirectionTransport;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.UserTransport;
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
import org.kaaproject.kaa.client.configuration.base.ConfigurationListener;
import org.kaaproject.kaa.client.configuration.base.ConfigurationManager;
import org.kaaproject.kaa.client.configuration.base.ResyncConfigurationManager;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationStorage;
import org.kaaproject.kaa.client.event.DefaultEventManager;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.event.registration.DefaultEndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.exceptions.KaaClusterConnectionException;
import org.kaaproject.kaa.client.exceptions.KaaException;
import org.kaaproject.kaa.client.exceptions.KaaRuntimeException;
import org.kaaproject.kaa.client.logging.AbstractLogCollector;
import org.kaaproject.kaa.client.logging.DefaultLogCollector;
import org.kaaproject.kaa.client.logging.LogStorage;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.notification.DefaultNotificationManager;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.client.persistence.KaaClientPropertiesState;
import org.kaaproject.kaa.client.persistence.KaaClientState;
import org.kaaproject.kaa.client.persistence.PersistentStorage;
import org.kaaproject.kaa.client.profile.DefaultProfileManager;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.TransportType;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Abstract class that holds general elements of Kaa library.
 * </p>
 *
 * <p>
 * This class creates and binds Kaa library modules. Public access to each
 * module is performed using {@link KaaClient} interface.
 * </p>
 *
 * <p>
 * Class contains abstract methods
 * {@link AbstractKaaClient#createHttpClient(String, PrivateKey, PublicKey, PublicKey)}
 * and {@link AbstractKaaClient#createPersistentStorage()} which are used to
 * reference the platform-specific implementation of http client and Kaa's state
 * persistent storage.
 * </p>
 *
 * <p>
 * Http client ({@link AbstractHttpClient}) is used to provide basic
 * communication with Bootstrap and Operation servers using HTTP protocol.
 * </p>
 *
 * @author Yaroslav Zeygerman
 *
 * @see KaaClient
 * @see AbstractHttpClient
 * @see PersistentStorage
 */
public abstract class AbstractKaaClient implements GenericKaaClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaClient.class);
    private static final long LONG_POLL_TIMEOUT = 60000L;

    private boolean isInitialized = false;

    protected final ConfigurationManager configurationManager;

    private final DefaultNotificationManager notificationManager;
    private final DefaultProfileManager profileManager;

    private final KaaClientProperties properties;
    private final KaaClientState kaaClientState;
    private final BootstrapManager bootstrapManager;
    private final EventManager eventManager;
    private final EventFamilyFactory eventFamilyFactory;

    private final DefaultEndpointRegistrationManager endpointRegistrationManager;
    protected final AbstractLogCollector logCollector;

    private final Map<TransportType, KaaTransport> transports = new HashMap<TransportType, KaaTransport>();
    private final DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor();
    private final DefaultBootstrapDataProcessor bootstrapDataProcessor = new DefaultBootstrapDataProcessor();
    private final MetaDataTransport metaDataTransport = new DefaultMetaDataTransport();
    private final KaaInternalChannelManager channelManager;

    private final EndpointObjectHash publicKeyHash;

    protected final KaaClientPlatformContext context;
    protected final KaaClientStateListener stateListener;

    AbstractKaaClient(KaaClientPlatformContext context, KaaClientStateListener listener) throws IOException, GeneralSecurityException {
        this.context = context;
        this.stateListener = listener;
        if (context.getProperties() != null) {
            this.properties = context.getProperties();
        } else {
            this.properties = new KaaClientProperties(context.getBase64());
        }

        Map<TransportProtocolId, List<TransportConnectionInfo>> bootstrapServers = this.properties.getBootstrapServers();
        if (bootstrapServers == null || bootstrapServers.isEmpty()) {
            throw new RuntimeException("Unable to obtain list of bootstrap servers."); // NOSONAR
        }

        for (Map.Entry<TransportProtocolId, List<TransportConnectionInfo>> cursor : bootstrapServers.entrySet()) {
            Collections.shuffle(cursor.getValue());
        }

        kaaClientState = new KaaClientPropertiesState(context.createPersistentStorage(), context.getBase64(), this.properties);

        BootstrapTransport bootstrapTransport = new DefaultBootstrapTransport(this.properties.getApplicationToken());
        ProfileTransport profileTransport = new DefaultProfileTransport();
        EventTransport eventTransport = new DefaultEventTransport(kaaClientState);
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

        bootstrapDataProcessor.setBootstrapTransport(bootstrapTransport);

        profileManager = new DefaultProfileManager(profileTransport);
        bootstrapManager = new DefaultBootstrapManager(bootstrapTransport);
        notificationManager = new DefaultNotificationManager(this.kaaClientState, notificationTransport);
        eventManager = new DefaultEventManager(this.kaaClientState, eventTransport);
        eventFamilyFactory = new EventFamilyFactory(this.eventManager);
        endpointRegistrationManager = new DefaultEndpointRegistrationManager(kaaClientState, userTransport, profileTransport);

        channelManager = new DefaultChannelManager(bootstrapManager, bootstrapServers);
        logCollector = new DefaultLogCollector(logTransport, channelManager);

        KaaDataChannel bootstrapChannel = new DefaultBootstrapChannel(this, kaaClientState);
        bootstrapChannel.setMultiplexer(bootstrapDataProcessor);
        bootstrapChannel.setDemultiplexer(bootstrapDataProcessor);
        channelManager.addChannel(bootstrapChannel);

        KaaDataChannel operationsChannel = new DefaultOperationTcpChannel(kaaClientState, channelManager);
        operationsChannel.setMultiplexer(operationsDataProcessor);
        operationsChannel.setDemultiplexer(operationsDataProcessor);
        channelManager.addChannel(operationsChannel);

        bootstrapManager.setChannelManager(channelManager);

        publicKeyHash = EndpointObjectHash.fromSHA1(kaaClientState.getPublicKey().getEncoded());
        metaDataTransport.setClientProperties(this.properties);
        metaDataTransport.setClientState(kaaClientState);
        metaDataTransport.setEndpointPublicKeyhash(publicKeyHash);
        metaDataTransport.setTimeout(LONG_POLL_TIMEOUT);

        bootstrapTransport.setBootstrapManager(bootstrapManager);

        configurationManager = new ResyncConfigurationManager(properties);
        
        transports.put(TransportType.BOOTSTRAP, bootstrapTransport);
        profileTransport.setProfileManager(profileManager);
        profileTransport.setClientProperties(this.properties);
        transports.put(TransportType.PROFILE, profileTransport);
        eventTransport.setEventManager(eventManager);
        transports.put(TransportType.EVENT, eventTransport);
        notificationTransport.setNotificationProcessor(notificationManager);
        transports.put(TransportType.NOTIFICATION, notificationTransport);
        configurationTransport.setConfigurationHashContainer(configurationManager.getConfigurationHashContainer());
        configurationTransport.setConfigurationProcessor(configurationManager.getConfigurationProcessor());
        //TODO: this should be part of properties and provided by user during SDK generation
        configurationTransport.setResyncOnly(true);
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

        channelManager.setConnectivityChecker(context.createConnectivityChecker());
    }

    public AbstractHttpClient createHttpClient(String url, PrivateKey privateKey, PublicKey publicKey, PublicKey remotePublicKey) {
        return context.createHttpClient(url, privateKey, publicKey, remotePublicKey);
    }

    @Override
    public void start() {
        try {
            if (!isInitialized) {
                isInitialized = true;
            } else {
                LOG.warn("Client is already initialized!");
                return;
            }
            //Load configuration
            configurationManager.init();
            bootstrapManager.receiveOperationsServerList();
            if (stateListener != null) {
                stateListener.onStarted();
            }
        } catch (TransportException e) {
            if (stateListener != null) {
                stateListener.onStartFailure(new KaaClusterConnectionException(e));
            }
        } catch (KaaRuntimeException e) {
            if (stateListener != null) {
                stateListener.onStartFailure(new KaaException(e));
            }
        }
    }

    @Override
    public void stop() {
        try {
            kaaClientState.persist();
            channelManager.shutdown();
            isInitialized = false;
            if (stateListener != null) {
                stateListener.onStopped();
            }
        } catch (Exception e) {
            if (stateListener != null) {
                stateListener.onStopFailure(new KaaException(e));
            }
        }
    }

    @Override
    public void pause() {
        try {
            kaaClientState.persist();
            channelManager.pause();
            if (stateListener != null) {
                stateListener.onPaused();
            }
        } catch (Exception e) {
            if (stateListener != null) {
                stateListener.onPauseFailure(new KaaException(e));
            }
        }
    }

    @Override
    public void resume() {
        try {
            channelManager.resume();
            if (stateListener != null) {
                stateListener.onResume();
            }
        } catch (Exception e) {
            if (stateListener != null) {
                stateListener.onResumeFailure(new KaaException(e));
            }
        }
    }

    @Override
    public void setProfileContainer(ProfileContainer container) {
        this.profileManager.setProfileContainer(container);
    };

    @Override
    public void updateProfile() {
        this.profileManager.updateProfile();
    }
    
    @Override
    public void setConfigurationStorage(ConfigurationStorage storage) {
        this.configurationManager.setConfigurationStorage(storage);
    }

    @Override
    public boolean addConfigurationListener(ConfigurationListener listener) {
        return this.configurationManager.addListener(listener);
    }

    @Override
    public boolean removeListener(ConfigurationListener listener) {
        return this.configurationManager.removeListener(listener);
    }

    @Override
    public List<Topic> getTopics() {
        return this.notificationManager.getTopics();
    }

    @Override
    public void addTopicListListener(NotificationTopicListListener listener) {
        this.notificationManager.addTopicListListener(listener);
    }

    @Override
    public void removeTopicListListener(NotificationTopicListListener listener) {
        this.notificationManager.removeTopicListListener(listener);
    }

    @Override
    public void addNotificationListener(NotificationListener listener) {
        this.notificationManager.addNotificationListener(listener);
    }

    @Override
    public void addNotificationListener(String topicId, NotificationListener listener) throws UnavailableTopicException {
        this.notificationManager.addNotificationListener(topicId, listener);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener) {
        this.notificationManager.removeNotificationListener(listener);
    }

    @Override
    public void removeNotificationListener(String topicId, NotificationListener listener) throws UnavailableTopicException {
        this.notificationManager.removeNotificationListener(topicId, listener);
    }

    @Override
    public void subscribeToTopic(String topicId) throws UnavailableTopicException {
        this.notificationManager.subscribeToTopic(topicId, true);
    }

    @Override
    public void subscribeToTopic(String topicId, boolean forceSync) throws UnavailableTopicException {
        this.notificationManager.subscribeToTopic(topicId, forceSync);
    }

    @Override
    public void subscribeToTopics(List<String> topicIds) throws UnavailableTopicException {
        this.notificationManager.subscribeToTopics(topicIds, true);
    }

    @Override
    public void subscribeToTopics(List<String> topicIds, boolean forceSync) throws UnavailableTopicException {
        this.notificationManager.subscribeToTopics(topicIds, forceSync);
    }

    @Override
    public void unsubscribeFromTopic(String topicId) throws UnavailableTopicException {
        this.notificationManager.unsubscribeFromTopic(topicId, true);
    }

    @Override
    public void unsubscribeFromTopic(String topicId, boolean forceSync) throws UnavailableTopicException {
        this.notificationManager.unsubscribeFromTopic(topicId, forceSync);
    }

    @Override
    public void unsubscribeFromTopics(List<String> topicIds) throws UnavailableTopicException {
        this.notificationManager.unsubscribeFromTopics(topicIds, true);
    }

    @Override
    public void unsubscribeFromTopics(List<String> topicIds, boolean forceSync) throws UnavailableTopicException {
        this.notificationManager.unsubscribeFromTopics(topicIds, forceSync);
    }

    @Override
    public void syncTopicsList() {
        this.notificationManager.sync();
    }

    @Override
    public void setLogStorage(LogStorage storage) {
        this.logCollector.setStorage(storage);
    }

    @Override
    public void setLogUploadStrategy(LogUploadStrategy strategy) {
        this.logCollector.setStrategy(strategy);
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
    public KaaChannelManager getChannelManager() {
        return channelManager;
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
    public String regenerateEndpointAccessToken() {
        return null;
    }

    @Override
    public String getEndpointAccessToken() {
        return kaaClientState.getEndpointAccessToken();
    }

    @Override
    public void attachEndpoint(EndpointAccessToken endpointAccessToken, OnAttachEndpointOperationCallback resultListener) {
        endpointRegistrationManager.attachEndpoint(endpointAccessToken, resultListener);
    }

    @Override
    public void detachEndpoint(EndpointKeyHash endpointKeyHash, OnDetachEndpointOperationCallback resultListener) {
        endpointRegistrationManager.detachEndpoint(endpointKeyHash, resultListener);
    }

    @Override
    public void attachUser(String userExternalId, String userAccessToken, UserAttachCallback callback) {
        endpointRegistrationManager.attachUser(userExternalId, userAccessToken, callback);
    }

    @Override
    public void attachUser(String userVerifierToken, String userExternalId, String userAccessToken, UserAttachCallback callback) {
        endpointRegistrationManager.attachUser(userVerifierToken, userExternalId, userAccessToken, callback);
    }

    @Override
    public boolean isAttachedToUser() {
        return kaaClientState.isAttachedToUser();
    }

    @Override
    public void setAttachedListener(AttachEndpointToUserCallback listener) {
        endpointRegistrationManager.setAttachedCallback(listener);
    }

    @Override
    public void setDetachedListener(DetachEndpointFromUserCallback listener) {
        endpointRegistrationManager.setDetachedCallback(listener);
    }
}
