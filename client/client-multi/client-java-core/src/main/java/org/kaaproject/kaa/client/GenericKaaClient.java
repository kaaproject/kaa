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
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import org.kaaproject.kaa.client.channel.KaaChannelManager;
import org.kaaproject.kaa.client.channel.KaaDataChannel;
import org.kaaproject.kaa.client.configuration.delta.manager.DeltaManager;
import org.kaaproject.kaa.client.configuration.manager.ConfigurationManager;
import org.kaaproject.kaa.client.configuration.storage.ConfigurationPersistenceManager;
import org.kaaproject.kaa.client.event.EndpointAccessToken;
import org.kaaproject.kaa.client.event.EndpointKeyHash;
import org.kaaproject.kaa.client.event.EventFamilyFactory;
import org.kaaproject.kaa.client.event.EventListenersResolver;
import org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback;
import org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback;
import org.kaaproject.kaa.client.event.registration.EndpointOperationCallback;
import org.kaaproject.kaa.client.event.registration.EndpointRegistrationManager;
import org.kaaproject.kaa.client.logging.LogStorage;
import org.kaaproject.kaa.client.logging.LogUploadStrategy;
import org.kaaproject.kaa.client.notification.NotificationListener;
import org.kaaproject.kaa.client.notification.NotificationManager;
import org.kaaproject.kaa.client.notification.NotificationTopicListListener;
import org.kaaproject.kaa.client.notification.UnavailableTopicException;
import org.kaaproject.kaa.client.profile.ProfileContainer;
import org.kaaproject.kaa.client.schema.storage.SchemaPersistenceManager;
import org.kaaproject.kaa.client.transport.TransportException;
import org.kaaproject.kaa.common.endpoint.gen.Topic;

/**
 * <p>
 * Root interface for the Kaa client.
 * This interface contain methods that are predefined and does not contain any auto-generated code. 
 * </p>
 *
 *
 * @author Yaroslav Zeygerman
 * @author Andrew Shvayka
 *
 * @see ConfigurationManager
 * @see DeltaManager
 * @see ConfigurationPersistenceManager
 * @see SchemaPersistenceManager
 * @see NotificationManager
 * @see EventFamilyFactory
 * @see EndpointRegistrationManager
 * @see EventListenersResolver
 * @see KaaChannelManager
 * @see PublicKey
 * @see PrivateKey
 * @see KaaDataChannel
 */
public interface GenericKaaClient {
    /**
     * <p>
     * Starts Kaa's workflow.
     * </p>
     *
     * <p>
     * Should be called after each call to {@link Kaa#init()}.
     * </p>
     *
     * @see AbstractKaaClient#start()
     */
    public void start() throws IOException, TransportException;

    /**
     * Stops Kaa's workflow.
     *
     * @see AbstractKaaClient#stop()
     */
    public void stop();

    /**
     * Pauses Kaa's workflow.
     */
    public void pause();

    /**
     * Resumes Kaa's workflow.
     */
    public void resume();
    /**
     * Sets profile container implemented by the user.
     *
     * @param container
     *            User-defined container
     * @see ProfileContainer
     *
     */
    void setProfileContainer(ProfileContainer container);

    /**
     * Sync of updated profile with server
     */
    void updateProfile();

    /**
     * <p>
     * Add listener for notification topics' list updates.
     * </p>
     *
     * @param listener
     *            the listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    void addTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>
     * Remove listener of notification topics' list updates.
     * </p>
     *
     * @param listener
     *            listener the listener which is no longer needs updates.
     * @see NotificationTopicListListener
     *
     */
    void removeTopicListListener(NotificationTopicListListener listener);

    /**
     * <p>
     * Retrieve a list of available notification topics.
     * </p>
     *
     * @return List of available topics
     *
     */
    List<Topic> getTopics();

    /**
     * <p>
     * Add listener to receive all notifications (both for mandatory and
     * optional topics).
     * </p>
     *
     * @param listener
     *            Listener to receive notifications
     *
     * @see NotificationListener
     */
    void addNotificationListener(NotificationListener listener);

    /**
     * <p>
     * Add listener to receive notifications relating to the specified topic.
     * </p>
     *
     * <p>
     * Listener(s) for optional topics may be added/removed irrespective to
     * whether subscription was already or not.
     * </p>
     *
     * @param topicId
     *            Id of topic (both mandatory and optional).
     * @param listener
     *            Listener to receive notifications.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided.
     *
     * @see NotificationListener
     */
    void addNotificationListener(String topicId, NotificationListener listener) throws UnavailableTopicException;

    /**
     * <p>
     * Remove listener receiving all notifications (both for mandatory and
     * optional topics).
     * </p>
     *
     * @param listener
     *            Listener to receive notifications
     *
     * @see NotificationListener
     */
    void removeNotificationListener(NotificationListener listener);

