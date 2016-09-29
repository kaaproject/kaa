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

package org.kaaproject.kaa.client;

import org.kaaproject.kaa.client.bootstrap.BootstrapManager;
import org.kaaproject.kaa.client.bootstrap.DefaultBootstrapManager;
import org.kaaproject.kaa.client.channel.BootstrapTransport;
import org.kaaproject.kaa.client.channel.ConfigurationTransport;
import org.kaaproject.kaa.client.channel.EventTransport;
import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.channel.KaaInternalChannelManager;
import org.kaaproject.kaa.client.channel.LogTransport;
import org.kaaproject.kaa.client.channel.MetaDataTransport;
import org.kaaproject.kaa.client.channel.NotificationTransport;
import org.kaaproject.kaa.client.channel.ProfileTransport;
import org.kaaproject.kaa.client.channel.RedirectionTransport;
import org.kaaproject.kaa.client.channel.TransportConnectionInfo;
import org.kaaproject.kaa.client.channel.TransportProtocolId;
import org.kaaproject.kaa.client.channel.UserTransport;
import org.kaaproject.kaa.client.channel.failover.DefaultFailoverManager;
import org.kaaproject.kaa.client.channel.failover.FailoverManager;
import org.kaaproject.kaa.client.channel.failover.strategies.FailoverStrategy;
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
import org.kaaproject.kaa.client.context.ExecutorContext;
import org.kaaproject.kaa.client.context.TransportContext;
import org.kaaproject.kaa.client.event.DefaultEventManager;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventManager;
import org.kaaproject.kaa.client.event.FindEventListenersCallback;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.event.registration.DefaultEndpointRegistrationManager;
import org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback;
import org.kaaproject.kaa.client.event.registration.OnAttachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.OnDetachEndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.UserAttachCallback;
import org.kaaproject.kaa.client.exceptions.KaaClusterConnectionException;
import org.kaaproject.kaa.client.exceptions.KaaException;
import org.kaaproject.kaa.client.exceptions.KaaRuntimeException;
import org.kaaproject.kaa.client.logging.AbstractLogCollector;
import org.kaaproject.kaa.client.logging.DefaultLogCollector;
import org.kaaproject.kaa.client.logging.LogDeliveryListener;
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
import org.kaaproject.kaa.client.profile.ProfileManager;
import org.kaaproject.kaa.client.transport.AbstractHttpClient;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.endpoint.gen.Topic;
import org.kaaproject.kaa.common.hash.EndpointObjectHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
 * Http client ({@link AbstractHttpClient}) is used to provide basic
 * communication with Bootstrap and Operation servers using HTTP protocol.
 * </p>
 *
 * @author Yaroslav Zeygerman
 * @author Andrew Shvayka
 * @see KaaClient
 * @see AbstractHttpClient
 * @see PersistentStorage
 */
public abstract class AbstractKaaClient implements GenericKaaClient {

