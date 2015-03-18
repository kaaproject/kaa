/*
 * Copyright 2014-2015 CyberVision, Inc.
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

#ifndef IKAACLIENT_HPP_
#define IKAACLIENT_HPP_

#include "kaa/profile/IProfileContainer.hpp"
#include "kaa/notification/INotificationTopicListListener.hpp"
#include "kaa/notification/gen/NotificationDefinitions.hpp"
#include "kaa/notification/INotificationListener.hpp"
#include "kaa/configuration/storage/IConfigurationStorage.hpp"
#include "kaa/configuration/gen/ConfigurationDefinitions.hpp"
#include "kaa/event/registration/IAttachEndpointCallback.hpp"
#include "kaa/event/registration/IDetachEndpointCallback.hpp"
#include "kaa/event/registration/IUserAttachCallback.hpp"
#include "kaa/event/registration/IAttachStatusListener.hpp"
#include "kaa/log/ILogCollector.hpp"


namespace kaa {

class EventFamilyFactory;;
class IKaaChannelManager;
class ILogCollector;
class IKaaDataMultiplexer;
class IKaaDataDemultiplexer;
class IFetchEventListeners;
class IConfigurationReceiver;
class KeyPair;

/**
 * Interface for the Kaa client.
 *
 * Base interface to operate with @link Kaa @endlink library.
 *
 * @author Yaroslav Zeygerman
 *
 */
class IKaaClient {
public:

    /**
     * Sets profile container implemented by the user.
     *
     * @param container User-defined container
     * @see AbstractProfileContainer
     *
     */
    virtual void setProfileContainer(ProfileContainerPtr container) = 0;

    /**
     * Retrieves Kaa event family factory.
     *
     * @return @link IEventFamilyFactory @endlink object.
     *
     */
    virtual EventFamilyFactory&               getEventFamilyFactory() = 0;

    /**
     * <p>Add listener to receive updates of available topics.</p>
     *
     * @param listener The listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    virtual void addTopicListListener(INotificationTopicListListenerPtr listener) = 0;

    /**
     * <p>Remove listener receiving updates of available topics.</p>
     *
     * @param listener The listener to receive updates.
     * @see NotificationTopicListListener
     *
     */
    virtual void removeTopicListListener(INotificationTopicListListenerPtr listener) = 0;

    /**
     * <p>Retrieve a list of available topics.</p>
     *
     * @return List of available topics
     *
     */
    virtual Topics getTopics() = 0;

    /**
     * <p>Add listener to receive all notifications (both for mandatory and
     * optional topics).</p>
     *
     * @param listener The listener to receive notifications.
     *
     * @see AbstractNotificationListener
     */
    virtual void addNotificationListener(INotificationListenerPtr listener) = 0;

    /**
     * <p>Add listener to receive notifications relating to the specified topic.</p>
     *
     * <p>Listener(s) for optional topics may be added/removed irrespective to
     * whether subscription was already or not.</p>
     *
     * @param topicId  Id of topic (either mandatory or optional).
     * @param listener The listener to receive notifications.
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    virtual void addNotificationListener(const std::string& topidId, INotificationListenerPtr listener) = 0;

    /**
     * <p>Remove listener receiving all notifications (both for mandatory and
     * optional topics).</p>
     *
     * @param listener Listener to receive notifications
     *
     * @see AbstractNotificationListener
     */
    virtual void removeNotificationListener(INotificationListenerPtr listener) = 0;

    /**
     * <p>Remove listener receiving notifications for the specified topic.</p>
     *
     * <p>Listener(s) for optional topics may be added/removed irrespective to
     * whether subscription was already or not.</p>
     *
     * @param topicId Id of topic (either mandatory or optional).
     * @param listener Listener to receive notifications.
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided.
     *
     * @see AbstractNotificationListener
     */
    virtual void removeNotificationListener(const std::string& topidId, INotificationListenerPtr listener) = 0;