    /**
     * <p>
     * Remove listener receiving notifications for the specified topic.
     * </p>
     *
     * <p>
     * Listener(s) for optional topics may be added/removed irrespective to
     * whether subscription was already or not.
     * </p>
     *
     * @param topicId
     *            Id of topic (both mandatory and optional).
     * @param listener
     *            Listener to receive notifications.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided.
     *
     * @see NotificationListener
     */
    void removeNotificationListener(String topicId, NotificationListener listener) throws UnavailableTopicException;

    /**
     * <p>
     * Subscribe to notifications relating to the specified optional topic.
     * </p>
     *
     * @param topicId
     *            Id of a optional topic.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     */
    void subscribeToTopic(String topicId) throws UnavailableTopicException;
    
    /**
     * <p>
     * Subscribe to notifications relating to the specified optional topic.
     * </p>
     *
     * @param topicId
     *            Id of a optional topic.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #syncTopicsList()
     */
    void subscribeToTopic(String topicId, boolean forceSync) throws UnavailableTopicException;

    /**
     * <p>
     * Subscribe to notifications relating to the specified list of optional
     * topics.
     * </p>
     *
     * @param topicIds
     *            List of optional topic id.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     */
    void subscribeToTopics(List<String> topicIds) throws UnavailableTopicException;

    /**
     * <p>
     * Subscribe to notifications relating to the specified list of optional
     * topics.
     * </p>
     *
     * @param topicIds
     *            List of optional topic id.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #syncTopicsList()
     */
    void subscribeToTopics(List<String> topicIds, boolean forceSync) throws UnavailableTopicException;
    
    /**
     * <p>
     * Unsubscribe from notifications relating to the specified optional topic.
     * </p>
     *
     * <p>
     * All previously added listeners will be removed automatically.
     * </p>
     *
     * @param topicId
     *            Id of a optional topic.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     */
    void unsubscribeFromTopic(String topicId) throws UnavailableTopicException;

    /**
     * <p>
     * Unsubscribe from notifications relating to the specified optional topic.
     * </p>
     *
     * <p>
     * All previously added listeners will be removed automatically.
     * </p>
     *
     * @param topicId
     *            Id of a optional topic.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #syncTopicsList()
     */
    void unsubscribeFromTopic(String topicId, boolean forceSync) throws UnavailableTopicException;
    
    /**
     * <p>
     * Unsubscribe from notifications relating to the specified list of optional
     * topics.
     * </p>
     *
     * <p>
     * All previously added listeners will be removed automatically.
     * </p>
     *
     * @param topicIds
     *            List of optional topic id.
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     */
    void unsubscribeFromTopics(List<String> topicIds) throws UnavailableTopicException;

    /**
     * <p>
     * Unsubscribe from notifications relating to the specified list of optional
     * topics.
     * </p>
     *
     * <p>
     * All previously added listeners will be removed automatically.
     * </p>
     *
     * @param topicIds
     *            List of optional topic id.
     * @param forceSync
     *            Define whether current subscription update should be accepted
     *            immediately (see {@link #sync()}).
     *
     * @throws UnavailableTopicException
     *             Throw if unknown topic id is provided or topic isn't
     *             optional.
     *
     * @see #syncTopicsList()
     */
    void unsubscribeFromTopics(List<String> topicIds, boolean forceSync) throws UnavailableTopicException;

    /**
     * <p>
     * Force sync of pending subscription changes with server.
     * </p>
     *
     * <p>
     * Should be used after all {@link #subscribeToTopic(String, boolean)},
     * {@link #subscribeToTopics(List, boolean)},
     * {@link #unsubscribeFromTopic(String, boolean)},
     * {@link #unsubscribeFromTopics(List, boolean)} calls with parameter
     * {@code forceSync} set to {@code false}.
     * </p>
     *
     * <p>
     * Use it as a convenient way to make different consequent changes in the
     * optional subscription:
     * </p>
     * 
     * <pre>
     * {
     *     // Make subscription changes
     *     kaaClient.subscribeOnTopics(Arrays.asList(&quot;optional_topic1&quot;, &quot;optional_topic2&quot;, &quot;optional_topic3&quot;), false);
     *     kaaClient.unsubscribeFromTopic(&quot;optional_topic4&quot;, false);
     * 
     *     // Add listeners for topics here
     * 
     *     // Commit changes
     *     kaaClient.syncTopicsList();
     * }
     * </pre>
     */
    void syncTopicsList();

    /**
     * Set user implementation of a log storage.
     *
     * @param storage User-defined log storage object
     */
    void setLogStorage(LogStorage storage);

    /**
     * Set user implementation of a log upload strategy.
     *
     * @param strategy User-defined log upload strategy object.
     */
    void setLogUploadStrategy(LogUploadStrategy strategy);

    /**
     * Retrieves Kaa configuration manager.
     *
     * @return {@link ConfigurationManager} object.
     *
     */
    ConfigurationManager getConfigurationManager();