  protected static final boolean FORCE_SYNC = true;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractKaaClient.class);
  private static final long LONG_POLL_TIMEOUT = 60000L;
  protected final ConfigurationManager configurationManager;
  protected final AbstractLogCollector logCollector;
  protected final KaaClientPlatformContext context;
  protected final KaaClientStateListener stateListener;
  private final DefaultNotificationManager notificationManager;
  private final ProfileManager profileManager;
  private final KaaClientProperties properties;
  private final KaaClientState kaaClientState;
  private final BootstrapManager bootstrapManager;
  private final EventManager eventManager;
  private final EventFamilyFactory eventFamilyFactory;
  private final DefaultEndpointRegistrationManager endpointRegistrationManager;
  private final KaaInternalChannelManager channelManager;
  private final FailoverManager failoverManager;
  protected volatile State clientState = State.CREATED;
  protected FailureListener failureListener = new FailureListener() {
    @Override
    public void onFailure() {
      stop();
    }
  };

  AbstractKaaClient(KaaClientPlatformContext context, KaaClientStateListener listener,
                    boolean isAutogeneratedKeys)
      throws IOException, GeneralSecurityException {

    this.context = context;
    this.stateListener = listener;

    if (context.getProperties() != null) {
      this.properties = context.getProperties();
    } else {
      this.properties = new KaaClientProperties();
    }
    this.properties.setBase64(context.getBase64());

    Map<TransportProtocolId, List<TransportConnectionInfo>> bootstrapServers =
            this.properties.getBootstrapServers();
    if (bootstrapServers == null || bootstrapServers.isEmpty()) {
      throw new RuntimeException("Unable to obtain list of bootstrap services."); // NOSONAR
    }

    for (Map.Entry<TransportProtocolId,
            List<TransportConnectionInfo>> cursor : bootstrapServers.entrySet()) {
      Collections.shuffle(cursor.getValue());
    }

    kaaClientState = new KaaClientPropertiesState(
            context.createPersistentStorage(), context.getBase64(),
        properties, isAutogeneratedKeys);

    TransportContext transportContext = buildTransportContext(properties, kaaClientState);

    bootstrapManager = buildBootstrapManager(properties, kaaClientState, transportContext);

    channelManager = buildChannelManager(bootstrapManager, bootstrapServers);
    failoverManager = buildFailoverManager(channelManager);
    channelManager.setFailoverManager(failoverManager);

    initializeChannels(channelManager, transportContext);

    bootstrapManager.setChannelManager(channelManager);
    bootstrapManager.setFailoverManager(failoverManager);

    profileManager = buildProfileManager(transportContext);
    notificationManager = buildNotificationManager(kaaClientState, transportContext);
    eventManager = buildEventManager(kaaClientState, transportContext);
    endpointRegistrationManager = buildRegistrationManager(kaaClientState, transportContext);
    logCollector = buildLogCollector(transportContext);
    configurationManager = buildConfigurationManager(
            properties, kaaClientState, context.getExecutorContext());

    transportContext.getRedirectionTransport().setBootstrapManager(bootstrapManager);
    transportContext.getBootstrapTransport().setBootstrapManager(bootstrapManager);
    transportContext.getProfileTransport().setProfileManager(profileManager);
    transportContext.getEventTransport().setEventManager(eventManager);
    transportContext.getNotificationTransport().setNotificationProcessor(notificationManager);
    transportContext.getConfigurationTransport().setConfigurationHashContainer(
            configurationManager.getConfigurationHashContainer());
    transportContext.getConfigurationTransport().setConfigurationProcessor(
            configurationManager.getConfigurationProcessor());
    transportContext.getUserTransport().setEndpointRegistrationProcessor(
            endpointRegistrationManager);
    transportContext.getLogTransport().setLogProcessor(logCollector);
    transportContext.initTransports(this.channelManager, this.kaaClientState);

    eventFamilyFactory = new EventFamilyFactory(eventManager, context.getExecutorContext());
  }

  protected void checkClientState(State expected, String message) {
    if (clientState != expected) {
      throw new KaaRuntimeException(message);
    }
  }

  protected void checkClientStateNot(State expected, String message) {
    if (clientState == expected) {
      throw new KaaRuntimeException(message);
    }
  }

  protected void setClientState(State state) {
    clientState = state;
  }

  @Override
  public void start() {
    if (context.needToCheckClientState()) {
      checkClientStateNot(State.STARTED, "Kaa client is already started");
      checkClientStateNot(State.PAUSED, "Kaa client is paused, need to be resumed");
    }
    setClientState(State.STARTED);

    checkReadiness();

    context.getExecutorContext().init();
    getLifeCycleExecutor().submit(new Runnable() {
      @Override
      public void run() {
        LOG.debug("Client startup initiated");
        try {
          // Load configuration
          configurationManager.init();
          bootstrapManager.receiveOperationsServerList();
          if (stateListener != null) {
            stateListener.onStarted();
          }
        } catch (TransportException ex) {
          LOG.error("Start failed", ex);
          if (stateListener != null) {
            stateListener.onStartFailure(new KaaClusterConnectionException(ex));
          }
        } catch (KaaRuntimeException ex) {
          LOG.error("Start failed", ex);
          if (stateListener != null) {
            stateListener.onStartFailure(new KaaException(ex));
          }
        }
      }
    });
  }

  private void checkReadiness() {
    if (profileManager == null || !profileManager.isInitialized()) {
      LOG.error("Profile manager isn't initialized: maybe profile container isn't set");
      if (stateListener != null) {
        stateListener.onStartFailure(new KaaException(
                "Profile manager isn't initialized: maybe profile container isn't set"));
      } else {
        throw new KaaRuntimeException(
                "Profile manager isn't initialized: maybe profile container isn't set");
      }
    }
  }

  @Override
  public void stop() {
    if (context.needToCheckClientState()) {
      checkClientStateNot(State.CREATED, "Kaa client is not started");
      checkClientStateNot(State.STOPPED, "Kaa client is already stopped");
    }
    setClientState(State.STOPPED);

    getLifeCycleExecutor().submit(new Runnable() {
      @Override
      public void run() {
        try {
          logCollector.stop();
          kaaClientState.persist();
          channelManager.shutdown();
          if (stateListener != null) {
            stateListener.onStopped();
          }
        } catch (Exception ex) {
          LOG.error("Stop failed", ex);
          if (stateListener != null) {
            stateListener.onStopFailure(new KaaException(ex));
          }
        }
      }
    });
    context.getExecutorContext().stop();
  }

  @Override
  public void pause() {
    if (context.needToCheckClientState()) {
      checkClientState(State.STARTED, "Kaa client is not started ("
              + clientState.toString().toLowerCase() + " now)");
    }
    setClientState(State.PAUSED);

    getLifeCycleExecutor().submit(new Runnable() {
      @Override
      public void run() {
        try {
          kaaClientState.persist();
          channelManager.pause();
          if (stateListener != null) {
            stateListener.onPaused();
          }
        } catch (Exception ex) {
          LOG.error("Pause failed", ex);
          if (stateListener != null) {
            stateListener.onPauseFailure(new KaaException(ex));
          }
        }
      }
    });
  }

  @Override
  public void resume() {
    if (context.needToCheckClientState()) {
      checkClientState(State.PAUSED, "Kaa client isn't paused");
    }
    setClientState(State.STARTED);

    getLifeCycleExecutor().submit(new Runnable() {
      @Override
      public void run() {
        try {
          channelManager.resume();
          if (stateListener != null) {
            stateListener.onResume();
          }
        } catch (Exception ex) {
          LOG.error("Resume failed", ex);
          if (stateListener != null) {
            stateListener.onResumeFailure(new KaaException(ex));
          }
        }
      }
    });
  }

  private ExecutorService getLifeCycleExecutor() {
    return context.getExecutorContext().getLifeCycleExecutor();
  }

  @Override
  public void setProfileContainer(ProfileContainer container) {
    this.profileManager.setProfileContainer(container);
  }

  @Override
  public void updateProfile() {
    checkClientState(State.STARTED, "Kaa client isn't started");
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
  public boolean removeConfigurationListener(ConfigurationListener listener) {
    return this.configurationManager.removeListener(listener);
  }

  @Override
  public List<Topic> getTopics() {
    checkClientState(State.STARTED, "Kaa client isn't started");
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
  public void addNotificationListener(Long topicId, NotificationListener listener)
          throws UnavailableTopicException {
    this.notificationManager.addNotificationListener(topicId, listener);
  }

  @Override
  public void removeNotificationListener(NotificationListener listener) {
    this.notificationManager.removeNotificationListener(listener);
  }

  @Override
  public void removeNotificationListener(Long topicId, NotificationListener listener)
          throws UnavailableTopicException {
    this.notificationManager.removeNotificationListener(topicId, listener);
  }

  @Override
  public void subscribeToTopic(Long topicId) throws UnavailableTopicException {
    subscribeToTopic(topicId, FORCE_SYNC);
  }

  @Override
  public void subscribeToTopic(Long topicId, boolean forceSync) throws UnavailableTopicException {
    checkClientState(State.STARTED, "Kaa client isn't started");
    notificationManager.subscribeToTopic(topicId, forceSync);
  }

  @Override
  public void subscribeToTopics(List<Long> topicIds) throws UnavailableTopicException {
    subscribeToTopics(topicIds, FORCE_SYNC);
  }

  @Override
  public void subscribeToTopics(List<Long> topicIds, boolean forceSync)
          throws UnavailableTopicException {
    checkClientState(State.STARTED, "Kaa client isn't started");
    notificationManager.subscribeToTopics(topicIds, forceSync);
  }

  @Override
  public void unsubscribeFromTopic(Long topicId) throws UnavailableTopicException {
    unsubscribeFromTopic(topicId, FORCE_SYNC);
  }

  @Override
  public void unsubscribeFromTopic(Long topicId, boolean forceSync)
          throws UnavailableTopicException {
    checkClientState(State.STARTED, "Kaa client isn't started");
    notificationManager.unsubscribeFromTopic(topicId, forceSync);
  }

  @Override
  public void unsubscribeFromTopics(List<Long> topicIds) throws UnavailableTopicException {
    unsubscribeFromTopics(topicIds, FORCE_SYNC);
  }

  @Override
  public void unsubscribeFromTopics(List<Long> topicIds, boolean forceSync)
          throws UnavailableTopicException {
    checkClientState(State.STARTED, "Kaa client isn't started");
    this.notificationManager.unsubscribeFromTopics(topicIds, forceSync);
  }

  @Override
  public void syncTopicsList() {
    checkClientState(State.STARTED, "Kaa client isn't started");
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
  public EventFamilyFactory getEventFamilyFactory() {
    //TODO: on which stage do we need to check client's state, here or in a specific event factory?
    return eventFamilyFactory;
  }

  @Override
  public void findEventListeners(List<String> eventFqns, FindEventListenersCallback listener) {
    checkClientState(State.STARTED, "Kaa client isn't started");
    eventManager.findEventListeners(eventFqns, listener);
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
  public PrivateKey getClientPrivateKey() {
    return kaaClientState.getPrivateKey();
  }

  @Override
  public String getEndpointKeyHash() {
    return kaaClientState.getEndpointKeyHash().getKeyHash();
  }

  @Override
  public String refreshEndpointAccessToken() {
    return endpointRegistrationManager.refreshEndpointAccessToken();
  }

  @Override
  public String getEndpointAccessToken() {
    return kaaClientState.getEndpointAccessToken();
  }

  @Override
  public void setEndpointAccessToken(String token) {
    endpointRegistrationManager.updateEndpointAccessToken(token);
  }

  @Override
  public void attachEndpoint(EndpointAccessToken endpointAccessToken,
                             OnAttachEndpointOperationCallback resultListener) {
    checkClientState(State.STARTED, "Kaa client isn't started");
    endpointRegistrationManager.attachEndpoint(endpointAccessToken, resultListener);
  }

  @Override
  public void detachEndpoint(
          EndpointKeyHash endpointKeyHash, OnDetachEndpointOperationCallback resultListener) {
    checkClientState(State.STARTED, "Kaa client isn't started");
    endpointRegistrationManager.detachEndpoint(endpointKeyHash, resultListener);
  }

  @Override
  public void attachUser(
          String userExternalId, String userAccessToken, UserAttachCallback callback) {
    checkClientState(State.STARTED, "Kaa client isn't started");
    endpointRegistrationManager.attachUser(userExternalId, userAccessToken, callback);
  }

  @Override
  public void attachUser(String userVerifierToken, String userExternalId, String userAccessToken,
                         UserAttachCallback callback) {
    checkClientState(State.STARTED, "Kaa client isn't started");
    endpointRegistrationManager.attachUser(
            userVerifierToken, userExternalId, userAccessToken, callback);
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

  @Override
  public void setLogDeliveryListener(LogDeliveryListener listener) {
    logCollector.setLogDeliveryListener(listener);
  }

  @Override
  public void setFailoverStrategy(FailoverStrategy failoverStrategy) {
    failoverManager.setFailoverStrategy(failoverStrategy);
  }

  @Override
  public void setFailureListener(FailureListener failureListener) {
    if (failureListener == null) {
      throw new IllegalStateException("Failure listener can't be null");
    }

    this.failureListener = failureListener;
  }

  protected TransportContext buildTransportContext(
          KaaClientProperties properties, KaaClientState kaaClientState) {

    EndpointObjectHash publicKeyHash = EndpointObjectHash.fromSha1(
            kaaClientState.getPublicKey().getEncoded());
    MetaDataTransport mdTransport = new DefaultMetaDataTransport();
    mdTransport.setClientProperties(properties);
    mdTransport.setClientState(kaaClientState);
    mdTransport.setEndpointPublicKeyhash(publicKeyHash);
    mdTransport.setTimeout(LONG_POLL_TIMEOUT);

    BootstrapTransport bootstrapTransport = buildBootstrapTransport(properties, kaaClientState);
    ProfileTransport profileTransport = buildProfileTransport();
    EventTransport eventTransport = buildEventTransport(kaaClientState);
    NotificationTransport notificationTransport = buildNotificationTransport();
    ConfigurationTransport configurationTransport = buildConfigurationTransport();
    UserTransport userTransport = buildUserTransport();
    RedirectionTransport redirectionTransport = buildRedirectionTransport();
    LogTransport logTransport = buildLogTransport();
    return new TransportContext(mdTransport, bootstrapTransport, profileTransport, eventTransport,
            notificationTransport, configurationTransport, userTransport, redirectionTransport,
            logTransport);
  }

  protected KaaInternalChannelManager buildChannelManager(
          BootstrapManager bootstrapManager, Map<TransportProtocolId,
          List<TransportConnectionInfo>> bootstrapServers) {
    KaaInternalChannelManager kaaInternalChannelManager =
        new DefaultChannelManager(bootstrapManager, bootstrapServers, context.getExecutorContext(),
                failureListener);
    kaaInternalChannelManager.setConnectivityChecker(context.createConnectivityChecker());
    return kaaInternalChannelManager;
  }

  protected void initializeChannels(
          KaaInternalChannelManager channelManager, TransportContext transportContext) {
    DefaultBootstrapDataProcessor bootstrapDataProcessor = new DefaultBootstrapDataProcessor();
    bootstrapDataProcessor.setBootstrapTransport(transportContext.getBootstrapTransport());

    DefaultOperationDataProcessor operationsDataProcessor = new DefaultOperationDataProcessor(
            kaaClientState);
    operationsDataProcessor.setConfigurationTransport(transportContext.getConfigurationTransport());
    operationsDataProcessor.setEventTransport(transportContext.getEventTransport());
    operationsDataProcessor.setMetaDataTransport(transportContext.getMdTransport());
    operationsDataProcessor.setNotificationTransport(transportContext.getNotificationTransport());
    operationsDataProcessor.setProfileTransport(transportContext.getProfileTransport());
    operationsDataProcessor.setRedirectionTransport(transportContext.getRedirectionTransport());
    operationsDataProcessor.setUserTransport(transportContext.getUserTransport());
    operationsDataProcessor.setLogTransport(transportContext.getLogTransport());

    KaaDataChannel bootstrapChannel = new DefaultBootstrapChannel(
            this, kaaClientState, failoverManager);
    bootstrapChannel.setMultiplexer(bootstrapDataProcessor);
    bootstrapChannel.setDemultiplexer(bootstrapDataProcessor);
    channelManager.addChannel(bootstrapChannel);

    KaaDataChannel operationsChannel = new DefaultOperationTcpChannel(
            kaaClientState, failoverManager, failureListener);
    operationsChannel.setMultiplexer(operationsDataProcessor);
    operationsChannel.setDemultiplexer(operationsDataProcessor);
    channelManager.addChannel(operationsChannel);
  }

  protected FailoverManager buildFailoverManager(KaaChannelManager channelManager) {
    return new DefaultFailoverManager(channelManager, context.getExecutorContext());
  }

  protected ResyncConfigurationManager buildConfigurationManager(
          KaaClientProperties properties, KaaClientState kaaClientState,
          ExecutorContext executorContext) {
    return new ResyncConfigurationManager(properties, kaaClientState, executorContext);
  }

  protected DefaultLogCollector buildLogCollector(TransportContext transportContext) {
    return new DefaultLogCollector(transportContext.getLogTransport(), context.getExecutorContext(),
            channelManager, failoverManager);
  }

  protected DefaultEndpointRegistrationManager buildRegistrationManager(
          KaaClientState kaaClientState, TransportContext transportContext) {
    return new DefaultEndpointRegistrationManager(kaaClientState, context.getExecutorContext(),
        transportContext.getUserTransport(), transportContext.getProfileTransport());
  }

  protected DefaultEventManager buildEventManager(KaaClientState kaaClientState,
                                                  TransportContext transportContext) {
    return new DefaultEventManager(kaaClientState, context.getExecutorContext(),
            transportContext.getEventTransport());
  }

  protected DefaultNotificationManager buildNotificationManager(
          KaaClientState kaaClientState, TransportContext transportContext) {
    return new DefaultNotificationManager(kaaClientState, context.getExecutorContext(),
            transportContext.getNotificationTransport());
  }

  protected ProfileManager buildProfileManager(TransportContext transportContext) {
    return new DefaultProfileManager(transportContext.getProfileTransport());
  }

  protected BootstrapManager buildBootstrapManager(
          KaaClientProperties properties, KaaClientState kaaClientState,
          TransportContext transportContext) {
    return new DefaultBootstrapManager(transportContext.getBootstrapTransport(),
            context.getExecutorContext(), failureListener);
  }

  public AbstractHttpClient createHttpClient(
          String url, PrivateKey privateKey, PublicKey publicKey, PublicKey remotePublicKey) {
    return context.createHttpClient(url, privateKey, publicKey, remotePublicKey);
  }

  protected BootstrapTransport buildBootstrapTransport(
          KaaClientProperties properties, KaaClientState kaaClientState) {
    return new DefaultBootstrapTransport(properties.getSdkToken());
  }

  protected ProfileTransport buildProfileTransport() {
    ProfileTransport transport = new DefaultProfileTransport();
    transport.setClientProperties(this.properties);
    return transport;
  }

  protected ConfigurationTransport buildConfigurationTransport() {
    ConfigurationTransport transport = new DefaultConfigurationTransport();
    // TODO: this should be part of properties and provided by user during
    // SDK generation
    transport.setResyncOnly(true);
    return transport;
  }

  protected NotificationTransport buildNotificationTransport() {
    return new DefaultNotificationTransport();
  }

  protected DefaultUserTransport buildUserTransport() {
    return new DefaultUserTransport();
  }

  protected EventTransport buildEventTransport(KaaClientState kaaClientState) {
    return new DefaultEventTransport(kaaClientState);
  }

  protected LogTransport buildLogTransport() {
    return new DefaultLogTransport();
  }

  protected RedirectionTransport buildRedirectionTransport() {
    return new DefaultRedirectionTransport();
  }

  protected enum State {
    CREATED,
    STARTED,
    PAUSED,
    STOPPED
  }

}