    /**
     * <p>Subscribe to notifications relating to the specified optional topic.</p>
     *
     * @param topicId Id of a optional topic.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see @link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void subscribeToTopic(const std::string& id, bool forceSync) = 0;

    /**
     * <p>Subscribe to notifications relating to the specified list of
     * optional topics.</p>
     *
     * @param topicIds List of optional topic id.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see @link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void subscribeToTopics(const std::list<std::string>& idList, bool forceSync) = 0;

    /**
     * <p>Unsubscribe from notifications relating to the specified optional topic.</p>
     *
     * <p>All previously added listeners will be removed automatically.</p>
     *
     * @param topicId Id of a optional topic.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see @link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void unsubscribeFromTopic(const std::string& id, bool forceSync) = 0;

    /**
     * <p>Unsubscribe from notifications relating to the specified list of
     * optional topics.</p>
     *
     * <p>All previously added listeners will be removed automatically.</p>
     *
     * @param topicIds List of optional topic id.
     * @param forceSync Define whether current subscription update should be
     * accepted immediately (see {@link sync() @endlink).
     *
     * @throws UnavailableTopicException Throw if unknown topic id is provided or
     * topic isn't optional.
     *
     * @see sync()
     */
    virtual void unsubscribeFromTopics(const std::list<std::string>& idList, bool forceSync) = 0;

    /**
     * <p>Accept optional subscription changes.</p>
     *
     * <p>Should be used after all @link subscribeToTopic() @endlink,
     * @link subscribeToTopics() @endlink, @link unsubscribeFromTopic() @endlink,
     * @link unsubscribeFromTopics() @endlink calls with parameter
     * <i>forceSync</i> set to <i>false</i>.</p>
     *
     * <p>Use it as a convenient way to make different consequent changes in
     * the optional subscription:</p>
     * @code
     *  NotificationManager notificationManager = kaaClient.getNotificationManager();
     *
     *  // Make subscription changes
     *  notificationManager.subscribeToTopics(Arrays.asList(
     *          "optional_topic1", "optional_topic2", "optional_topic3"), false);
     *  notificationManager.unsubscribeFromTopic("optional_topic4", false);
     *
     *  // Add listeners for optional topics (optional)
     *
     *  // Commit changes
     *  notificationManager.sync();
     * @endcode
     * </pre>
     */
    virtual void syncTopicsList() = 0;

    /**
     * Subscribes listener of configuration updates.
     *
     * @param receiver Listener to be added to notification list.
     */
    virtual void addConfigurationListener(IConfigurationReceiver &receiver) = 0;

    /**
     * Unsubscribes listener of configuration updates.
     *
     * @param receiver Listener to be removed from notification list.
     */
    virtual void removeConfigurationListener(IConfigurationReceiver &receiver) = 0;
    /**
     * Returns full configuration tree which is actual at current moment.
     *
     * @return @link ICommonRecord @endlink containing current configuration tree.
     */
    virtual const KaaRootConfiguration& getConfiguration() = 0;

    /**
     * Registers new configuration persistence routines. Replaces previously set value.
     * Memory pointed by given parameter should be managed by user.
     *
     * @param storage User-defined persistence routines.
     * @see IConfigurationStorage
     */
    virtual void setConfigurationStorage(IConfigurationStoragePtr storage) = 0;