    /**
     * Retrieves Kaa delta manager.
     *
     * @return {@link DeltaManager} object.
     *
     */
    DeltaManager getDeltaManager();

    /**
     * Retrieves Kaa configuration persistence manager.
     *
     * @return {@link ConfigurationPersistenceManager} object.
     *
     */
    ConfigurationPersistenceManager getConfigurationPersistenceManager();

    /**
     * Retrieves Kaa schema persistence manager.
     *
     * @return {@link SchemaPersistenceManager} object.
     *
     */
    SchemaPersistenceManager getSchemaPersistenceManager();

    /**
     * Retrieves Kaa event family factory.
     *
     * @return {@link EventFamilyFactory} object.
     *
     */
    EventFamilyFactory getEventFamilyFactory();

    /**
     * Retrieves Kaa endpoint registration manager
     *
     * @return {@link EndpointRegistrationManager} object
     */
    EndpointRegistrationManager getEndpointRegistrationManager();

    /**
     * Retrieves Kaa event listeners resolver
     *
     * @return {@link EventListenersResolver} object
     */
    EventListenersResolver getEventListenerResolver();

    /**
     * Retrieves Kaa channel manager
     *
     * @return {@link KaaChannelManager} object
     */
    KaaChannelManager getChannelManager();

    /**
     * <p>
     * Retrieves the client's public key.
     * </p>
     *
     * <p>
     * Required in user implementation of an operation data channel. Public key
     * hash (SHA-1) is used by servers as identification number to uniquely
     * identify each connected endpoint.
     * </p>
     *
     * @return client's public key
     */
    PublicKey getClientPublicKey();

    /**
     * <p>
     * Retrieves endpoint public key hash.
     * </p>
     *
     * <p>
     * Required in {@link EndpointRegistrationManager} implementation to react
     * on detach response from Operations server.
     * </p>
     *
     * @return String containing current endpoint's public key hash.
     */
    String getEndpointKeyHash();

    /**
     * <p>
     * Retrieves the client's private key.
     * </p>
     *
     * <p>
     * Required in user implementation of an operation data channel. Private key
     * is used by encryption schema between endpoint and servers.
     * </p>
     *
     * @return client's private key
     */
    PrivateKey getClientPrivateKey();

    /**
     * Generate new access token for a current endpoint
     */
    String regenerateEndpointAccessToken();

    /**
     * Retrieve an access token for a current endpoint
     */
    String getEndpointAccessToken();

    /**
     * Updates with new endpoint attach request<br>
     * <br>
     * {@link org.kaaproject.kaa.client.event.registration.EndpointOperationCallback} is populated with {@link org.kaaproject.kaa.client.event.EndpointKeyHash} of an
     * attached endpoint.
     *
     * @param endpointAccessToken Access token of the attaching endpoint
     * @param resultListener Listener to notify about result of the endpoint attaching
     *
     * @see org.kaaproject.kaa.client.event.EndpointAccessToken
     * @see org.kaaproject.kaa.client.event.registration.EndpointOperationCallback
     */
    void attachEndpoint(EndpointAccessToken endpointAccessToken, EndpointOperationCallback resultListener);

    /**
     * Updates with new endpoint detach request
     *
     * @param endpointKeyHash Key hash of the detaching endpoint
     * @param resultListener Listener to notify about result of the enpoint attaching
     *
     * @see org.kaaproject.kaa.client.event.EndpointKeyHash
     * @see EndpointOperationCallback
     */
    void detachEndpoint(EndpointKeyHash endpointKeyHash, EndpointOperationCallback resultListener);

    /**
     * Creates user attach request using default verifier. Default verifier is selected during SDK generation.
     * If there was no default verifier selected this method will throw runtime exception.
     *
     * @param userExternalId
     * @param userAccessToken
     * @param callback called when authentication result received
     *
     * @see UserAuthResultListener
     */
    void attachUser(String userExternalId, String userAccessToken, UserAuthResultListener callback);

    /**
     * Creates user attach request using specified verifier.
     *
     * @param userVerifierToken
     * @param userExternalId
     * @param userAccessToken
     * @param callback called when authentication result received
     *
     * @see UserAuthResultListener
     */
    void attachUser(String userVerifierToken, String userExternalId, String userAccessToken, UserAuthResultListener callback);

    /**
     * Checks if current endpoint is attached to user.
     *
     * @return true if current endpoint is attached to any user, false otherwise.
     */
    boolean isAttachedToUser();

    /**
     * Sets callback for notifications when current endpoint is attached to user
     *
     * @param listener
     *
     * @see org.kaaproject.kaa.client.event.registration.AttachEndpointToUserCallback
     */
    void setAttachedListener(AttachEndpointToUserCallback listener);

    /**
     * Sets callback for notifications when current endpoint is detached from user
     *
     * @param listener
     *
     * @see org.kaaproject.kaa.client.event.registration.DetachEndpointFromUserCallback
     */
    void setDetachedListener(DetachEndpointFromUserCallback listener);
}
