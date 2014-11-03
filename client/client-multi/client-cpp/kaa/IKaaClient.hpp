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

#ifndef IKAACLIENT_HPP_
#define IKAACLIENT_HPP_

#include "kaa/security/KeyUtils.hpp"
#include "kaa/KaaDefaults.hpp"

namespace kaa {

class IDeltaManager;
class IProfileManager;
class EventFamilyFactory;
class INotificationManager;
class IConfigurationManager;
class IEventListenersResolver;
class IExternalTransportManager;
class ISchemaPersistenceManager;
class IEndpointRegistrationManager;
class IConfigurationPersistenceManager;
class IKaaChannelManager;
class ILogCollector;
class IKaaDataMultiplexer;
class IKaaDataDemultiplexer;

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

#ifdef KAA_USE_CONFIGURATION
    /**
     * Retrieves Kaa delta manager.
     *
     * @return @link IDeltaManager @endlink object.
     *
     */
    virtual IDeltaManager&                    getDeltaManager() = 0;
#endif

    /**
     * Retrieves Kaa profile manager.
     *
     * @return @link IProfileManager @endlink object.
     *
     */
    virtual IProfileManager&                  getProfileManager() = 0;

    /**
     * Retrieves Kaa event family factory.
     *
     * @return @link IEventFamilyFactory @endlink object.
     *
     */
    virtual EventFamilyFactory&               getEventFamilyFactory() = 0;

#ifdef KAA_USE_NOTIFICATIONS
    /**
     * Retrieves Kaa notification manager.
     *
     * @return @link INotificationManager @endlink object.
     *
     */
    virtual INotificationManager&             getNotificationManager() = 0;
#endif

#ifdef KAA_USE_CONFIGURATION
    /**
     * Retrieves Kaa configuration manager.
     *
     * @return @link IConfigurationManager @endlink object.
     *
     */
    virtual IConfigurationManager&            getConfigurationManager() = 0;

    /**
     * Retrieves Kaa schema persistence manager.
     *
     * @return @link ISchemaPersistenceManager @endlink object.
     *
     */
    virtual ISchemaPersistenceManager&        getSchemaPersistenceManager() = 0;

    /**
     * Retrieves Kaa configuration persistence manager.
     *
     * @return @link IConfigurationPersistenceManager @endlink object.
     *
     */
    virtual IConfigurationPersistenceManager& getConfigurationPersistenceManager() = 0;
#endif

    /**
     * Retrieves Kaa endpoint registration manager
     *
     * @return @link IEndpointRegistrationManager @endlink object
     */
    virtual IEndpointRegistrationManager&     getEndpointRegistrationManager() = 0;

    /**
     * Retrieves Kaa event listeners resolver
     *
     * @return @link IEventListenersResolver @endlink object
     */
    virtual IEventListenersResolver&          getEventListenersResolver() = 0;

    /**
     * Retrieves Kaa channel manager
     *
     * @return @link IKaaChannelManager @endlink object
     */
    virtual IKaaChannelManager&               getChannelManager() = 0;

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

#ifdef KAA_USE_LOGGING
    /**
     * Retrieves Kaa log collector
     *
     * @return @link LogCollector @endlink object
     */
    virtual ILogCollector&                    getLogCollector() = 0;
#endif

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