    /**
     * @brief Attaches the specified endpoint to the user to which the current endpoint is attached.
     *
     *    @param[in] endpointAccessToken    The access token of the endpoint to be attached to the user.
     * @param[in] listener               The optional listener to notify of the result.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void attachEndpoint(const std::string&  endpointAccessToken
                                 , IAttachEndpointCallbackPtr listener = IAttachEndpointCallbackPtr()) = 0;

    /**
     * @brief Detaches the specified endpoint from the user to which the current endpoint is attached.
     *
     * @param[in] endpointKeyHash    The key hash of the endpoint to be detached from the user.
     * @param[in] listener           The optional listener to notify of the result.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void detachEndpoint(const std::string&  endpointKeyHash
                                  , IDetachEndpointCallbackPtr listener = IDetachEndpointCallbackPtr()) = 0;

    /**
     * @brief Attaches the current endpoint to the specifier user. The user verification is carried out by the default verifier.
     *
     * @b NOTE: If the default user verifier (@link DEFAULT_USER_VERIFIER_TOKEN @endlink) is not specified,
     * the attach attempt fails with the @c KaaException exception.
     *
     * <b>Only endpoints associated with the same user can exchange events.</b>
     *
     * @param[in] userExternalId     The external user ID.
     * @param[in] userAccessToken    The user access token.
     *
     * @throw BadCredentials                The endpoint access token is empty.
     * @throw TransportNotFoundException    The Kaa SDK isn't fully initialized.
     * @throw KaaException                  Some other failure has happened.
     */
    virtual void attachUser(const std::string& userExternalId
                              , const std::string& userAccessToken
                              , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr()) = 0;

    virtual void attachUser(const std::string& userExternalId
                              , const std::string& userAccessToken
                              , const std::string& userVerifierToken
                              , IUserAttachCallbackPtr listener = IUserAttachCallbackPtr()) = 0;

    /**
     * @brief Sets listener to notify of the current endpoint is attached/detached by another one.
     *
     * @param[in] listener    Listener to notify of the attach status is changed.
     */
    virtual void setAttachStatusListener(IAttachStatusListenerPtr listener) = 0;
    /**
     * @brief Checks if the current endpoint is already attached to some user.
     *
     * @return TRUE if the current endpoint is attached, FALSE otherwise.
     */
    virtual bool isAttachedToUser() = 0;

    /**
     * Submits an event listeners resolution request
     *
     * @param eventFQNs     List of event class FQNs which have to be supported by endpoint.
     * @param listener      Result listener {@link IFetchEventListeners}}
     *
     * @throw KaaException when data is invalid (empty list or null listener)
     *
     * @return Request ID of submitted request
     */
    virtual std::int32_t findEventListeners(const std::list<std::string>& eventFQNs
               , IFetchEventListeners* listener) = 0;

    virtual void addLogRecord(const KaaUserLogRecord& record) = 0;
    virtual void setLogStorage(ILogStoragePtr storage) = 0;
    virtual void setLogUploadStrategy(ILogUploadStrategyPtr strategy) = 0;

    /**
     * Retrieves the Channel Manager
     */
    virtual IKaaChannelManager&                 getChannelManager() = 0;
    /**
     * Retrieves the client's public and private key.
     *
     * Required in user implementation of an operation data channel.
     * Public key hash (SHA-1) is used by servers as identification number to
     * uniquely identify each connected endpoint.
     *
     * Private key is used by encryption schema between endpoint and servers.
     *
     * @return client's public/private key pair
     */
    virtual const KeyPair&                    getClientKeyPair() = 0;

    /**
     * Retrieves Kaa operations data multiplexer
     *
     * @return @link IKaaDataMultiplexer @endlink object
     */
    virtual IKaaDataMultiplexer&              getOperationMultiplexer() = 0;

    /**
     * Retrieves Kaa operations data demultiplexer
     *
     * @return @link IKaaDataDemultiplexer @endlink object
     */
    virtual IKaaDataDemultiplexer&            getOperationDemultiplexer() = 0;

    /**
     * Retrieves Kaa bootstrap data multiplexer
     *
     * @return @link IKaaDataMultiplexer @endlink object
     */
    virtual IKaaDataMultiplexer&              getBootstrapMultiplexer() = 0;

    /**
     * Retrieves Kaa bootstrap data demultiplexer
     *
     * @return @link IKaaDataDemultiplexer @endlink object
     */
    virtual IKaaDataDemultiplexer&            getBootstrapDemultiplexer() = 0;

    virtual ~IKaaClient() { }
};

}


#endif /* IKAACLIENT_HPP_ */
